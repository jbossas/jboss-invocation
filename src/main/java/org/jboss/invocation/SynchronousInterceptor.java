/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import org.wildfly.common.Assert;

/**
 * An interceptor which executes the synchronous part of an invocation.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class SynchronousInterceptor implements AsynchronousInterceptor {
    private final Interceptor[] interceptors;

    /**
     * Construct a new instance.
     *
     * @param interceptors the interceptors to apply to the synchronous invocation (must not be {@code null})
     */
    public SynchronousInterceptor(final Interceptor[] interceptors) {
        Assert.checkNotNullParam("interceptors", interceptors);
        this.interceptors = interceptors;
    }

    public void processInvocation(final AsynchronousInterceptorContext context) throws Exception {
        InterceptorContext synchContext = context.toSynchronous();
        synchContext.setInterceptors(interceptors);
        context.setResultSupplier(ResultSupplier.succeeded(synchContext.proceed()));
    }
}
