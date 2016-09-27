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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@SuppressWarnings("serial")
final class AsynchronousTask extends AtomicReference<AsynchronousInterceptor.CancellationHandle> implements Runnable, AsynchronousInterceptor.CancellationHandle {
    static final Sentinel CANCEL_NORMAL = new Sentinel();
    static final Sentinel CANCEL_AGGRESSIVE = new Sentinel();
    private final AsynchronousInterceptorContext context;
    private final AsynchronousInterceptor.ResultHandler resultHandler;

    AsynchronousTask(final AsynchronousInterceptorContext context, final AsynchronousInterceptor.ResultHandler resultHandler) {
        this.context = context;
        this.resultHandler = resultHandler;
    }

    public void run() {
        AsynchronousInterceptor.CancellationHandle oldVal, newVal;
        oldVal = get();
        if (oldVal == CANCEL_NORMAL || oldVal == CANCEL_AGGRESSIVE) {
            // we've been cancelled already in another thread; just return
            return;
        }
        if (oldVal != null) {
            // already ran; should be impossible
            return;
        }
        newVal = context.proceed(resultHandler);
        while (! compareAndSet(oldVal, newVal)) {
            oldVal = get();
        }
        if (oldVal == CANCEL_NORMAL || oldVal == CANCEL_AGGRESSIVE) {
            // we've been cancelled already in another thread; pass it on to the handle we just got back
            newVal.cancel(oldVal == CANCEL_AGGRESSIVE);
            return;
        }
    }

    public void cancel(final boolean aggressiveCancelRequested) {
        AsynchronousInterceptor.CancellationHandle oldVal, newVal;
        oldVal = get();
        if (oldVal != null) {
            if (oldVal == CANCEL_AGGRESSIVE) {
                // nothing to do; return
                return;
            } else if (oldVal == CANCEL_NORMAL && aggressiveCancelRequested) {
                // we have to upgrade to aggressive cancel
                newVal = CANCEL_AGGRESSIVE;
                while (! compareAndSet(oldVal, newVal)) {
                    oldVal = get();
                    if (oldVal == CANCEL_AGGRESSIVE) {
                        // another thread did it for us
                        return;
                    }
                }
                return;
            } else {
                // handle already set; pass on cancel
                oldVal.cancel(aggressiveCancelRequested);
                return;
            }
        }
        newVal = aggressiveCancelRequested ? CANCEL_AGGRESSIVE : CANCEL_NORMAL;
        while (! compareAndSet(null, newVal)) {
            oldVal = get();
            if (oldVal != null) {
                // handle already set; pass on cancel
                oldVal.cancel(aggressiveCancelRequested);
                return;
            }
        }
        // we got in before the invocation proceeded; indicate successful cancellation
        resultHandler.setCancelled();
    }

    static final class Sentinel implements AsynchronousInterceptor.CancellationHandle {
        public void cancel(final boolean aggressiveCancelRequested) {
        }
    }
}
