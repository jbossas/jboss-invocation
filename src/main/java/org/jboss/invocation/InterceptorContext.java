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
import java.util.List;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * An interceptor/invocation context object.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class InterceptorContext extends AbstractInterceptorContext implements Cloneable, PrivilegedExceptionAction<Object> {
    private static final Interceptor[] EMPTY = new Interceptor[0];

    private Interceptor[] interceptors = EMPTY;
    private int interceptorPosition = 0;
    private InvocationContext invocationContext;

    public InterceptorContext() {
    }

    InterceptorContext(final InterceptorContext interceptorContext) {
        super(interceptorContext, true);
        interceptors = interceptorContext.interceptors;
        interceptorPosition = interceptorContext.interceptorPosition;
    }

    InterceptorContext(final AsynchronousInterceptorContext context) {
        super(context, false);
    }

    public Object getTarget() {
        return super.getTarget();
    }

    public void setTarget(final Object target) {
        super.setTarget(target);
    }

    public Method getMethod() {
        return super.getMethod();
    }

    public void setMethod(final Method method) {
        super.setMethod(method);
    }

    public Constructor<?> getConstructor() {
        return super.getConstructor();
    }

    public void setConstructor(final Constructor<?> constructor) {
        super.setConstructor(constructor);
    }

    public Object[] getParameters() {
        return super.getParameters();
    }

    public void setParameters(final Object[] parameters) {
        super.setParameters(parameters);
    }

    public Map<String, Object> getContextData() throws IllegalStateException {
        return super.getContextData();
    }

    public void setContextData(final Map<String, Object> contextData) {
        super.setContextData(contextData);
    }

    public Object getTimer() {
        return super.getTimer();
    }

    public void setTimer(final Object timer) {
        super.setTimer(timer);
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

    public <T> T getPrivateData(final Class<T> type) {
        return super.getPrivateData(type);
    }

    public Object getPrivateData(final Object key) {
        return super.getPrivateData(key);
    }

    public <T> T putPrivateData(final Class<T> type, final T value) {
        return super.putPrivateData(type, value);
    }

    public Object putPrivateData(final Object key, final Object value) {
        return super.putPrivateData(key, value);
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
    @SuppressWarnings("unused")
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

    public boolean isBlockingCaller() {
        return super.isBlockingCaller();
    }

    public void setBlockingCaller(final boolean blockingCaller) {
        super.setBlockingCaller(blockingCaller);
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
        return new InterceptorContext(this);
    }

    private class Invocation implements InvocationContext, PrivilegedExceptionAction<Object> {
        public Object getTarget() {
            return InterceptorContext.this.getTarget();
        }

        public Method getMethod() {
            return InterceptorContext.this.getMethod();
        }

        public Object[] getParameters() {
            final Object[] parameters = InterceptorContext.this.getParameters();
            if (parameters == null) {
                throw new IllegalStateException("Cannot call InvocationContext.getParameters() in a lifecycle interceptor method");
            }
            return parameters;
        }

        public void setParameters(final Object[] params) {
            final Object[] parameters = InterceptorContext.this.getParameters();
            if(parameters == null) {
                throw new IllegalStateException("Cannot call InvocationContext.setParameters() in a lifecycle interceptor method");
            }
            if (params == null) {
                throw new IllegalArgumentException("Parameters must not be null");
            }
            final Method method = InterceptorContext.this.getMethod();
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
            InterceptorContext.this.setParameters(params);
        }

        public Map<String, Object> getContextData() {
            return InterceptorContext.this.getContextData();
        }

        public Object getTimer() {
            return InterceptorContext.this.getTimer();
        }

        public Object proceed() throws Exception {
            return InterceptorContext.this.proceed();
        }

        public Object run() throws Exception {
            return InterceptorContext.this.proceed();
        }

        @Override
        public Constructor<?> getConstructor() {
            return InterceptorContext.this.getConstructor();
        }
    }
}
