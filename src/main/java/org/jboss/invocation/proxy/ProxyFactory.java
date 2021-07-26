/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.invocation.proxy;

import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;

/**
 * Proxy Factory that generates proxies that delegate all calls to an {@link InvocationHandler}.
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
 * @param <T> the superclass type
 */
public class ProxyFactory<T> extends AbstractProxyFactory<T> {

    private static final Class<?>[] NO_CLASSES = new Class<?>[0];

    private volatile Field invocationHandlerField;

    /**
     * Overrides superclass methods and forwards calls to the dispatcher.
     * 
     * @author Stuart Douglas
     * 
     */
    public class ProxyMethodBodyCreator implements MethodBodyCreator {

        /**
         * Override a method by forwarding all calls to the dispatcher.
         *
         * @param method the method to populate
         * @param superclassMethod the method to override
         */
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
                            throw new RuntimeException("Unknown primitive type descriptor: " + typeChar);
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
    public class ProxyConstructorBodyCreator implements ConstructorBodyCreator {

        /**
         * Override the given constructor.
         *
         * @param method the class method to populate
         * @param constructor the constructor to override
         */
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
     * Generates the writeReplace method if advanced serialization is enabled.
     * 
     * @author Stuart Douglas
     * 
     */
    public class WriteReplaceBodyCreator implements MethodBodyCreator {

        /**
         * Generate the writeReplace method body.
         *
         * @param method the method to populate
         * @param superclassMethod the method to override
         */
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
     * this field on the generated class stores if the constructor has been completed yet. No methods will be delegated to the
     * dispatcher until the constructor has finished. This prevents virtual methods called from the constructor being delegated
     * to a handler that is null.
     */
    public static final String CONSTRUCTED_GUARD = "proxy$$Constructor$$finished";

    /**
     * A list of additional interfaces that should be added to the proxy, and should have invocations delegated to the
     * dispatcher.
     */
    private final Class<?>[] additionalInterfaces;

    /**
     * The type of {@link SerializableProxy} to generate from the writeReplace method.
     */
    private Class<? extends SerializableProxy> serializableProxyClass;

    /**
     * Construct a new instance.
     *
     * @param proxyConfiguration The configuration to use to build the proxy
     */
    public ProxyFactory(ProxyConfiguration<T> proxyConfiguration) {
        super(proxyConfiguration.getProxyName(), proxyConfiguration.getSuperClass(), proxyConfiguration.getClassLoader(),
              proxyConfiguration.getClassFactory(), proxyConfiguration.getProtectionDomain(), proxyConfiguration.getMetadataSource());
        this.additionalInterfaces = proxyConfiguration.getAdditionalInterfaces().toArray(NO_CLASSES);
    }

    /**
     * Create a new proxy, initialising it with the given invocation handler.
     *
     * @param handler the invocation handler to use
     * @return the new proxy instance
     * @throws IllegalAccessException if the constructor is not accessible
     * @throws InstantiationException if instantiation failed due to an exception
     */
    public T newInstance(InvocationHandler handler) throws InstantiationException, IllegalAccessException {
        T ret = newInstance();
        setInvocationHandler(ret, handler);
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    protected void generateClass() {
        classFile.addField(AccessFlag.PRIVATE, INVOCATION_HANDLER_FIELD, InvocationHandler.class);
        classFile.addField(AccessFlag.PRIVATE, CONSTRUCTED_GUARD, "Z");
        if (serializableProxyClass != null) {
            createWriteReplace();
        }
        MethodBodyCreator creator = getDefaultMethodOverride();

        boolean isMessaging = false;
        for (int i = additionalInterfaces.length - 1; i >= 0; i--) {
            if ("MessageListener".equals(additionalInterfaces[i].getSimpleName())) {
                isMessaging = true;
                break;
            }
        }

        // For messaging endpoint, addInterface() is called after overrideAllMethods()
        // so that proxy methods from additional interfaces win.
        if (isMessaging) {
            overrideAllMethods(creator);
            for (Class<?> iface : additionalInterfaces) {
                addInterface(creator, iface);
            }
        } else {
            for (Class<?> iface : additionalInterfaces) {
                addInterface(creator, iface);
            }
            overrideAllMethods(creator);
        }

        overrideEquals(creator);
        overrideHashcode(creator);
        overrideToString(creator);
        createConstructorDelegates(new ProxyConstructorBodyCreator());
        finalizeStaticConstructor();
        for (Annotation annotation : this.getSuperClass().getDeclaredAnnotations()) {
            classFile.getRuntimeVisibleAnnotationsAttribute().addAnnotation(annotation);
        }
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
     * superclass and {@link InvocationHandler} are both serializable.
     * <p>
     * 
     * @see SerializableProxy
     * @see DefaultSerializableProxy
     * @param serializableProxyClass the proxy class
     * @throws IllegalStateException If the proxy class has already been generated
     */
    public void setSerializableProxyClass(Class<? extends SerializableProxy> serializableProxyClass) {
        if (classFile == null) {
            throw new IllegalStateException(
                    "Cannot set a ProxyFactories SerializableProxyClass after the proxy has been created");
        }
        this.serializableProxyClass = serializableProxyClass;
    }

    private Field getInvocationHandlerField() {
        if (invocationHandlerField == null) {
            synchronized (this) {
                if (invocationHandlerField == null) {
                    try {
                        invocationHandlerField = AccessController.doPrivileged(new PrivilegedExceptionAction<Field>() {
                            @Override
                            public Field run() throws NoSuchFieldException {
                                final Field field = defineClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
                                field.setAccessible(true);
                                return field;
                            }
                        });

                    } catch (PrivilegedActionException e) {
                        throw new RuntimeException("Could not find invocation handler on generated proxy", e);
                    }
                }
            }
        }
        return invocationHandlerField;
    }

    @Override
    public MethodBodyCreator getDefaultMethodOverride() {
        return new ProxyMethodBodyCreator();
    }

    /**
     * Sets the invocation handler for a proxy created from this factory.
     *
     * @param proxy the proxy to modify
     * @param handler the handler to use
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
     * Returns the invocation handler for a proxy created from this factory.
     *
     * @param proxy the proxy
     * @return the invocation handler
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
     * Sets the invocation handler for a proxy. This method is less efficient than
     * {@link #setInvocationHandler(Object, InvocationHandler)}, however it will work on any proxy, not just proxies from a
     * specific factory.
     *
     * @param proxy the proxy to modify
     * @param handler the handler to use
     */
    public static void setInvocationHandlerStatic(Object proxy, InvocationHandler handler) {
        try {
            final Field field = proxy.getClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
            AccessController.doPrivileged(new SetAccessiblePrivilege(field));
            field.set(proxy, handler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find invocation handler on generated proxy", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the {@link InvocationHandler} for a given proxy instance. This method is less efficient than
     * {@link #getInvocationHandler(Object)}, however it will work for any proxy, not just proxies from a specific factory
     * instance.
     *
     * @param proxy the proxy
     * @return the invocation handler
     */
    public static InvocationHandler getInvocationHandlerStatic(Object proxy) {
        try {
            final Field field = proxy.getClass().getDeclaredField(INVOCATION_HANDLER_FIELD);
            AccessController.doPrivileged(new SetAccessiblePrivilege(field));
            return (InvocationHandler) field.get(proxy);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find invocation handler on generated proxy", e);
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
