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

package org.jboss.invocation.proxy;

import java.security.ProtectionDomain;

import org.jboss.classfilewriter.ClassFile;

/**
 * Base class for all class factories.
 * <p>
 * Sub classes should override {@link #generateClass()} to perform the actual class generation. The class will only be generated
 * once at most
 * 
 * @author Stuart Douglas
 * 
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
     * The ProtectionDomain that the generated class will be definied in
     */
    private final ProtectionDomain protectionDomain;

    /**
     * The class object for the generated class
     */
    private volatile Class<? extends T> generatedClass;

    /**
     * The class file that is used to generate the class.
     * <p>
     * Note that this object is not thread safe, so care should be taken by subclasses to ensure that no more than one thread
     * accesses this at once. In normal use this should not be an issue, as {@link #defineClass()} will only be called once by a
     * single thread.
     * <p>
     * This is set to null after the class is generated
     */
    protected ClassFile classFile;

    public AbstractClassFactory(String className, Class<T> superClass, ClassLoader classLoader,
            ProtectionDomain protectionDomain) {
        this.className = className;
        this.superClass = superClass;
        this.classLoader = classLoader;
        this.protectionDomain = protectionDomain;
        this.classFile = new ClassFile(className, superClass.getName());
    }

    public AbstractClassFactory(String className, Class<T> superClass, ClassLoader classLoader) {
        this(className, superClass, classLoader, null);
    }

    public AbstractClassFactory(String className, Class<T> superClass) {
        this(className, superClass, superClass.getClassLoader(), null);
    }

    protected abstract void generateClass();

    /**
     * Cleans up any resources left over from generating the class. Implementors should ensure they call super.cleanup();
     */
    protected abstract void cleanup();

    /**
     * Returns the {@link Class} object for the generated class, creating it if it does not exist
     * 
     */
    public Class<? extends T> defineClass() {
        if (generatedClass == null) {
            synchronized (this) {
                if (generatedClass == null) {
                    generateClass();
                    if (protectionDomain == null) {
                        generatedClass = (Class<? extends T>)classFile.define(classLoader);
                    } else {
                        generatedClass = (Class<? extends T>)classFile.define(classLoader, protectionDomain);
                    }
                    cleanup();
                    classFile = null;
                }
            }
        }
        return generatedClass;
    }

    /**
     * Creates a new instance of the generated class by invoking the default constructor.
     * <p>
     * If the generated class has not been defined it will be created
     * 
     */
    public T newInstance() throws InstantiationException, IllegalAccessException {
        return defineClass().newInstance();
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return superClass.getName();
    }

    public Class<T> getSuperClass() {
        return superClass;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }
}
