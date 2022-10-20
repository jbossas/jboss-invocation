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

/**
 * A factory for method invoking interceptors.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInvokingInterceptorFactory implements InterceptorFactory {
    private final InterceptorInstanceFactory instanceFactory;
    private final Method method;

    /**
     * Construct a new instance.
     *
     * @param instanceFactory the instance factory
     * @param method the method to invoke
     */
    public MethodInvokingInterceptorFactory(final InterceptorInstanceFactory instanceFactory, final Method method) {
        this.instanceFactory = instanceFactory;
        this.method = method;
    }

    /** {@inheritDoc} */
    public Interceptor create(final InterceptorFactoryContext context) {
        return new MethodInvokingInterceptor(instanceFactory.createInstance(context), method);
    }
}
