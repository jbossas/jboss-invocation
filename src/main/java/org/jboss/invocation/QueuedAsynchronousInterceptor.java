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
