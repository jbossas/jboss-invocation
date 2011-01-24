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

package org.jboss.invocation.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.marshalling.FieldSetter;

/**
 * A unique identification of a method within some class or interface which is class loader-agnostic.  Suitable for
 * serialization as well as usage as a hash table key.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodIdentifier implements Serializable {

    private static final long serialVersionUID = -4303462176794600579L;

    private static final FieldSetter hashCodeSetter = FieldSetter.get(MethodIdentifier.class, "hashCode");

    private final String returnType;
    private final String name;
    private final String[] parameterTypes;
    private final transient int hashCode;
    private static final String[] NO_STRINGS = new String[0];
    private static final Map<String, Class<?>> PRIMITIVES;

    static {
        final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();
        primitives.put("boolean", boolean.class);
        primitives.put("byte", byte.class);
        primitives.put("char", char.class);
        primitives.put("double", double.class);
        primitives.put("float", float.class);
        primitives.put("int", int.class);
        primitives.put("long", long.class);
        primitives.put("short", short.class);
        primitives.put("void", void.class);
        PRIMITIVES = primitives;
    }

    private MethodIdentifier(final String returnType, final String name, final String... parameterTypes) {
        if (returnType == null) {
            throw new IllegalArgumentException("returnType is null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes is null");
        }
        this.returnType = returnType;
        this.name = name;
        this.parameterTypes = parameterTypes == null || parameterTypes.length == 0 ? NO_STRINGS : parameterTypes.clone();
        hashCode = calculateHash(returnType, name, parameterTypes);
    }

    private MethodIdentifier(final Method method) {
        returnType = method.getReturnType().getName();
        final String name = (this.name = method.getName());
        final Class<?>[] methodParameterTypes = method.getParameterTypes();
        final String[] parameterTypes = methodParameterTypes.length == 0 ? NO_STRINGS : namesOf(methodParameterTypes);
        hashCode = calculateHash(returnType, name, parameterTypes);
        this.parameterTypes = parameterTypes;
    }

    private static String[] namesOf(final Class<?>[] types) {
        final String[] strings = new String[types.length];
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            strings[i] = types[i].getName();
        }
        return strings;
    }

    private static Class<?>[] typesOf(final String[] names, final ClassLoader classLoader) throws ClassNotFoundException {
        final Class<?>[] types = new Class<?>[names.length];
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            final Class<?> prim = PRIMITIVES.get(names[i]);
            types[i] = prim == null ? Class.forName(names[i], false, classLoader) : prim;
        }
        return types;
    }

    private static int calculateHash(final String returnType, final String name, final String[] parameterTypes) {
        return name.hashCode() * 7 + (returnType.hashCode() * 7 + Arrays.hashCode(parameterTypes));
    }

    /**
     * Get the method name.
     *
     * @return the method name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parameter type names, as strings.
     *
     * @return the parameter type names
     */
    public String[] getParameterTypes() {
        final String[] parameterTypes = this.parameterTypes;
        return parameterTypes == NO_STRINGS ? parameterTypes : parameterTypes.clone();
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof MethodIdentifier && equals((MethodIdentifier)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(MethodIdentifier other) {
        return this == other || other != null && hashCode == other.hashCode && returnType.equals(other.returnType) && name.equals(other.name) && Arrays.equals(parameterTypes, other.parameterTypes);
    }

    /**
     * Get the hash code for this method identifier.  The hash code is equal to:
     * <pre>
     *    n * 7 + (r * 7 + a)
     * </pre>
     * where <em>n</em> is the method name's hash code, <em>r</em> is the method return type's name's hash code
     * and <em>a</em> is the result of calling {@link Arrays#hashCode(Object[])} on the parameter type name list (of
     * strings).
     *
     * @return the hash code
     */
    public int hashCode() {
        return hashCode;
    }

    /**
     * Look up a public method matching this method identifier using reflection.
     *
     * @param clazz the class to search
     * @return the method
     * @throws NoSuchMethodException if no such method exists
     * @throws ClassNotFoundException if one of the classes referenced by this identifier are not found in {@code clazz}'s
     *      class loader
     */
    public Method getPublicMethod(final Class<?> clazz) throws NoSuchMethodException, ClassNotFoundException {
        return clazz.getMethod(name, typesOf(parameterTypes, clazz.getClassLoader()));
    }

    /**
     * Get the human-readable representation of this identifier.
     *
     * @return the string
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("Method ").append(name).append('(');
        for (String type : parameterTypes) {
            b.append(type);
        }
        return b.append(')').toString();
    }

    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        hashCodeSetter.setInt(this, calculateHash(name, name, parameterTypes));
    }

    /**
     * Get an identifier for the given reflection method.
     *
     * @param method the method
     * @return the identifier
     */
    public static MethodIdentifier getIdentifierForMethod(final Method method) {
        return new MethodIdentifier(method);
    }

    /**
     * Construct a new instance using class objects for the parameter types.
     *
     * @param returnType the method return type
     * @param name the method name
     * @param parameterTypes the method parameter types
     * @return the identifier
     */
    public static MethodIdentifier getIdentifier(final Class<?> returnType, final String name, final Class<?>... parameterTypes) {
        return new MethodIdentifier(returnType.getName(), name, namesOf(parameterTypes));
    }

    /**
     * Construct a new instance using string names for the return and parameter types.
     *
     * @param returnType the return type name
     * @param name the method name
     * @param parameterTypes the method parameter type names
     * @return the identifier
     */
    public static MethodIdentifier getIdentifier(final String returnType, final String name, final String... parameterTypes) {
        return new MethodIdentifier(returnType, name, parameterTypes);
    }

    /**
     * The method identifier for {@code Object.equals()}.
     */
    public static final MethodIdentifier EQUALS = getIdentifier(boolean.class, "equals", Object.class);
    /**
     * The method identifier for {@code Object.hashCode()}.
     */
    public static final MethodIdentifier HASH_CODE = getIdentifier(int.class, "hashCode");
    /**
     * The method identifier for {@code Object.toString()}.
     */
    public static final MethodIdentifier TO_STRING = getIdentifier(String.class, "toString");
    /**
     * The method identifier for {@code Object.finalize()}.
     */
    public static final MethodIdentifier FINALIZE = getIdentifier(void.class, "finalize");
}
