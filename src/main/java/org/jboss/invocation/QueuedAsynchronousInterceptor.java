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
    private final ArrayDeque<AsynchronousInterceptorContext> queue = new ArrayDeque<>();
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
        final ArrayDeque<AsynchronousInterceptorContext> queue = this.queue;
        AsynchronousInterceptorContext context;
        for (;;) {
            synchronized (queue) {
                context = queue.poll();
                if (context == null) {
                    return;
                }
            }
            context.setResultSupplier(ResultSupplier.CANCELLED);
            context.complete();
        }
    }

    public int getQueueSize() {
        final ArrayDeque<AsynchronousInterceptorContext> queue = this.queue;
        synchronized (queue) {
            return queue.size();
        }
    }

    public void processInvocation(final AsynchronousInterceptorContext context) throws Exception {
        final ArrayDeque<AsynchronousInterceptorContext> queue = this.queue;
        synchronized (queue) {
            if (running) {
                queue.add(context);
                return;
            } else {
                running = true;
            }
        }
        context.proceed();
    }

    public Object processResult(final AsynchronousInterceptorContext context) throws Exception {
        try {
            return context.getResult();
        } finally {
            final ArrayDeque<AsynchronousInterceptorContext> queue = QueuedAsynchronousInterceptor.this.queue;
            final AsynchronousInterceptorContext next;
            synchronized (queue) {
                assert running;
                if (queue.isEmpty()) {
                    running = false;
                    next = null;
                } else {
                    next = queue.poll();
                }
            }
            if (next != null) executor.execute(() -> {
                try {
                    next.proceed();
                } catch (Exception e) {
                    next.setResultSupplier(ResultSupplier.failed(e));
                }
            });
        }
    }
}
