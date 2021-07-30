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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFactory;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.invocation.proxy.reflection.ClassMetadataSource;
import org.jboss.invocation.proxy.reflection.ReflectionMetadataSource;

/**
 * Class factory for classes that override superclass methods.
 * <p/>
 * This class extends {@link AbstractClassFactory} by adding convenience methods to override methods on the superclass.
 *
 * @param <T> the superclass type
 * @author Stuart Douglas
 */
public abstract class AbstractSubclassFactory<T> extends AbstractClassFactory<T> {


    /**
     * Tracks methods that have already been overridden
     */
    private final Set<MethodIdentifier> overriddenMethods = new HashSet<MethodIdentifier>();

    /**
     * Interfaces that have been added
     */
    private final Set<Class<?>> interfaces = new HashSet<Class<?>>();

    /**
     * The metadata source used to generate the proxy
     */
    protected final ReflectionMetadataSource reflectionMetadataSource;

    /**
     * Methods that should not be overridden by default
     */
    private static final Set<MethodIdentifier> SKIP_BY_DEFAULT;

    static {
        HashSet<MethodIdentifier> skip = new HashSet<MethodIdentifier>();
        skip.add(MethodIdentifier.CLONE);
        skip.add(MethodIdentifier.EQUALS);
        skip.add(MethodIdentifier.FINALIZE);
        skip.add(MethodIdentifier.HASH_CODE);
        skip.add(MethodIdentifier.TO_STRING);
        SKIP_BY_DEFAULT = Collections.unmodifiableSet(skip);
    }

    /**
     * Construct a new instance.
     *
     * @param className        the class name
     * @param superClass       the superclass
     * @param classLoader      the defining class loader
     * @param protectionDomain the protection domain
     * @deprecated use {@link #AbstractSubclassFactory(String, Class, ClassLoader, ClassFactory, ProtectionDomain, ReflectionMetadataSource)} instead
     */
    @Deprecated
    protected AbstractSubclassFactory(String className, Class<T> superClass, ClassLoader classLoader,
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
    protected AbstractSubclassFactory(String className, Class<T> superClass, ClassLoader classLoader, ClassFactory classFactory,
                                      ProtectionDomain protectionDomain, final ReflectionMetadataSource reflectionMetadataSource) {
        super(className, superClass, classLoader, classFactory, protectionDomain);
        this.reflectionMetadataSource = reflectionMetadataSource;
    }

    /**
     * Creates a new method on the generated class that overrides the given methods, unless a method with the same signature has
     * already been overridden.
     *
     * @param method     The method to override
     * @param identifier The identifier of the method to override
     * @param creator    The {@link MethodBodyCreator} used to create the method body
     * @return {@code true} if the method was successfully overridden, {@code false} otherwise
     */
    protected boolean overrideMethod(Method method, MethodIdentifier identifier, MethodBodyCreator creator) {
        if (!overriddenMethods.contains(identifier)) {
            overriddenMethods.add(identifier);
            creator.overrideMethod(classFile.addMethod(method), method);
            return true;
        }
        return false;
    }

    /**
     * Creates a new method on the generated class that overrides the given methods, unless a method with the same signature has
     * already been overridden.
     *
     * @param method     The method to override
     * @param identifier The identifier of the method to override
     * @param creator    The {@link MethodBodyCreator} used to create the method body
     * @return {@code false} if the method has already been overridden
     */
    protected boolean overrideMethod(ClassMethod method, MethodIdentifier identifier, MethodBodyCreator creator) {
        if (!overriddenMethods.contains(identifier)) {
            overriddenMethods.add(identifier);
            creator.overrideMethod(method, null);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() {
        overriddenMethods.clear();
    }

    /**
     * Calls {@link #overridePublicMethods(MethodBodyCreator)} with the default {@link MethodBodyCreator}.
     */
    protected void overridePublicMethods() {
        overridePublicMethods(getDefaultMethodOverride());
    }

    /**
     * Overrides all public methods on the superclass. The given {@link MethodBodyCreator} is used to generate the class body.
     * <p/>
     * Note this will not override <code>clone()</code>, <code>equals(Object)</code>, <code>finalize()</code>,
     * <code>hashCode()</code> and <code>toString()</code>, these should be overridden separately using
     * {@link #overrideClone(MethodBodyCreator)}
     * {@link #overrideEquals(MethodBodyCreator)}
     * {@link #overrideFinalize(MethodBodyCreator)}
     * {@link #overrideHashcode(MethodBodyCreator)}
     * {@link #overrideToString(MethodBodyCreator)}
     *
     * @param override the method body creator to use
     */
    protected void overridePublicMethods(final MethodBodyCreator override) {
        Class<?> currentClass = getSuperClass();
        ClassMetadataSource data;
        MethodIdentifier identifier;
        while (currentClass != null && currentClass != Object.class) {
            data = reflectionMetadataSource.getClassMetadata(currentClass);

            // first pass to exclude any final methods and their overridden methods in superclass
            for (Method method : data.getDeclaredMethods()) {
                if (Modifier.isFinal(method.getModifiers()) && method.getDeclaringClass() != Object.class) {
                    overriddenMethods.add(MethodIdentifier.getIdentifierForMethod(method));
                }
            }
            for (Method method : data.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue; // don't override non public methods
                }
                if (Modifier.isFinal(method.getModifiers())) {
                    continue; // don't override final methods
                }
                identifier = MethodIdentifier.getIdentifierForMethod(method);
                if (SKIP_BY_DEFAULT.contains(identifier)) {
                    continue; // don't override configured methods
                }
                overrideMethod(method, identifier, override);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Calls {@link #overrideAllMethods(MethodBodyCreator)} with the default {@link MethodBodyCreator}.
     */
    protected void overrideAllMethods() {
        overrideAllMethods(getDefaultMethodOverride());
    }

    /**
     * Overrides all methods on the superclass with the exception of <code>equals(Object)</code>, <code>hashCode()</code>,
     * <code>toString()</code> and <code>finalize()</code>. The given {@link MethodBodyCreator} is used to generate the class
     * body.
     * <p/>
     * Note this will not override <code>clone()</code>, <code>equals(Object)</code>, <code>finalize()</code>,
     * <code>hashCode()</code> and <code>toString()</code>, these should be overridden separately using
     * {@link #overrideClone(MethodBodyCreator)}
     * {@link #overrideEquals(MethodBodyCreator)}
     * {@link #overrideFinalize(MethodBodyCreator)}
     * {@link #overrideHashcode(MethodBodyCreator)}
     * {@link #overrideToString(MethodBodyCreator)}
     * <p/>
     * Note that private methods are not actually overridden, and if the sub-class is loaded by a different ClassLoader to the
     * parent class then neither will package-private methods. These methods will still be present on the new class however, and
     * can be accessed via reflection
     *
     * @param override the method body creator to use
     */
    protected void overrideAllMethods(final MethodBodyCreator override) {
        Class<?> currentClass = getSuperClass();
        ClassMetadataSource data;
        MethodIdentifier identifier;
        while (currentClass != null && currentClass != Object.class) {
            data = reflectionMetadataSource.getClassMetadata(currentClass);

            // first pass to exclude any final methods and their overridden methods in superclass
            for (Method method : data.getDeclaredMethods()) {
                if (Modifier.isFinal(method.getModifiers()) && method.getDeclaringClass() != Object.class) {
                    overriddenMethods.add(MethodIdentifier.getIdentifierForMethod(method));
                }
            }
            for (Method method : data.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue; // don't override static methods
                }
                if (Modifier.isPrivate(method.getModifiers())) {
                    continue; // don't override private methods
                }
                if (Modifier.isFinal(method.getModifiers())) {
                    continue; // don't override final methods
                }
                identifier = MethodIdentifier.getIdentifierForMethod(method);
                if (SKIP_BY_DEFAULT.contains(identifier)) {
                    continue; // don't override configured methods
                }
                overrideMethod(method, identifier, override);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Override the equals method using the default {@link MethodBodyCreator}.
     *
     * @return true if the method was not already overridden
     */
    protected boolean overrideEquals() {
        return overrideEquals(getDefaultMethodOverride());
    }

    /**
     * Override the equals method using the given {@link MethodBodyCreator}.
     *
     * @param creator the method body creator to use
     * @return true if the method was not already overridden
     */
    protected boolean overrideEquals(final MethodBodyCreator creator) {
        final ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(Object.class);
        final Method equals;
        try {
            equals = data.getMethod("equals", Boolean.TYPE, Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return overrideMethod(equals, MethodIdentifier.getIdentifierForMethod(equals), creator);
    }

    /**
     * Override the hashCode method using the default {@link MethodBodyCreator}.
     *
     * @return true if the method was not already overridden
     */
    protected boolean overrideHashcode() {
        return overrideHashcode(getDefaultMethodOverride());
    }

    /**
     * Override the hashCode method using the given {@link MethodBodyCreator}.
     *
     * @param creator the method body creator to use
     * @return true if the method was not already overridden
     */
    protected boolean overrideHashcode(final MethodBodyCreator creator) {
        final ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(Object.class);
        final Method hashCode;
        try {
            hashCode = data.getMethod("hashCode", Integer.TYPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return overrideMethod(hashCode, MethodIdentifier.getIdentifierForMethod(hashCode), creator);
    }

    /**
     * Override the toString method using the default {@link MethodBodyCreator}
     *
     * @return true if the method was not already overridden
     */
    protected boolean overrideToString() {
        return overrideToString(getDefaultMethodOverride());
    }

    /**
     * Override the toString method using the given {@link MethodBodyCreator}.
     *
     * @param creator the method body creator to use
     * @return true if the method was not already overridden
     */
    protected boolean overrideToString(final MethodBodyCreator creator) {
        final ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(Object.class);
        final Method toString;
        try {
            toString = data.getMethod("toString", String.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return overrideMethod(toString, MethodIdentifier.getIdentifierForMethod(toString), creator);
    }

    /**
     * Override the finalize method using the default {@link MethodBodyCreator}.
     *
     * @return true if the method was not already overridden
     */
    protected boolean overrideFinalize() {
        return overrideFinalize(getDefaultMethodOverride());
    }

    /**
     * Override the finalize method using the given {@link MethodBodyCreator}.
     *
     * @param creator the method body creator to use
     * @return true if the method was not already overridden
     */
    protected boolean overrideFinalize(final MethodBodyCreator creator) {
        final ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(Object.class);
        final Method finalize;
        try {
            finalize = data.getMethod("finalize", void.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return overrideMethod(finalize, MethodIdentifier.getIdentifierForMethod(finalize), creator);
    }

    /**
     * Override the clone method using the default {@link MethodBodyCreator}.
     *
     * @return true if the method was not already overridden
     */
    protected boolean overrideClone() {
        return overrideClone(getDefaultMethodOverride());
    }

    /**
     * Override the clone method using the given {@link MethodBodyCreator}.
     *
     * @param creator the method body creator to use
     * @return true if the method was not already overridden
     */
    protected boolean overrideClone(final MethodBodyCreator creator) {
        final ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(Object.class);
        final Method clone;
        try {
            clone = data.getMethod("clone", Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return overrideMethod(clone, MethodIdentifier.getIdentifierForMethod(clone), creator);
    }

    /**
     * Adds an interface to the generated subclass, using the default {@link MethodBodyCreator} to generate the method bodies.
     *
     * @param interfaceClass the interface to add
     */
    protected boolean addInterface(Class<?> interfaceClass) {
        return addInterface(getDefaultMethodOverride(), interfaceClass);
    }

    /**
     * Adds an interface to the generated subclass, using the given {@link MethodBodyCreator} to generate the method bodies
     *
     * @param override       the method body creator to use
     * @param interfaceClass the interface to add
     * @return true if the interface was not already overridden
     */
    protected boolean addInterface(MethodBodyCreator override, Class<?> interfaceClass) {
        if (interfaces.contains(interfaceClass)) {
            return false;
        }
        interfaces.add(interfaceClass);
        classFile.addInterface(interfaceClass.getName());
        final Set<Class<?>> interfaces = new HashSet<Class<?>>();
        final Set<Class<?>> toProcess = new HashSet<Class<?>>();
        toProcess.add(interfaceClass);

        //walk the interface hierarchy and get all methods
        while(!toProcess.isEmpty()) {
            Iterator<Class<?>> it = toProcess.iterator();
            final Class<?> c = it.next();
            it.remove();
            interfaces.add(c);
            for(Class<?> i : c.getInterfaces()) {
                if(!interfaces.contains(i)) {
                    toProcess.add(i);
                }
            }
        }
        ClassMetadataSource classMd = reflectionMetadataSource.getClassMetadata(getSuperClass());
        for(final Class<?> c : interfaces) {
            ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(c);
            for (Method method : data.getDeclaredMethods()) {
                Method classMethod = null;
                for(Method cm : classMd.getDeclaredMethods() ) {
                    if(method.getName().equals(cm.getName()) && method.getReturnType().equals(cm.getReturnType()) && Arrays.equals(method.getParameterTypes(), cm.getParameterTypes())) {
                        classMethod = cm;
                        break;
                    }
                }
                if ((classMethod == null || !Modifier.isFinal(classMethod.getModifiers())) && !Modifier.isStatic(method.getModifiers())) {
                    overrideMethod(method, MethodIdentifier.getIdentifierForMethod(method), override);
                }
            }
        }
        return true;
    }

    /**
     * Adds a constructor for every non-private constructor present on the superclass. The constructor bodies are generated with
     * the default {@link ConstructorBodyCreator}
     */
    protected void createConstructorDelegates() {
        createConstructorDelegates(getDefaultConstructorOverride());
    }

    /**
     * Adds constructors that delegate the the superclass constructor for all non-private constructors present on the superclass
     *
     * @param creator the constructor body creator to use
     */
    protected void createConstructorDelegates(ConstructorBodyCreator creator) {
        ClassMetadataSource data = reflectionMetadataSource.getClassMetadata(getSuperClass());
        for (Constructor<?> constructor : data.getConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                creator.overrideConstructor(classFile.addMethod(AccessFlag.PUBLIC, "<init>", "V", DescriptorUtils
                        .parameterDescriptors(constructor.getParameterTypes())), constructor);
            }
        }
    }

    /**
     * Returns the default {@link MethodBodyCreator} to use when creating overridden methods.
     *
     * @return the default method body creator
     */
    public MethodBodyCreator getDefaultMethodOverride() {
        return DefaultMethodBodyCreator.INSTANCE;
    }

    /**
     * Returns the default {@link ConstructorBodyCreator} to use then creating overridden subclasses.
     *
     * @return the default constructor body creator
     */
    public ConstructorBodyCreator getDefaultConstructorOverride() {
        return DefaultConstructorBodyCreator.INSTANCE;
    }
}
