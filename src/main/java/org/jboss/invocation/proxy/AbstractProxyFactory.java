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

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFactory;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.invocation.proxy.classloading.ClassIdentifier;
import org.jboss.invocation.proxy.classloading.MethodStore;
import org.jboss.invocation.proxy.reflection.ReflectionMetadataSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A subclass factory specializing in proxy generation.
 *
 * @param <T> the superclass type
 */
public abstract class AbstractProxyFactory<T> extends AbstractSubclassFactory<T> {

    private static final String METHOD_FIELD_PREFIX = "METHOD$$IDENTIFIER";

    private static final String METHOD_FIELD_DESCRIPTOR = "Ljava/lang/reflect/Method;";

    private final Map<Method, Integer> methodIdentifiers = new HashMap<Method, Integer>();

    private int identifierCount = 0;

    private ClassMethod staticConstructor;

    private final List<Method> cachedMethods = new ArrayList<Method>(0);


    /**
     * Construct a new instance.
     *
     * @param className        the class name
     * @param superClass       the superclass
     * @param classLoader      the defining class loader
     * @param protectionDomain the protection domain
     * @deprecated use {@link #AbstractProxyFactory(String, Class, ClassLoader, ClassFactory, ProtectionDomain, ReflectionMetadataSource)} instead
     */
    @Deprecated
    protected AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader,
                                   ProtectionDomain protectionDomain, final ReflectionMetadataSource reflectionMetadataSource) {
        this(className, superClass, classLoader, null, protectionDomain, reflectionMetadataSource);
    }

    /**
     * Construct a new instance.
     *
     * @param className        the class name
     * @param superClass       the superclass
     * @param classLoader      the defining class loader
     * @param protectionDomain the protection domain
     */
    protected AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader, ClassFactory classFactory,
                                   ProtectionDomain protectionDomain, final ReflectionMetadataSource reflectionMetadataSource) {
        super(className, superClass, classLoader, classFactory, protectionDomain, reflectionMetadataSource);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    /**
     * This method must be called by subclasses after they have finished generating the class.
     */
    protected void finalizeStaticConstructor() {
        setupCachedProxyFields();
        staticConstructor.getCodeAttribute().returnInstruction();
    }

    /**
     * Sets the accessible flag on the cached methods
     */
    @Override
    public void afterClassLoad(Class<?> clazz) {
        super.afterClassLoad(clazz);
        //force <clinit> to be run, while the correct ThreadLocal is set
        //if we do not run this then <clinit> may be run later, perhaps even in
        //another thread
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupCachedProxyFields() {
        cachedMethods.addAll(methodIdentifiers.keySet());

        //set the methods to be accessible
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                for (Method method : cachedMethods) {
                    method.setAccessible(true);
                }
                return null;
            }
        });

        //store the Method objects in a thread local, so that
        //the proxies <clinit> method can access them
        //this removes the need for reflection in the proxy <clinit> method
        final Method[] methods = new Method[identifierCount];
        for (Map.Entry<Method, Integer> entry : methodIdentifiers.entrySet()) {
            methods[entry.getValue()] = entry.getKey();
        }
        MethodStore.METHODS.put(new ClassIdentifier(classFile.getName(), getClassLoader()), methods);

        //add the bytecode to load the cached fields in the static constructor
        CodeAttribute ca = staticConstructor.getCodeAttribute();
        ca.getstatic(MethodStore.class.getName(), "METHODS", "Ljava/util/Map;");
        ca.newInstruction(ClassIdentifier.class);
        ca.dup();
        ca.ldc(classFile.getName());
        ca.loadClass(classFile.getName());
        ca.invokevirtual("java.lang.Class", "getClassLoader", "()Ljava/lang/ClassLoader;");
        ca.invokespecial(ClassIdentifier.class.getName(), "<init>", "(Ljava/lang/String;Ljava/lang/ClassLoader;)V");
        ca.invokeinterface(Map.class.getName(), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;");
        ca.checkcast("[Ljava/lang/reflect/Method;");
        for (int i = 0; i < identifierCount; ++i) {
            ca.dup();
            ca.ldc(i);
            ca.aaload();
            ca.putstatic(getClassName(), METHOD_FIELD_PREFIX + i, METHOD_FIELD_DESCRIPTOR);
        }
    }

    /**
     * Returns all Method objects that are cached by the proxy. These Methods objects are passed to the proxies
     * {@link InvocationHandler} when the corresponding proxy action is invoked
     *
     * @return The cached methods
     */
    public List<Method> getCachedMethods() {
        defineClass();
        return cachedMethods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() {
        staticConstructor = null;
        methodIdentifiers.clear();
        super.cleanup();
    }

    /**
     * Writes the bytecode to load an instance of Method for the given method onto the stack
     * <p/>
     * If loadMethod has not already been called for the given method then a static field to hold the method is added to the
     * class, and code is added to the static constructor to initialize the field to the correct Method.
     *
     * @param methodToLoad the method to load
     * @param method       the subclass method to populate
     */
    protected void loadMethodIdentifier(Method methodToLoad, ClassMethod method) {
        if (!methodIdentifiers.containsKey(methodToLoad)) {
            int identifierNo = identifierCount++;
            String fieldName = METHOD_FIELD_PREFIX + identifierNo;
            classFile.addField(AccessFlag.PRIVATE | AccessFlag.STATIC, fieldName, Method.class);
            methodIdentifiers.put(methodToLoad, identifierNo);

        }
        final Integer fieldNo = methodIdentifiers.get(methodToLoad);
        method.getCodeAttribute().getstatic(getClassName(), METHOD_FIELD_PREFIX + fieldNo, METHOD_FIELD_DESCRIPTOR);
    }

    /**
     * {@link PrivilegedAction} that loads all cached {@link Method} objects from a proxy class
     *
     * @author Stuart Douglas
     * @see AbstractProxyFactory#loadMethodIdentifier(Method, ClassMethod)
     */
    private class CachedMethodGetter implements PrivilegedAction<Method[]> {

        @Override
        public Method[] run() {
            Method[] methods = new Method[identifierCount];
            Class<?> clazz = defineClass();
            int i = 0;
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    if (field.getName().startsWith(METHOD_FIELD_PREFIX)) {
                        field.setAccessible(true);
                        methods[i] = (Method) field.get(null);
                        methods[i].setAccessible(true);
                        i++;
                    }
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return methods;
        }

    }
}

