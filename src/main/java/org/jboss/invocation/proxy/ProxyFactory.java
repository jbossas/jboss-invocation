/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.invocation.proxy;

import java.io.ObjectStreamException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;

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
 * Invocations on these proxies are very efficient, as no reflection is involved.
 * 
 * @author Stuart Douglas
 * 
 * @param <T>
 */
public class ProxyFactory<T> extends AbstractProxyFactory<T> {

    private volatile Field invocationHandlerField;

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
            ca.getfield(getClassName(), INVOCATION_HANDLER_FIELD, InvocationHandler.class);
            ca.aload(0);
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
            ca.invokeinterface(InvocationHandler.class.getName(), "invoke",
                    "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");

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
            ca.aload(0);
            ca.invokeinterface(SerializableProxy.class.getName(), "setProxyInstance", "(Ljava/lang/Object;)V");
            ca.returnInstruction();
        }
    }

    /**
     * Name of the field that holds the generated dispatcher on the generated proxy
     */
    public static final String INVOCATION_HANDLER_FIELD = "invocation$$dispatcher";

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
    public T newInstance(InvocationHandler handler) throws InstantiationException, IllegalAccessException {
        T ret = newInstance();
        setInvocationHandler(ret, handler);
        return ret;
    }

    @Override
    protected void generateClass() {
        classFile.addField(AccessFlag.PRIVATE, INVOCATION_HANDLER_FIELD, InvocationHandler.class);
        classFile.addField(AccessFlag.PRIVATE, CONSTRUCTED_GUARD, "Z");
        if (serializableProxyClass != null) {
            createWriteReplace();
        }
        ProxyMethodBodyCreator creator = new ProxyMethodBodyCreator();
        overrideAllMethods(creator);
        for (Class<?> iface : additionalInterfaces) {
            addInterface(creator, iface);
        }
        overrideToString(creator);
        overrideEquals(creator);
        overrideHashcode(creator);
        createConstructorDelegates(new ProxyConstructorBodyCreator());
        finalizeStaticConstructor();
    }

    private void createWriteReplace() {
        MethodIdentifier identifier = MethodIdentifier.getIdentifier(Object.class, "writeReplace");
        ClassMethod method = classFile.addMethod(AccessFlag.PROTECTED, "writeReplace", "Ljava/lang/Object;");
        method.addCheckedExceptions(ObjectStreamException.class);
        overrideMethod(method, identifier, new WriteReplaceBodyCreator());
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

    private Field getInvocationHandlerField() {
        if (invocationHandlerField == null) {
            synchronized (this) {
                if (invocationHandlerField == null) {
                    try {
                        invocationHandlerField = defineClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
                        AccessController.doPrivileged(new SetAccessiblePrivilege(invocationHandlerField));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException("Could not find inocation handler on generated proxy", e);
                    }
                }
            }
        }
        return invocationHandlerField;
    }

    /**
     * Sets the invocation hander for a proxy created from this factory
     */
    public void setInvocationHandler(Object proxy, InvocationHandler handler) {
        Field field = getInvocationHandlerField();
        try {
            field.set(proxy, handler);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * returns the invocation handler for a proxy created from this factory
     */
    public InvocationHandler getInvocationHandler(Object proxy) {
        Field field = getInvocationHandlerField();
        try {
            return (InvocationHandler) field.get(proxy);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Object is not a proxy of correct type", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the invocation handler for a proxy. This method will is less efficient than
     * {@link #setInvocationHandler(Object, InvocationHandler)}, however it will work on any proxy, not just proxies from a
     * specific factory
     */
    public static void setInvocationHandlerStatic(Object proxy, InvocationHandler handler) {
        try {
            final Field field = proxy.getClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
            AccessController.doPrivileged(new SetAccessiblePrivilege(field));
            field.set(proxy, handler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find inocation handler on generated proxy", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the {@link InvocationHandler} for a given proxy instance. This method is less effiecient than
     * {@link #getInvocationHandler(Object)}, however it will work for any proxy, not just proxies from a specific factory
     * instance
     */
    public static InvocationHandler getInvocationHandlerStatic(Object proxy) {
        try {
            final Field field = proxy.getClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
            AccessController.doPrivileged(new SetAccessiblePrivilege(field));
            return (InvocationHandler) field.get(proxy);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find inocation handler on generated proxy", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Object is not a proxy of correct type", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SetAccessiblePrivilege implements PrivilegedAction<Void> {
        private final AccessibleObject object;

        public SetAccessiblePrivilege(final AccessibleObject object) {
            this.object = object;
        }

        @Override
        public Void run() {
            object.setAccessible(true);
            return null;
        }
    }
}
