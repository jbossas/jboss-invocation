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

package org.jboss.invocation;

import static org.jboss.invocation.InvocationMessages.msg;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * An interceptor/invocation context object.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class InterceptorContext implements Cloneable, PrivilegedExceptionAction<Object> {
    private static final Interceptor[] EMPTY = new Interceptor[0];
    private static final Map<Class<?>, Class<?>> PRIMITIVES;

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

    private Object target;
    private Method method;
    private Constructor<?> constructor;
    private Object[] parameters;
    private Map<String, Object> contextData;
    private Object timer;
    private Interceptor[] interceptors = EMPTY;
    private int interceptorPosition = 0;
    private boolean blockingCaller = false;
    private final Map<Object, Object> privateData = new IdentityHashMap<Object, Object>(4);
    private InvocationContext invocationContext;

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
     * Get the invocation context.
     *
     * @return the invocation context
     */
    public InvocationContext getInvocationContext() {
        if(invocationContext == null) {
            //we lazily allocate the context
            //as if there are no user level interceptors it may not be required
            invocationContext = new Invocation();
        }
        return invocationContext;
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
     * Get the current interceptors.  The returned array should not be modified.
     *
     * @return the interceptors
     */
    public Interceptor[] getInterceptors() {
        return interceptors;
    }

    /**
     * Binary compatibility bridge for interceptors list.
     *
     * @return the interceptors array as a list
     */
    public List<Interceptor> getInterceptors$$bridge() {
        return Collections.unmodifiableList(Arrays.asList(interceptors));
    }

    /**
     * Returns the next interceptor index.
     *
     * @return the next interceptor index
     */
    public int getNextInterceptorIndex() {
        return interceptorPosition;
    }

    /**
     * Set the interceptor iterator.
     *
     * @param interceptors the interceptor list
     */
    public void setInterceptors(final Interceptor[] interceptors) {
        setInterceptors(interceptors, 0);
    }

    /**
     * Set the interceptor array from a list.
     *
     * @param interceptorList the interceptor list
     */
    public void setInterceptors(final List<Interceptor> interceptorList) {
        setInterceptors(interceptorList.toArray(new Interceptor[interceptorList.size()]), 0);
    }

    /**
     * Set the interceptors, with a starting index to proceed from.
     *
     * @param interceptors the interceptor array
     * @param nextIndex the next index to proceed
     */
    public void setInterceptors(final Interceptor[] interceptors, int nextIndex) {
        if (interceptors == null) {
            throw new IllegalArgumentException("interceptors is null");
        }
        this.interceptors = interceptors;
        this.interceptorPosition = nextIndex;
    }

    /**
     * Set the interceptors, with a starting index to proceed from.
     *
     * @param interceptorList the interceptor list
     * @param nextIndex the next index to proceed
     */
    public void setInterceptors(final List<Interceptor> interceptorList, int nextIndex) {
        setInterceptors(interceptorList.toArray(new Interceptor[interceptorList.size()]), nextIndex);
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

    /**
     * Pass the invocation on to the next step in the chain.
     *
     * @return the result
     * @throws Exception if an invocation throws an exception
     */
    public Object proceed() throws Exception {
        if (interceptorPosition < interceptors.length) {
            Interceptor next = interceptors[interceptorPosition++];
            try {
                return next.processInvocation(this);
            } finally {
                interceptorPosition--;
            }
        } else {
            throw msg.cannotProceed();
        }
    }

    /**
     * Synonymous with {@link #proceed()}; exists to implement {@link PrivilegedExceptionAction}.
     *
     * @return the result of {@link #proceed()}
     * @throws Exception if {@link #proceed()} threw an exception
     */
    public Object run() throws Exception {
        return proceed();
    }

    /**
     * Clone this interceptor context instance.  The cloned context will resume execution at the same point that
     * this context would have at the moment it was cloned.
     *
     * @return the copied context
     */
    public InterceptorContext clone() {
        final InterceptorContext clone = new InterceptorContext();
        final Map<String, Object> contextData = this.contextData;
        if (contextData != null) {
            clone.contextData = new HashMap<String, Object>(contextData);
        }
        clone.privateData.putAll(privateData);
        clone.target = target;
        clone.method = method;
        clone.constructor = constructor;
        clone.parameters = parameters;
        clone.timer = timer;
        clone.interceptors = interceptors;
        clone.interceptorPosition = interceptorPosition;
        return clone;
    }

    private class Invocation implements InvocationContext {
        public Object getTarget() {
            return target;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getParameters() {
            if(parameters == null) {
                throw new IllegalStateException("Cannot call InvocationContext.getParameters() in a lifecycle interceptor method");
            }
            return parameters;
        }

        public void setParameters(final Object[] params) {
            if(parameters == null) {
                throw new IllegalStateException("Cannot call InvocationContext.setParameters() in a lifecycle interceptor method");
            }
            if (params == null) {
                throw new IllegalArgumentException("Parameters must not be null");
            }
            if (method != null) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (params.length != parameterTypes.length) {
                    throw new IllegalArgumentException("Number of parameters must match number of method arguments");
                }
                for (int i = 0; i < params.length; ++i) {
                    final Object param = params[i];
                    final Class<?> type = parameterTypes[i];
                    if (param == null) {
                        if (type.isPrimitive()) {
                            throw new IllegalArgumentException("Null cannot be assigned to primitive parameter " + i + " (" + parameterTypes[i] + ")");
                        }
                    } else {
                        final Class<?> wrappedType = type.isPrimitive() ? PRIMITIVES.get(type) : type;
                        if (!wrappedType.isAssignableFrom(param.getClass())) {
                            throw new IllegalArgumentException("Parameter " + i + " (" + param + ") is not assignable to method parameter type " + parameterTypes[i]);
                        }
                    }
                }
            }
            parameters = params;
        }

        public Map<String, Object> getContextData() {
            return contextData;
        }

        public Object getTimer() {
            return timer;
        }

        public Object proceed() throws Exception {
            return InterceptorContext.this.proceed();
        }

        @Override
        public Constructor<?> getConstructor() {
            return constructor;
        }
    }
}
