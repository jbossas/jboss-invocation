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

import java.io.Serializable;

import org.wildfly.common.Assert;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ChainedInterceptorFactory implements InterceptorFactory, Serializable {

    private static final long serialVersionUID = -4300168217824335867L;

    private final InterceptorFactory[] interceptorFactories;

    ChainedInterceptorFactory(final InterceptorFactory... interceptorFactories) {
        Assert.checkNotNullParam("interceptorFactories", interceptorFactories);
        this.interceptorFactories = interceptorFactories;
    }

    /** {@inheritDoc}
     * @param context*/
    public Interceptor create(final InterceptorFactoryContext context) {
        final InterceptorFactory[] factories = interceptorFactories;
        final int length = factories.length;
        final Interceptor[] interceptors = new Interceptor[length];
        for (int i = 0; i < length; i++) {
            interceptors[i] = factories[i].create(context);
        }
        return new ChainedInterceptor(interceptors);
    }
}
