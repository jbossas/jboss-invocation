/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationDispatcher;
import org.jboss.invocation.InvocationReply;

/**
 * Proxy Factory that generates proxis that delegate all calls to an {@link InvocationDispatcher}.
 * <p>
 * Typical usage looks like:
 * <p>
 * 
 * <pre>
 * ProxyFactory&lt;SimpleClass&gt; proxyFactory = new ProxyFactory&lt;SimpleClass&gt;(SimpleClass.class);
 * SimpleClass instance = proxyFactory.newInstance(new SimpleDispatcher());
 * </pre>
 * 
 * </p>
 * This will create a proxy for SimpleClass, and return a new instance that handles invocations using the InvocationDispatcher
 * SimpleDispatcher.
 * <p>
 * Invocations on these proxies are very efficent, as no reflection is involved.
 * 
 * @author Stuart Douglas
 * 
 * @param <T>
 */
public class ProxyFactory<T> extends AbstractProxyFactory<T> {

    /**
     * Overrides superclass methods and forwards calls to the dispatcher
     * 
     * @author Stuart Douglas
     * 
     */
    protected class ProxyMethodBodyCreator implements MethodBodyCreator {

        // we simply want to load the corresponding identifier
        // and then forward it to the dispatcher
        @Override
        public void overrideMethod(ClassMethod method, Method superclassMethod) {
            CodeAttribute ca = method.getCodeAttribute();
            // first we need to check the constructed field
            ca.aload(0);
            ca.getfield(getClassName(), CONSTRUCTED_GUARD, "Z");
            // if the object has not been constructed yet invoke the superclass version of the method
            BranchEnd end = ca.ifne();
            ca.aload(0);
            ca.loadMethodParameters();
            ca.invokespecial(getSuperClassName(), method.getName(), method.getDescriptor());
            ca.returnInstruction();
            // normal invocation path begins here
            ca.branchEnd(end);
            ca.aload(0);
            ca.getfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
            // now we have the dispatcher on the stack, we need to build an invocation
            ca.newInstruction(Invocation.class.getName());
            ca.dup();
            // the constructor we are using is Invocation(final Class<?> declaringClass, final MethodIdentifier
            // methodIdentifier, final Object... args)
            ca.loadClass(getClassName());
            loadMethodIdentifier(superclassMethod, method);
            // now we need to stick the parameters into an array, boxing if nessesary
            String[] params = method.getParameters();
            ca.iconst(params.length);
            ca.anewarray("java/lang/Object");
            int loadPosition = 1;
            for (int i = 0; i < params.length; ++i) {
                ca.dup();
                ca.iconst(i);
                String type = params[i];
                if (type.length() == 1) { // primitive
                    char typeChar = type.charAt(0);
                    switch (typeChar) {
                        case 'I':
                            ca.iload(loadPosition);
                            Boxing.boxInt(ca);
                            break;
                        case 'S':
                            ca.iload(loadPosition);
                            Boxing.boxShort(ca);
                            break;
                        case 'B':
                            ca.iload(loadPosition);
                            Boxing.boxByte(ca);
                            break;
                        case 'Z':
                            ca.iload(loadPosition);
                            Boxing.boxBoolean(ca);
                            break;
                        case 'C':
                            ca.iload(loadPosition);
                            Boxing.boxChar(ca);
                            break;
                        case 'D':
                            ca.dload(loadPosition);
                            Boxing.boxDouble(ca);
                            loadPosition++;
                            break;
                        case 'J':
                            ca.lload(loadPosition);
                            Boxing.boxLong(ca);
                            loadPosition++;
                            break;
                        case 'F':
                            ca.fload(loadPosition);
                            Boxing.boxFloat(ca);
                            break;
                        default:
                            throw new RuntimeException("Unkown primitive type descriptor: " + typeChar);
                    }
                } else {
                    ca.aload(loadPosition);
                }
                ca.aastore();
                loadPosition++;
            }
            ca.invokespecial(Invocation.class.getName(), "<init>",
                    "(Ljava/lang/Class;Lorg/jboss/invocation/MethodIdentifier;[Ljava/lang/Object;)V");
            // now we have the invocation on top of the stack, with the dispatcher below it
            ca.invokeinterface(InvocationDispatcher.class.getName(), "dispatch",
                    "(Lorg/jboss/invocation/Invocation;)Lorg/jboss/invocation/InvocationReply;");
            ca.invokevirtual(InvocationReply.class.getName(), "getReply", "()Ljava/lang/Object;");
            if (superclassMethod.getReturnType() != void.class) {
                if (superclassMethod.getReturnType().isPrimitive()) {
                    Boxing.unbox(ca, method.getReturnType());
                } else {
                    ca.checkcast(superclassMethod.getReturnType().getName());
                }
            }
            ca.returnInstruction();
        }
    }

    /**
     * Implements the methods from the {@link ProxyInstance} interface
     * 
     * @author Stuart Douglas
     * 
     */
    protected class ProxyInstanceMethodBodyCreator implements MethodBodyCreator {

        @Override
        public void overrideMethod(ClassMethod method, Method superclassMethod) {
            CodeAttribute ca = method.getCodeAttribute();
            if (method.getName().equals("_getProxyInvocationDispatcher")) {
                ca.aload(0);
                ca.getfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
                ca.returnInstruction();
            } else if (method.getName().equals("_setProxyInvocationDispatcher")) {
                ca.aload(0);
                ca.aload(1);
                ca.putfield(getClassName(), INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
                ca.returnInstruction();
            } else {
                throw new RuntimeException("Unkown method on interface " + ProxyInstance.class);
            }
        }
    }

    /**
     * Generates a proxy constructor that delegates to super(), and then sets the constructed flag to true.
     * 
     * @author Stuart Douglas
     * 
     */
    protected class ProxyConstructorBodyCreator implements ConstructorBodyCreator {

        @Override
        public void overrideConstructor(ClassMethod method, Constructor<?> constructor) {
            CodeAttribute ca = method.getCodeAttribute();
            ca.aload(0);
            ca.iconst(0);
            ca.putfield(getClassName(), CONSTRUCTED_GUARD, "Z");
            ca.aload(0);
            ca.loadMethodParameters();
            ca.invokespecial(constructor);
            ca.aload(0);
            ca.iconst(1);
            ca.putfield(getClassName(), CONSTRUCTED_GUARD, "Z");
            ca.returnInstruction();
        }
    }

    /**
     * Generates the writereplace method if advanced serialization is enabled
     * 
     * @author Stuart Douglas
     * 
     */
    protected class WriteReplaceBodyCreator implements MethodBodyCreator {

        @Override
        public void overrideMethod(ClassMethod method, Method superclassMethod) {
            // superClassMethod will be null
            CodeAttribute ca = method.getCodeAttribute();
            ca.newInstruction(serializableProxyClass.getName());
            ca.dup();
            ca.invokespecial(serializableProxyClass.getName(), "<init>", "()V");
            ca.dup();
            ca.invokeinterface(SerializableProxy.class.getName(), "setProxyInstance", "(Lorg/jboss/proxy/ProxyInstance;)V");
            ca.returnInstruction();
        }
    }

    /**
     * Name of the field that holds the generated dispatcher on the generated proxy
     */
    public static final String INVOCATION_DISPATCHER_FIELD = "invocation$$dispatcher";

    /**
     * atomic integer used to generate proxy names
     */
    private static final AtomicInteger nameCount = new AtomicInteger();

    /**
     * this field on the generated class stores if the constructor has been completed yet. No methods will be delegated to the
     * dispacher until the constructor has finished. This prevents virtual methods called from the constructor being delegated
     * to a handler that is null.
     */
    private static final String CONSTRUCTED_GUARD = "proxy$$Constructor$$finished";

    /**
     * A list of additional interfaces that should be added to the proxy, and should have invocations delegated to the
     * dispatcher
     */
    private final Class<?>[] additionalInterfaces;

    /**
     * The type of {@link SerializableProxy} to generate from the writeReplace method
     */
    private Class<? extends SerializableProxy> serializableProxyClass;

    /**
     * 
     * @param className the name of the generated proxy
     * @param superClass the superclass of the generated proxy
     * @param classLoader the classloader to load the proxy with
     * @param protectionDomain the ProtectionDomain to define the class with
     * @param additionalInterfaces Additional interfaces that should be implemented by the proxy class
     */
    public ProxyFactory(String className, Class<T> superClass, ClassLoader classLoader, ProtectionDomain protectionDomain,
            Class<?>... additionalInterfaces) {
        super(className, superClass, classLoader, protectionDomain);
        this.additionalInterfaces = additionalInterfaces;
    }

    /**
     * 
     * @param className the name of the generated proxy
     * @param superClass the superclass of the generated proxy
     * @param classLoader the classloader to load the proxy with
     * @param additionalInterfaces Additional interfaces that should be implemented by the proxy class
     */
    public ProxyFactory(String className, Class<T> superClass, ClassLoader classLoader, Class<?>... additionalInterfaces) {
        super(className, superClass, classLoader);
        this.additionalInterfaces = additionalInterfaces;
    }

    /**
     * 
     * @param className The name of the proxy class
     * @param superClass The name of proxies superclass
     * @param additionalInterfaces Additional interfaces that should be implemented by the proxy class
     */
    public ProxyFactory(String className, Class<T> superClass, Class<?>... additionalInterfaces) {
        super(className, superClass);
        this.additionalInterfaces = additionalInterfaces;
    }

    /**
     * Create a ProxyFactory for the given superclass, using the default name and the classloader of the superClass
     * 
     * @param superClass the superclass of the generated proxy
     * @param additionalInterfaces Additional interfaces that should be implemented by the proxy class
     */
    public ProxyFactory(Class<T> superClass, Class<?>... additionalInterfaces) {
        super(superClass.getName() + "$$Proxy" + nameCount.incrementAndGet(), superClass);
        this.additionalInterfaces = additionalInterfaces;
    }

    /**
     * Create a new proxy, initialising it with the given dispatcher
     */
    public T newInstance(InvocationDispatcher dispatcher) throws InstantiationException, IllegalAccessException {
        T ret = newInstance();
        ((ProxyInstance) ret)._setProxyInvocationDispatcher(dispatcher);
        return ret;
    }

    @Override
    protected void generateClass() {
        classFile.addField(AccessFlag.PRIVATE, INVOCATION_DISPATCHER_FIELD, InvocationDispatcher.class);
        classFile.addField(AccessFlag.PRIVATE, CONSTRUCTED_GUARD, "Z");
        ProxyMethodBodyCreator creator = new ProxyMethodBodyCreator();
        overrideAllMethods(creator);
        for (Class<?> iface : additionalInterfaces) {
            addInterface(creator, iface);
        }
        overrideToString(creator);
        overrideEquals(creator);
        overrideHashcode(creator);
        addInterface(new ProxyInstanceMethodBodyCreator(), ProxyInstance.class);
        createConstructorDelegates(new ProxyConstructorBodyCreator());
        finalizeStaticConstructor();
    }

    private void createWriteReplace() {

    }

    /**
     * Sets the {@link SerializableProxy} class to emit from the proxies writeReplace method. If this is set to null (the
     * default) then no writeReplace method will be generated. The proxy may still be serializable, providing that the
     * superclass and {@link InvocationDispatcher} are both serializable.
     * <p>
     * 
     * @see SerializableProxy
     * @see DefaultSerializableProxy
     * @param serializableProxyClass
     * @throws IllegalStateException If the proxy class has already been generated
     */
    public void setSerializableProxyClass(Class<? extends SerializableProxy> serializableProxyClass) {
        if (classFile == null) {
            throw new IllegalStateException(
                    "Cannot set a ProxyFactories SerialiableProxyClass after the proxy has been created");
        }
        this.serializableProxyClass = serializableProxyClass;
    }
}
