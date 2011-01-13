/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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
package org.jboss.invocation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Permission;
import org.jboss.marshalling.cloner.ClassLoaderClassCloner;
import org.jboss.marshalling.cloner.ClonerConfiguration;
import org.jboss.marshalling.cloner.ObjectCloner;
import org.jboss.marshalling.cloner.ObjectClonerFactory;
import org.jboss.marshalling.cloner.ObjectCloners;

/**
 * A unified view of a {@link Method} invocation.  Composes the target method, arguments, and mapped context into a single
 * entity.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @version $Revision: $
 */
public final class Invocation implements Serializable {

    private static final long serialVersionUID = 2691631710015102611L;

    /**
     * The invocation runtime permission.
     */
    public static final Permission INVOCATION_PERMISSION = new RuntimePermission("invocation");

    /**
     * This field contains the method-call arguments.
     */
    private final Object[] args;
    /**
     * This field contains the declaring class of the method that was invoked.
     */
    private final Class<?> declaringClass;
    /**
     * This field contains the identifier of the method which was invoked.
     */
    private final MethodIdentifier methodIdentifier;
    /**
     * This field contains the invocation properties.  As a special case, if the properties is empty at serialization
     * time, it is written as {@code null} to conserve bandwidth.
     */
    private transient volatile InvocationProperties properties;

    private static final Object[] NO_OBJECTS = new Object[0];

    /**
     * Construct a new instance.
     *
     * @param properties the invocation properties, or {@code null} for none
     * @param declaringClass the declaring class of the invoked method
     * @param methodIdentifier the method identifier of the invoked method
     * @param args the arguments passed to the invoked method
     */
    public Invocation(final InvocationProperties properties, final Class<?> declaringClass, final MethodIdentifier methodIdentifier, final Object[] args) {
        if (declaringClass == null) {
            throw new IllegalArgumentException("declaringClass is null");
        }
        if (methodIdentifier == null) {
            throw new IllegalArgumentException("methodIdentifier is null");
        }
        this.args = defaulted(args, NO_OBJECTS);
        this.declaringClass = declaringClass;
        this.methodIdentifier = methodIdentifier;
        this.properties = defaulted(properties, InvocationProperties.EMPTY);
    }

    /**
     * Construct a new instance.
     *
     * @param declaringClass the declaring class of the invoked method
     * @param methodIdentifier the identifier of the invoked method
     * @param args the arguments of the original invocation
     */
    public Invocation(final Class<?> declaringClass, final MethodIdentifier methodIdentifier, final Object... args) {
        this(null, declaringClass, methodIdentifier, (Object[]) args);
    }

    /**
     * Construct a new instance with no arguments.
     *
     * @param declaringClass the declaring class of the invoked method
     * @param methodIdentifier the identifier of the invoked method
     */
    public Invocation(final Class<?> declaringClass, final MethodIdentifier methodIdentifier) {
        this(null, declaringClass, methodIdentifier, (Object[]) null);
    }

    /**
     * Construct a new instance.
     *
     * @param properties the initial invocation properties
     * @param method the method which was invoked
     * @param args the arguments of the original invocation
     */
    public Invocation(final InvocationProperties properties, final Method method, final Object... args) {
        this(properties, method.getDeclaringClass(), MethodIdentifier.getIdentifierForMethod(method), args);
    }

    /**
     * Construct a new instance.
     *
     * @param method the method which was invoked
     * @param args the arguments of the original invocation
     */
    public Invocation(final Method method, final Object... args) {
        this(null, method.getDeclaringClass(), MethodIdentifier.getIdentifierForMethod(method), args);
    }

    //-------------------------------------------------------------------------------------||
    // Contracts --------------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * Get the declaring class of the method which was invoked.
     *
     * @return the declaring class
     */
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Get the identifier of the method which was invoked.
     *
     * @return the identifier
     */
    public MethodIdentifier getMethodIdentifier() {
        return methodIdentifier;
    }

    /**
     * Returns the arguments to be passed along to this method invocation
     *
     * @return the method call arguments
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Get the invocation context, which may be used to get information about this invocation.  It may also be used
     * to create a new, derived invocation context by way of the {@link InvocationProperties#builder()} method.
     *
     * @return the current invocation context
     */
    public InvocationProperties getProperties() {
        return properties;
    }

    /**
     * Replace this invocation's {@code InvocationContext}.
     *
     * @param properties the new invocation context
     */
    public void setProperties(final InvocationProperties properties) {
        this.properties = properties;
    }

    /**
     * Get a string representation of this object.
     *
     * @return the string
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("Invocation of ").append(methodIdentifier.toString()).append(" of ").append(declaringClass);
        b.append(" with arguments (");
        for (int i = 0; i < args.length; i++) {
            b.append(args[i]);
            if (i < args.length - 1) {
                b.append(',');
            }
        }
        b.append(')');
        return b.toString();
    }

    /**
     * Create a cloned invocation to another class loader.  All of the classes referenced by the invocation must be
     * visible (either from the same or different class loaders) to the new class loader.  Note that the invocation
     * itself will still be of the same type as this one.
     *
     * @param classLoader the destination class loader
     * @return the cloned invocation
     * @throws ClassNotFoundException if a class required by this invocation is not present in the destination
     *     class loader
     * @throws IOException if an I/O error occurs during the cloning process
     */
    public Invocation cloneTo(ClassLoader classLoader) throws ClassNotFoundException, IOException {
        final ObjectClonerFactory clonerFactory = ObjectCloners.getSerializingObjectClonerFactory();
        final ClonerConfiguration configuration = new ClonerConfiguration();
        final ClassLoaderClassCloner classCloner = new ClassLoaderClassCloner(classLoader);
        configuration.setClassCloner(classCloner);
        final ObjectCloner cloner = clonerFactory.createCloner(configuration);
        final Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = cloner.clone(args[i]);
        }
        return new Invocation(properties, classCloner.clone(declaringClass), methodIdentifier, newArgs);
    }

    //-------------------------------------------------------------------------------------||
    // Serialization ----------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        final InvocationProperties properties = this.properties;
        oos.writeObject(properties.isEmpty() ? null : properties);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (args == null) {
            throw new InvalidObjectException("args is null");
        }
        if (methodIdentifier == null) {
            throw new InvalidObjectException("methodIdentifier is null");
        }
        if (declaringClass == null) {
            throw new InvalidObjectException("declaringClass is null");
        }
        properties = defaulted((InvocationProperties) ois.readObject(), InvocationProperties.EMPTY);
    }

    private static <T> T defaulted(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
