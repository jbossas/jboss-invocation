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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * An immutable collection of contextual properties which may be associated with an {@link Invocation}.  To construct
 * instances, use the {@link Builder} interface by way of the {@link InvocationProperties#builder()} method.  An empty
 * context instance is available at {@link InvocationProperties#EMPTY}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @version $Revision: $
 */
public final class InvocationProperties implements Serializable {

    private static final long serialVersionUID = -8786118177256759054L;

    /**
     * An {@code InvocationContext} is written by way of an {@code Externalizable} proxy object.  Thus, no fields
     * are ever serialized for this object.
     */
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    private transient final Map<Object, Object> backingMap;

    private InvocationProperties(final Map<Object, Object> backingMap) {
        this.backingMap = backingMap;
    }

    //-------------------------------------------------------------------------------------||
    // Contracts --------------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * Obtains the context property associated with the specified key, or {@code null} if not found
     *
     * @param key the key to look up
     *
     * @return The value under the specified key, or null if not found
     *
     * @throws IllegalArgumentException If the key is not specified
     */
    public Object getProperty(Object key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        return backingMap.get(key);
    }

    /**
     * Obtains the context property associated with the specified key, or {@code null} if not found
     *
     * @param <T> Type of the object to be returned
     * @param key the key to look up
     * @param expectedType Expected type of the object to be returned
     *
     * @return The value under the specified key, or null if not found
     *
     * @throws IllegalArgumentException If the key is not specified
     * @throws ClassCastException If the expected type is not the correct type for the object under the specified key
     */
    public <T> T getProperty(Object key, Class<T> expectedType) throws IllegalArgumentException, ClassCastException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (expectedType == null) {
            throw new IllegalArgumentException("expectedType is null");
        }
        return expectedType.cast(backingMap.get(key));
    }

    /**
     * Determines whether the context properties are empty (ie. none exist)
     *
     * @return {@code true} if the map is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    /**
     * Get a builder which is prepopulated with the values from this context.
     *
     * @return a new builder
     */
    public Builder builder() {
        return new BuilderImpl(backingMap);
    }

    /**
     * Replace this instance with a serializable proxy.
     *
     * @return the replacement
     */
    protected Object writeReplace() {
        return new Serialized(backingMap);
    }

    /**
     * An empty invocation context.
     */
    public static final InvocationProperties EMPTY = new InvocationProperties(Collections.emptyMap());

    /**
     * A builder for invocation contexts.
     */
    public interface Builder {

        /**
         * Add or replace a property.
         *
         * @param key the property key
         * @param value the new value
         * @throws IllegalArgumentException if either the key or the value is {@code null}
         */
        void setProperty(Object key, Object value) throws IllegalArgumentException;

        /**
         * Remove a property, if it is present.
         *
         * @param key the property to remove
         */
        void removeProperty(Object key);

        /**
         * Create an invocation context from the current state of this builder.
         *
         * @return a new invocation context
         */
        InvocationProperties create();
    }

    static final class BuilderImpl implements Builder {
        private final FastCopyHashMap<Object, Object> map;

        BuilderImpl(final Map<Object, Object> map) {
            this.map = new FastCopyHashMap<Object, Object>(map);
        }

        public void setProperty(final Object key, final Object value) throws IllegalArgumentException {
            if (key == null) {
                throw new IllegalArgumentException("key is null");
            }
            if (value == null) {
                throw new IllegalArgumentException("value is null");
            }
            map.put(key, value);
        }

        public void removeProperty(final Object key) {
            if (key != null) map.remove(key);
        }

        public InvocationProperties create() {
            final FastCopyHashMap<Object, Object> map = this.map;
            switch (map.size()) {
                case 0: return EMPTY;
                case 1: {
                    final Object key = map.keySet().iterator().next();
                    return new InvocationProperties(Collections.singletonMap(key, map.get(key)));
                }
                default: {
                    return new InvocationProperties(map.clone());
                }
            }
        }
    }

    static final class Serialized implements Externalizable {

        private static final long serialVersionUID = -2451954101448516318L;

        private Map<Object, Object> values;

        public Serialized(final Map<Object, Object> values) {
            this.values = values;
        }

        public Serialized() {
        }

        public void writeExternal(final ObjectOutput objectOutput) throws IOException {
            objectOutput.writeInt(values.size());
            for (Map.Entry<Object, Object> entry : values.entrySet()) {
                objectOutput.writeObject(entry.getKey());
                objectOutput.writeObject(entry.getValue());
            }
        }

        public void readExternal(final ObjectInput objectInput) throws IOException, ClassNotFoundException {
            final int len = objectInput.readInt();
            switch (len) {
                case 0: values = Collections.emptyMap(); return;
                case 1: values = Collections.singletonMap(objectInput.readObject(), objectInput.readObject()); return;
            }
            values = new FastCopyHashMap<Object, Object>(len);
            for (int i = 0; i < len; i++) {
                values.put(objectInput.readObject(), objectInput.readObject());
            }
        }

        protected Object readResolve() {
            if (values.isEmpty()) return EMPTY;
            return new InvocationProperties(values);
        }
    }
}
