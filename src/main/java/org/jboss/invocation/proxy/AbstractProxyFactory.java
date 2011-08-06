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

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.code.CodeLocation;
import org.jboss.classfilewriter.util.DescriptorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A subclass factory specializing in proxy generation.
 *
 * @param <T> the superclass type
 */
public abstract class AbstractProxyFactory<T> extends AbstractSubclassFactory<T> {

    private static final String METHOD_FIELD_PREFIX = "METHOD$$IDENTIFIER";

    private static final String METHOD_FIELD_DESCRIPTOR = "Ljava/lang/reflect/Method;";

    private final Map<Method, String> methodIdentifiers = new HashMap<Method, String>();

    private int identifierCount = 0;

    private ClassMethod staticConstructor;

    private volatile Method[] cachedMethods;

    private static final AtomicInteger count = new AtomicInteger();

    /**
     * Construct a new instance.
     *
     * @param className   the class name
     * @param superClass  the superclass
     * @param classLoader the defining class loader
     */
    protected AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        super(className, superClass, classLoader);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    /**
     * Construct a new instance.
     *
     * @param className        the class name
     * @param superClass       the superclass
     * @param classLoader      the defining class loader
     * @param protectionDomain the protection domain
     */
    protected AbstractProxyFactory(String className, Class<T> superClass, ClassLoader classLoader,
                                   ProtectionDomain protectionDomain) {
        super(className, superClass, classLoader, protectionDomain);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    /**
     * Construct a new instance.
     *
     * @param className  the class name
     * @param superClass the superclass
     */
    protected AbstractProxyFactory(String className, Class<T> superClass) {
        super(className, superClass);
        staticConstructor = classFile.addMethod(AccessFlag.of(AccessFlag.PUBLIC, AccessFlag.STATIC), "<clinit>", "V");
    }

    /**
     * This method must be called by subclasses after they have finished generating the class.
     */
    protected void finalizeStaticConstructor() {
        staticConstructor.getCodeAttribute().returnInstruction();
    }

    /**
     * Sets the accessible flag on the cached methods
     */
    @Override
    public void afterClassLoad(Class<?> clazz) {
        super.afterClassLoad(clazz);
        cachedMethods = AccessController.doPrivileged(new CachedMethodGetter());
        methodIdentifiers.clear();
    }

    /**
     * Returns all Method objects that are cached by the proxy. These Methods objects are passed to the proxies
     * {@link InvocationHandler} when the corresponding proxy action is invoked
     *
     * @return The cached methods
     */
    public Method[] getCachedMethods() {
        defineClass();
        return cachedMethods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() {
        staticConstructor = null;
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
            methodIdentifiers.put(methodToLoad, fieldName);
            // we need to create the method in the static constructor
            CodeAttribute ca = staticConstructor.getCodeAttribute();
            // we need to call getDeclaredMethods and then iterate
            ca.loadClass(methodToLoad.getDeclaringClass().getName());
            ca.invokevirtual("java.lang.Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;");
            ca.dup();
            ca.arraylength();
            ca.dup();
            ca.istore(0);
            ca.aconstNull();
            ca.astore(1);
            ca.aconstNull();
            ca.astore(2);
            ca.aconstNull();
            ca.astore(3);
            // so here we have the array index on top of the stack, followed by the array
            CodeLocation loopBegin = ca.mark();
            BranchEnd loopEnd = ca.ifeq();
            ca.dup();
            ca.iinc(0, -1);
            ca.iload(0); // load the array index into the stack
            ca.dupX1(); // index, array, index, array
            ca.aaload();
            ca.checkcast("java.lang.reflect.Method");
            ca.dup();
            ca.astore(2); // Method, index, array
            // compare method names
            ca.invokevirtual("java.lang.reflect.Method", "getName", "()Ljava/lang/String;");
            ca.ldc(methodToLoad.getName());
            ca.invokevirtual("java.lang.Object", "equals", "(Ljava/lang/Object;)Z"); // int,index,array
            ca.ifEq(loopBegin);
            // compare return types
            ca.aload(2);
            ca.invokevirtual("java.lang.reflect.Method", "getReturnType", "()Ljava/lang/Class;");
            ca.loadType(DescriptorUtils.makeDescriptor(methodToLoad.getReturnType()));
            ca.invokevirtual("java.lang.Object", "equals", "(Ljava/lang/Object;)Z"); // int,index,array
            ca.ifEq(loopBegin);
            // load the method parameters
            Class<?>[] parameters = methodToLoad.getParameterTypes();
            ca.aload(2);
            ca.invokevirtual("java.lang.reflect.Method", "getParameterTypes", "()[Ljava/lang/Class;");
            ca.dup();
            ca.astore(3);
            ca.arraylength();
            ca.iconst(parameters.length);
            ca.ifIcmpne(loopBegin); // compare parameter array length

            for (int i = 0; i < parameters.length; ++i) {
                ca.aload(3);
                ca.iconst(i);
                ca.aaload();
                ca.loadType(DescriptorUtils.makeDescriptor(parameters[i]));
                ca.invokevirtual("java.lang.Object", "equals", "(Ljava/lang/Object;)Z"); // int,index,array
                ca.ifEq(loopBegin);
            }
            ca.pop();

            BranchEnd gotoEnd = ca.gotoInstruction(); // we have found the method, goto the pointwhere we write it to a static
            // field

            // throw runtime exception as we could not find the method.
            // this will only happen if the proxy isloaded into the wrong classloader
            ca.branchEnd(loopEnd);
            ca.newInstruction("java.lang.RuntimeException");
            ca.dup();
            ca.ldc("Could not find method " + methodToLoad);
            ca.invokespecial("java.lang.RuntimeException", "<init>", "(Ljava/lang/String;)V");
            ca.athrow();
            ca.branchEnd(gotoEnd);
            ca.pop();
            ca.aload(2);
            ca.checkcast("java.lang.reflect.Method");
            ca.putstatic(getClassName(), fieldName, METHOD_FIELD_DESCRIPTOR);

        }
        String fieldName = methodIdentifiers.get(methodToLoad);
        method.getCodeAttribute().getstatic(getClassName(), fieldName, METHOD_FIELD_DESCRIPTOR);
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

