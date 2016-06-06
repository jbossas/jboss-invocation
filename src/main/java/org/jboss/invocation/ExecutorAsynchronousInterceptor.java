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
    private final boolean onlyIfBlocking;

    /**
     * Construct a new instance.
     *
     * @param executor the executor to dispatch to (must not be {@code null})
     * @param onlyIfBlocking {@code true} to only dispatch invocations that are blocking the caller; {@code false} to
     *      dispatch all invocations
     */
    public ExecutorAsynchronousInterceptor(final Executor executor, final boolean onlyIfBlocking) {
        this.onlyIfBlocking = onlyIfBlocking;
        Assert.checkNotNullParam("executor", executor);
        this.executor = executor;
    }

    public CancellationHandle processInvocation(final AsynchronousInterceptorContext context, final ResultHandler resultHandler) {
        final boolean ibc = context.isBlockingCaller();
        if (ibc) {
            context.setBlockingCaller(false);
        } else {
            if (onlyIfBlocking) {
                return context.proceed(resultHandler);
            }
        }
        // no matter what happens, the next interceptor can never be directly blocking the calling thread
        AsynchronousTask task = new AsynchronousTask(context, resultHandler);
        executor.execute(task);
        return task;
    }
}
