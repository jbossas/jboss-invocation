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
 * An interceptor which passes invocations through a series of nested interceptors.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class ChainedInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = 7951017996430287249L;

    private final Interceptor[] interceptors;

    /**
     * Construct a new instance.
     *
     * @param interceptors the child interceptors
     */
    ChainedInterceptor(final Interceptor... interceptors) {
        Assert.checkNotNullParam("interceptors", interceptors);
        this.interceptors = interceptors;
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        final int oldNext = context.getNextInterceptorIndex();
        final Interceptor[] old = context.getInterceptors();
        context.setInterceptors(interceptors);
        try {
            return context.proceed();
        } finally {
            context.setInterceptors(old, oldNext);
        }
    }
}
