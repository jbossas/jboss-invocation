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

package org.jboss.invocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
abstract class AbstractInterceptorContext {
    static final Map<Class<?>, Class<?>> PRIMITIVES;

    static {
        final HashMap<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
        map.put(Boolean.TYPE, Boolean.class);
        map.put(Character.TYPE, Character.class);
        map.put(Byte.TYPE, Byte.class);
        map.put(Short.TYPE, Short.class);
        map.put(Integer.TYPE, Integer.class);
        map.put(Long.TYPE, Long.class);
        map.put(Float.TYPE, Float.class);
        map.put(Double.TYPE, Double.class);
        PRIMITIVES = map;
    }

    private final Map<Object, Object> privateData;
    private Object target;
    private Method method;
    private Constructor<?> constructor;
    private Object[] parameters;
    private Map<String, Object> contextData;
    private Object timer;
    private boolean blockingCaller = false;

    AbstractInterceptorContext() {
        privateData = new IdentityHashMap<Object, Object>(4);
    }

    AbstractInterceptorContext(final AbstractInterceptorContext interceptorContext, final boolean copyData) {
        if (copyData) {
            privateData = new IdentityHashMap<Object, Object>(interceptorContext.privateData);
            contextData = new HashMap<String, Object>(interceptorContext.contextData);
        } else {
            privateData = interceptorContext.privateData;
            contextData = interceptorContext.contextData;
        }
        target = interceptorContext.target;
        method = interceptorContext.method;
        constructor = interceptorContext.constructor;
        parameters = interceptorContext.parameters;
        timer = interceptorContext.timer;
    }

    /**
     * Get the invocation target which is reported to the interceptor invocation context.
     *
     * @return the invocation target
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Set the invocation target which is reported to the interceptor invocation context.
     *
     * @param target the invocation target
     */
    public void setTarget(final Object target) {
        this.target = target;
    }

    /**
     * Get the invoked method which is reported to the interceptor invocation context.
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Set the invoked method which is reported to the interceptor invocation context.
     *
     * @param method the method
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * Get the intercepted constructor.
     *
     * @return the constructor
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * Set the intercepted constructor.
     *
     * @param constructor the constructor
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * Get the method parameters which are reported to the interceptor invocation context.
     *
     * @return the method parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Set the method parameters which are reported to the interceptor invocation context.
     *
     * @param parameters the method parameters
     */
    public void setParameters(final Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Get the context data which is reported to the interceptor invocation context.
     *
     * @return the context data
     * @throws IllegalStateException if the context data was never initialized
     */
    public Map<String, Object> getContextData() throws IllegalStateException {
        Map<String, Object> contextData = this.contextData;
        if (contextData == null) {
            throw new IllegalStateException("The context data was not set");
        }
        return contextData;
    }

    /**
     * Set the context data which is reported to the interceptor invocation context.
     *
     * @param contextData the context data
     */
    public void setContextData(final Map<String, Object> contextData) {
        this.contextData = contextData;
    }

    /**
     * Get the timer object which is reported to the interceptor invocation context.
     *
     * @return the timer object
     */
    public Object getTimer() {
        return timer;
    }

    /**
     * Set the timer object which is reported to the interceptor invocation context.
     *
     * @param timer the timer object
     */
    public void setTimer(final Object timer) {
        this.timer = timer;
    }

    /**
     * Get a private data item.
     *
     * @param type the data type class object
     * @param <T> the data type
     * @return the data item or {@code null} if no such item exists
     */
    public <T> T getPrivateData(Class<T> type) {
        return type.cast(privateData.get(type));
    }

    /**
     * Get a private data item.  The key will be looked up by object identity, not by value.
     *
     * @param key the object key
     * @return the private data object
     */
    public Object getPrivateData(Object key) {
        return privateData.get(key);
    }

    /**
     * Insert a private data item.
     *
     * @param type the data type class object
     * @param value the data item value, or {@code null} to remove the mapping
     * @param <T> the data type
     * @return the data item which was previously mapped to this position, or {@code null} if no such item exists
     */
    public <T> T putPrivateData(Class<T> type, T value) {
        if (value == null) {
            return type.cast(privateData.remove(type));
        } else {
            return type.cast(privateData.put(type, type.cast(value)));
        }
    }

    /**
     * Insert a private data item.  The key is used by object identity, not by value; in addition, if the key is
     * a {@code Class} then the value given must be assignable to that class.
     *
     * @param key the data key
     * @param value the data item value, or {@code null} to remove the mapping
     * @return the data item which was previously mapped to this position, or {@code null} if no such item exists
     */
    public Object putPrivateData(Object key, Object value) {
        if (key instanceof Class) {
            final Class<?> type = (Class<?>) key;
            if (value == null) {
                return type.cast(privateData.remove(type));
            } else {
                return type.cast(privateData.put(type, type.cast(value)));
            }
        } else {
            if (value == null) {
                return privateData.remove(key);
            } else {
                return privateData.put(key, value);
            }
        }
    }

    /**
     * Determine whether this invocation is currently blocking the calling thread.
     *
     * @return {@code true} if the calling thread is being blocked; {@code false} otherwise
     */
    public boolean isBlockingCaller() {
        return blockingCaller;
    }

    /**
     * Establish whether this invocation is currently blocking the calling thread.
     *
     * @param blockingCaller {@code true} if the calling thread is being blocked; {@code false} otherwise
     */
    public void setBlockingCaller(final boolean blockingCaller) {
        this.blockingCaller = blockingCaller;
    }
}
