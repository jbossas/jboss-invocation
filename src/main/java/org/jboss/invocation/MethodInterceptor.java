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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jakarta.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationMessages.msg;

import org.wildfly.common.Assert;

/**
 * A method interceptor.  The target method should be non-final and must accept no parameters or a single
 * parameter of type {@link InvocationContext} (or any supertype thereof).  The method must belong to the given
 * interceptor object's class or one of its supertypes.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInterceptor implements Interceptor {

    private final Object interceptorInstance;
    private final Method method;
    private final boolean withContext;
    private final boolean changeMethod;

    /**
     * Construct a new instance.  The given method should be a proper interceptor method; otherwise invocation may fail.
     *
     * @param interceptorInstance the interceptor object instance
     * @param method the interceptor method
     * @param changeMethod {@code true} to change the method on the context to equal the given method, {@code false} to leave it as-is
     */
    public MethodInterceptor(final Object interceptorInstance, final Method method, final boolean changeMethod) {
        Assert.checkNotNullParam("interceptorInstance", interceptorInstance);
        Assert.checkNotNullParam("method", method);
        this.changeMethod = changeMethod;
        this.method = method;
        this.interceptorInstance = interceptorInstance;
        checkMethodType(interceptorInstance);
        withContext = method.getParameterCount() == 1;
    }

    /**
     * Construct a new instance.  The given method should be a proper interceptor method; otherwise invocation may fail.
     *
     * @param interceptorInstance the interceptor object instance
     * @param method the interceptor method
     */
    public MethodInterceptor(final Object interceptorInstance, final Method method) {
        this(interceptorInstance, method, false);
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        try {
            Method method = this.method;
            if (withContext) {
                if (changeMethod) {
                    final Method oldMethod = context.getMethod();
                    context.setMethod(method);
                    try {
                        return method.invoke(interceptorInstance, context.getInvocationContext());
                    } finally {
                        context.setMethod(oldMethod);
                    }
                } else {
                    return method.invoke(interceptorInstance, context.getInvocationContext());
                }
            } else {
                method.invoke(interceptorInstance, null);
                return context.proceed();
            }
        } catch (IllegalAccessException e) {
            final IllegalAccessError n = new IllegalAccessError(e.getMessage());
            n.setStackTrace(e.getStackTrace());
            throw n;
        } catch (InvocationTargetException e) {
            throw Interceptors.rethrow(e.getCause());
        }
    }

    private void checkMethodType(final Object interceptorInstance) {
        final Method method = this.method;
        if (! method.getDeclaringClass().isInstance(interceptorInstance)) {
            throw msg.wrongInterceptorType();
        }
        final int modifiers = method.getModifiers();
        if (! Modifier.isPublic(modifiers) && ! method.isAccessible()) {
            throw msg.interceptorInaccessible();
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final int length = parameterTypes.length;
        if (length > 1) {
            throw msg.interceptorTargetOneParam();
        }
        // allow contravariant parameter types
        if (length == 1 && ! parameterTypes[0].isAssignableFrom(InvocationContext.class)) {
            throw msg.interceptorTargetAssignableFrom(InvocationContext.class);
        }
        // allow covariant return types (but not primitives, which are not Objects); also allow void for lifecycle interceptors
        final Class<?> returnType = method.getReturnType();
        if (returnType != void.class && ! Object.class.isAssignableFrom(returnType)) {
            throw msg.interceptorReturnObject();
        }
    }
}
