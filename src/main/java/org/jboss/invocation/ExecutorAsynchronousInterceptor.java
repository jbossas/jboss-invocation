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

import java.util.concurrent.Executor;

import org.wildfly.common.Assert;

/**
 * An interceptor which dispatches to an executor.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ExecutorAsynchronousInterceptor implements AsynchronousInterceptor {
    private final Executor executor;

    /**
     * Construct a new instance.
     *
     * @param executor the executor to dispatch to (must not be {@code null})
     */
    public ExecutorAsynchronousInterceptor(final Executor executor) {
        Assert.checkNotNullParam("executor", executor);
        this.executor = executor;
    }

    public void processInvocation(final AsynchronousInterceptorContext context) throws Exception {
        executor.execute(() -> {
            try {
                context.proceed();
            } catch (Exception e) {
                context.setResultSupplier(ResultSupplier.failed(e));
                context.complete();
            }
        });
    }
}
