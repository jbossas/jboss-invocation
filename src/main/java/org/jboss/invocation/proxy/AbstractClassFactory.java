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

import org.jboss.classfilewriter.ClassFile;

import java.security.ProtectionDomain;

/**
 * Base class for all class factories.
 * <p/>
 * Sub classes should override {@link #generateClass()} to perform the actual class generation. The class will only be generated
 * once at most
 *
 * @param <T> the type of the superclass
 * @author Stuart Douglas
 */
public abstract class AbstractClassFactory<T> {

    /**
     * The name of the generated class
     */
    private final String className;

    /**
     * superclass of the generated class
     */
    private final Class<T> superClass;

    /**
     * The class loader that is used to load the class
     */
    private final ClassLoader classLoader;

    /**
     * The ProtectionDomain that the generated class will be defined in
     */
    private final ProtectionDomain protectionDomain;

    /**
     * The class object for the generated class
     */
    private volatile Class<? extends T> generatedClass;

    /**
     * The class file that is used to generate the class.
     * <p/>
     * Note that this object is not thread safe, so care should be taken by subclasses to ensure that no more than one thread
     * accesses this at once. In normal use this should not be an issue, as {@link #generateClass()} will only be called once
     * by a single thread.
     */
    protected ClassFile classFile;

    /**
     * Flag that records if the class is generated
     */
    private volatile boolean classGenerated;

    /**
     * Construct a new instance.
     *
     * @param className        the generated class name
     * @param superClass       the superclass of the generated class
     * @param classLoader      the class loader used to load the class
     * @param protectionDomain the protection domain of the class
     */
    protected AbstractClassFactory(String className, Class<T> superClass, ClassLoader classLoader, ProtectionDomain protectionDomain) {
        if (classLoader == null) {
            throw new IllegalArgumentException("ClassLoader cannot be null when attempting to proxy " + superClass + ". If you are trying to proxy a JDK class you must specify the class loader explicitly to prevent memory leaks");
        }
        if(superClass == null) {
            throw new IllegalArgumentException("Superclass cannot be null for proxy " + className);
        }
        if(className == null) {
            throw new IllegalArgumentException("Class name cannot be null");
        }
        this.className = className;
        this.superClass = superClass;
        this.classLoader = classLoader;
        this.protectionDomain = protectionDomain;
        classFile = new ClassFile(className, superClass.getName());
    }

    /**
     * Construct a new instance with a {@code null} protection domain.
     *
     * @param className   the generated class name
     * @param superClass  the superclass of the generated class
     * @param classLoader the class loader used to load the class
     */
    protected AbstractClassFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        this(className, superClass, classLoader, null);
    }

    /**
     * Construct a new instance with a {@code null} protection domain.
     *
     * @param className  the generated class name
     * @param superClass the superclass of the generated class
     */
    protected AbstractClassFactory(String className, Class<T> superClass) {
        this(className, superClass, superClass.getClassLoader(), null);
    }

    /**
     * Generate the class.
     */
    protected abstract void generateClass();

    /**
     * Cleans up any resources left over from generating the class. Implementors should ensure they call super.cleanup();
     */
    protected abstract void cleanup();

    /**
     * Hook that is called after the class is loaded, before {@link #cleanup()} is called.
     * <p/>
     * This method may be called mutiple times, if the proxy is definined in multiple class loaders
     *
     * @param clazz The newly loaded class
     */
    public void afterClassLoad(Class<?> clazz) {

    }

    /**
     * Returns the {@link Class} object for the generated class, creating it if it does not exist
     *
     * @return the generated class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends T> defineClass() {
        if (generatedClass == null) {
            synchronized (this) {
                if (generatedClass == null) {
                    try {
                        // first check that the proxy has not already been created
                        generatedClass = (Class<? extends T>) classLoader.loadClass(this.className);
                    } catch (ClassNotFoundException e) {
                        buildClassDefinition();
                        if (protectionDomain == null) {
                            generatedClass = (Class<? extends T>) classFile.define(classLoader);
                        } else {
                            generatedClass = (Class<? extends T>) classFile.define(classLoader, protectionDomain);
                        }
                        afterClassLoad(generatedClass);
                    }
                    classFile = null;
                }
            }
        }
        return generatedClass;
    }

    /**
     * Checks if the proxy class is defined in the factories class loader
     *
     * @return true if the proxy class already exists
     */
    public boolean isProxyClassDefined() {
        return isProxyClassDefined(classLoader);
    }

    /**
     * Checks if the proxy class has been defined in the given class loader
     *
     * @param classLoader The class loader to check
     * @return true if the proxy is defined in the class loader
     */
    public boolean isProxyClassDefined(ClassLoader classLoader) {
        try {
            // first check that the proxy has not already been created
            classLoader.loadClass(this.className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Generates the class
     */
    public void buildClassDefinition() {
        if (!classGenerated) {
            synchronized (this) {
                if (!classGenerated) {
                    generateClass();
                    cleanup();
                    classGenerated = true;
                }
            }
        }
    }

    /**
     * Creates a new instance of the generated class by invoking the default constructor.
     * <p/>
     * If the generated class has not been defined it will be created.
     *
     * @return the new instance
     * @throws InstantiationException if the new instance could not be created
     * @throws IllegalAccessException if the new constructor is inaccessible for some reason
     */
    public T newInstance() throws InstantiationException, IllegalAccessException {
        return defineClass().newInstance();
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get the superclass name.
     *
     * @return the superclass name
     */
    public String getSuperClassName() {
        return superClass.getName();
    }

    /**
     * Get the superclass.
     *
     * @return the superclass
     */
    public Class<T> getSuperClass() {
        return superClass;
    }

    /**
     * Get the defining class loader.
     *
     * @return the defining class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Get the defined protection domain.
     *
     * @return the protection domain
     */
    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }
}
