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

/**
 * Weaves a series of interceptors into an existing interceptor chain.
 *
 * This interceptor is not very memory efficient, and should be avoided
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Deprecated
class WeavedInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = -2015905619563503718L;

    private final Interceptor[] interceptors;

    WeavedInterceptor(final Interceptor... interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public Object processInvocation(InterceptorContext context) throws Exception {
        final int oldNext = context.getNextInterceptorIndex();
        final Interceptor[] old = context.getInterceptors();
        final Interceptor[] interceptors = new Interceptor[this.interceptors.length + old.length - oldNext];
        System.arraycopy(this.interceptors, 0, interceptors, 0, this.interceptors.length);
        System.arraycopy(old, oldNext, interceptors, this.interceptors.length, old.length - oldNext);
        context.setInterceptors(interceptors);
        try {
            return context.proceed();
        }
        finally {
            context.setInterceptors(old, oldNext);
        }
    }
}
