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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * An interceptor factory for interceptor objects which call a specific method via reflection.  If this factory is
 * called more than once in the same context, it will return the same interceptor instance.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInterceptorFactory implements InterceptorFactory {
    private final InterceptorInstanceFactory instanceFactory;
    private final Method interceptorMethod;
    private final boolean changeMethod;

    /**
     * Construct a new instance.
     *
     * @param instanceFactory the instance factory for the interceptor instance
     * @param interceptorMethod the interceptor method
     * @param changeMethod {@code true} to change the method on the context to equal the given method, {@code false} to leave it as-is
     */
    public MethodInterceptorFactory(final InterceptorInstanceFactory instanceFactory, final Method interceptorMethod, final boolean changeMethod) {
        this.instanceFactory = instanceFactory;
        this.interceptorMethod = interceptorMethod;
        this.changeMethod = changeMethod;
    }

    /**
     * Construct a new instance.
     *
     * @param instanceFactory the instance factory for the interceptor instance
     * @param interceptorMethod the interceptor method
     */
    public MethodInterceptorFactory(final InterceptorInstanceFactory instanceFactory, final Method interceptorMethod) {
        this(instanceFactory, interceptorMethod, false);
    }

    /** {@inheritDoc} */
    public Interceptor create(final InterceptorFactoryContext context) {
        final Map<Object,Object> map = context.getContextData();
        if (map.containsKey(this)) {
            return (Interceptor) map.get(this);
        } else {
            final MethodInterceptor interceptor = new MethodInterceptor(instanceFactory.createInstance(context), interceptorMethod, changeMethod);
            map.put(this, interceptor);
            return interceptor;
        }
    }
}
