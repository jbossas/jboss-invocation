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
