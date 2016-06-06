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

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * An interceptor which queues concurrent accesses such that only one invocation runs at a time.  The invocations
 * are run in FIFO (first-in, first-out) order.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class QueuedAsynchronousInterceptor implements AsynchronousInterceptor {
    private boolean running;
    private final ArrayDeque<AsynchronousTask> queue = new ArrayDeque<>();
    private final Executor executor;

    /**
     * Construct a new instance.
     *
     * @param executor the executor to use to continue previously suspended invocations
     */
    public QueuedAsynchronousInterceptor(final Executor executor) {
        this.executor = executor;
    }

    /**
     * Cancel all currently waiting invocations.
     */
    public void cancelAll() {
        final ArrayDeque<AsynchronousTask> queue = this.queue;
        AsynchronousTask task;
        for (;;) {
            synchronized (queue) {
                task = queue.poll();
                if (task == null) {
                    return;
                }
            }
            // flag doesn't matter because this task was in the queue (i.e. cancel works regardless of the flag)
            task.cancel(false);
        }
    }

    public int getQueueSize() {
        final ArrayDeque<AsynchronousTask> queue = this.queue;
        synchronized (queue) {
            return queue.size();
        }
    }

    public CancellationHandle processInvocation(final AsynchronousInterceptorContext context, final ResultHandler resultHandler) {
        final ArrayDeque<AsynchronousTask> queue = this.queue;
        synchronized (queue) {
            if (running) {
                final AsynchronousTask task = new AsynchronousTask(context, resultHandler);
                queue.add(task);
                return task;
            } else {
                running = true;
            }
        }
        return context.proceed(new ResultHandler() {
            public void setResult(final ResultSupplier resultSupplier) {
                try {
                    runNext();
                } finally {
                    resultHandler.setResult(resultSupplier);
                }
            }

            public void setCancelled() {
                try {
                    runNext();
                } finally {
                    resultHandler.setCancelled();
                }
            }

            public void setException(final Exception exception) {
                try {
                    runNext();
                } finally {
                    resultHandler.setException(exception);
                }
            }

            private void runNext() {
                final ArrayDeque<AsynchronousTask> queue = QueuedAsynchronousInterceptor.this.queue;
                final AsynchronousTask next;
                synchronized (queue) {
                    assert running;
                    if (queue.isEmpty()) {
                        running = false;
                        next = null;
                    } else {
                        next = queue.poll();
                    }
                }
                if (next != null) executor.execute(next);
            }
        });
    }
}
