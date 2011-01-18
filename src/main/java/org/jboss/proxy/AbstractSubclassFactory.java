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
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.jboss.invocation.MethodIdentifier;

/**
 * Class factory for classes that override superclass methods
 * 
 * @author Stuart Douglas
 * 
 */
public abstract class AbstractSubclassFactory<T> extends AbstractClassFactory<T> {

    public AbstractSubclassFactory(String className, Class<T> superClass, ClassLoader classLoader,
            ProtectionDomain protectionDomain) {
        super(className, superClass, classLoader, protectionDomain);
    }

    public AbstractSubclassFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        super(className, superClass, classLoader);
    }

    public AbstractSubclassFactory(String className, Class<T> superClass) {
        super(className, superClass);
    }

    /**
     * overrides all public methods on the superclass. The default {@link MethodBodyCreator} is used to generate the class body
     */
    protected void overridePublicMethods(boolean includeEquals, boolean includeHashcode, boolean includeToString) {
        overridePublicMethods(getDefaultMethodOverride(), includeEquals, includeHashcode, includeToString);
    }
    /**
     * overrides all public methods on the superclass. The given {@link MethodBodyCreator} is used to generate the class body
     */
    protected void overridePublicMethods(MethodBodyCreator override, boolean includeEquals, boolean includeHashcode,
            boolean includeToString) {
        for (Method method : getSuperClass().getMethods()) {
            MethodIdentifier identifier = MethodIdentifier.getIdentifierForMethod(method);
            if (Modifier.isFinal(method.getModifiers())) {
                continue;
            }
            if (identifier.equals(MethodIdentifier.EQUALS)) {
                if (includeEquals) {
                    override.overrideMethod(classFile.addMethod(method), method);
                }
            } else if (identifier.equals(MethodIdentifier.HASH_CODE)) {
                if (includeHashcode) {
                    override.overrideMethod(classFile.addMethod(method), method);
                }
            } else if (identifier.equals(MethodIdentifier.TO_STRING)) {
                if (includeToString) {
                    override.overrideMethod(classFile.addMethod(method), method);
                }
            } else {
                override.overrideMethod(classFile.addMethod(method), method);
            }
        }
    }

    /**
     * Overrides all methods on the superclass with the exception of <code>equals(OBject)</code>, <code>hashCode()</code>,
     * <code>toString()</code> and <code>finalize()</code>. The default {@link MethodBodyCreator} is used to generate the class
     * body.
     * <p>
     * Note that private methods are not actually overriden, and if the sub-class is loaded by a different ClassLoader to the
     * parent class then neither will package-private methods.
     * 
     */
    protected void overrideAllMethods() {
        overrideAllMethods(getDefaultMethodOverride());
    }

    /**
     * Overrides all methods on the superclass with the exception of <code>equals(OBject)</code>, <code>hashCode()</code>,
     * <code>toString()</code> and <code>finalize()</code>. The given {@link MethodBodyCreator} is used to generate the class
     * body
     * <p>
     * Note that private methods are not actually overriden, and if the sub-class is loaded by a different ClassLoader to the
     * parent class then neither will package-private methods.
     * 
     */
    protected void overrideAllMethods(MethodBodyCreator override) {
        Set<MethodIdentifier> methodIdentifiers = new HashSet<MethodIdentifier>();
        Class<?> currentClass = getSuperClass();
        while (currentClass != null) {
            for (Method method : getSuperClass().getDeclaredMethods()) {
                // do not override static or private methods
                if (Modifier.isStatic(method.getModifiers()) || Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }
                // make sure we do not override methods twice
                MethodIdentifier identifier = MethodIdentifier.getIdentifierForMethod(method);
                if (methodIdentifiers.contains(identifier)) {
                    continue;
                }
                methodIdentifiers.add(identifier);
                // don't attempt to override final methods
                if (Modifier.isFinal(method.getModifiers()) || Modifier.isNative(method.getModifiers())) {
                    continue;
                }
                if (!(identifier.equals(MethodIdentifier.EQUALS) || identifier.equals(MethodIdentifier.HASH_CODE)
                        || identifier.equals(MethodIdentifier.TO_STRING) || identifier.equals(MethodIdentifier.FINALIZE))) {
                    override.overrideMethod(classFile.addMethod(method), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Override the equals method using the given {@link MethodBodyCreator}
     */
    public void overrideEquals(MethodBodyCreator creator) {
        Method equals = null;
        try {
            equals = getSuperClass().getMethod("equals", Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        creator.overrideMethod(classFile.addMethod(equals), equals);
    }

    /**
     * Override the hashCode method using the given {@link MethodBodyCreator}
     */
    public void overrideHashcode(MethodBodyCreator creator) {
        Method hashCode = null;
        try {
            hashCode = getSuperClass().getMethod("hashCode");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        creator.overrideMethod(classFile.addMethod(hashCode), hashCode);
    }

    /**
     * Override the toString method using the given {@link MethodBodyCreator}
     */
    public void overrideToString(MethodBodyCreator creator) {
        Method toString = null;
        try {
            toString = getSuperClass().getMethod("toString");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        creator.overrideMethod(classFile.addMethod(toString), toString);
    }

    /**
     * Adds an interface to the generated subclass, using the default {@link MethodBodyCreator} to generate the method bodies
     */
    protected void addInterface(Class<?> interfaceClass) {
        addInterface(getDefaultMethodOverride(), interfaceClass);
    }

    /**
     * Adds an interface to the generated subclass, using the given {@link MethodBodyCreator} to generate the method bodies
     */
    protected void addInterface(MethodBodyCreator override, Class<?> interfaceClass) {
        classFile.addInterface(interfaceClass.getName());
        for (Method method : interfaceClass.getMethods()) {
            override.overrideMethod(classFile.addMethod(method), method);
        }
    }

    /**
     * Adds a constructor for every non-private constructor present on the superclass. The constrcutor bodies are generated with
     * the default {@link ConstructorBodyCreator}
     */
    protected void createConstructorDelegates() {
        createConstructorDelegates(getDefaultConstructorOverride());
    }

    /**
     * Adds constructors that delegate the the superclass constructor for all non-private constructors present on the superclass
     */
    protected void createConstructorDelegates(ConstructorBodyCreator creator) {
        for (Constructor<?> constructor : getSuperClass().getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                creator.overrideConstructor(classFile.addConstructor(constructor), constructor);
            }
        }
    }

    /**
     * returns the default {@link MethodBodyCreator} to use when creating overriden methods
     */
    public MethodBodyCreator getDefaultMethodOverride() {
        return DefaultMethodBodyCreator.INSTANCE;
    }

    /**
     * returns the default {@link ConstructorBodyCreator} to use then creating overriden subclasses
     */
    public ConstructorBodyCreator getDefaultConstructorOverride() {
        return DefaultConstructorBodyCreator.INSTANCE;
    }

}
