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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.wildfly.common.Assert;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * An asynchronous interceptor, which can optionally defer execution to another thread or later time without blocking.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface AsynchronousInterceptor {

    /**
     * An empty asynchronous interceptor array.
     */
    AsynchronousInterceptor[] EMPTY_ARRAY = new AsynchronousInterceptor[0];

    /**
     * Process an invocation.  The invocation can be handled directly, or passed on to the next processor in the
     * chain.  This method <b>must</b> eventually call a method on the given {@link ResultHandler}, otherwise the invocation may hang.
     * <p>
     * This method must return a handle which can be used to request cancellation.
     *
     * @param context the interceptor context (not {@code null})
     * @param resultHandler the handler for the invocation result (not {@code null})
     * @return a handle which can be used to request a cancellation of the invocation (must not be {@code null})
     */
    CancellationHandle processInvocation(AsynchronousInterceptorContext context, ResultHandler resultHandler);


    /**
     * A handler for the result of a method invocation.  A call to any of these methods indicates that the invocation
     * is complete, one way or another.
     */
    interface ResultHandler {
        /**
         * Indicate that the invocation is complete, and that the result can be read from the given supplier.  Note
         * that reading the result may fail, even if the invocation itself succeeded.
         *
         * @param resultSupplier the result supplier (must not be {@code null})
         * @throws IllegalStateException if one of the {@code set*()} methods was already called
         */
        void setResult(ResultSupplier resultSupplier);

        /**
         * Indicate that the invocation was cancelled.
         *
         * @throws IllegalStateException if one of the {@code set*()} methods was already called
         */
        void setCancelled();

        /**
         * Indicate that the invocation failed.
         *
         * @param exception the failure cause (must not be {@code null})
         * @throws IllegalStateException if one of the {@code set*()} methods was already called
         */
        void setException(Exception exception);

        /**
         * Create a new result handler that delegates to this one but first performs an action.
         *
         * @param arg1 the first argument to pass to {@code action}
         * @param arg2 the second argument to pass to {@code action}
         * @param action the action to run (must not be {@code null})
         * @return the new result handler (not {@code null})
         */
        default <T, U> ResultHandler withAction(T arg1, U arg2, BiConsumer<T, U> action) {
            Assert.checkNotNullParam("action", action);
            final ResultHandler outer = this;
            return new ResultHandler() {
                public void setResult(final ResultSupplier resultSupplier) {
                    try {
                        action.accept(arg1, arg2);
                    } finally {
                        outer.setResult(resultSupplier);
                    }
                }

                public void setCancelled() {
                    try {
                        action.accept(arg1, arg2);
                    } finally {
                        outer.setCancelled();
                    }
                }

                public void setException(final Exception exception) {
                    try {
                        action.accept(arg1, arg2);
                    } finally {
                        outer.setException(exception);
                    }
                }
            };
        }

        /**
         * Create a new result handler that delegates to this one but first performs an action.
         *
         * @param argument the argument to pass to {@code action}
         * @param action the action to run (must not be {@code null})
         * @return the new result handler (not {@code null})
         */
        default <T> ResultHandler withAction(T argument, Consumer<T> action) {
            return withAction(action, argument, Consumer::accept);
        }

        /**
         * Create a new result handler that delegates to this one but first performs an action.
         *
         * @param action the action to run (must not be {@code null})
         * @return the new result handler (not {@code null})
         */
        default ResultHandler withAction(Runnable action) {
            return withAction(action, Runnable::run);
        }
    }

    /**
     * The invocation result supplier, which returns the invocation result or propagates its exception.  Some suppliers
     * need to perform special cleanup actions if the result is discarded; such suppliers will implement the {@link #discard()}
     * method.
     */
    interface ResultSupplier extends ExceptionSupplier<Object, Exception> {
        /**
         * Get the invocation result or throw its exception.
         *
         * @return the invocation result
         * @throws Exception the invocation exception
         */
        Object get() throws Exception;

        /**
         * Discard the result, performing any cleanup actions necessary.
         */
        default void discard() {}

        /**
         * Create a failed result supplier that throws the given exception.
         *
         * @param cause the exception to throw
         * @return the result supplier
         */
        static ResultSupplier failed(Exception cause) {
            return () -> { throw cause; };
        }

        /**
         * Create a result supplier that returns the given result.
         *
         * @param result the result to return
         * @return the result supplier
         */
        static ResultSupplier succeeded(Object result) {
            return () -> result;
        }

        /**
         * A result supplier that throws a cancellation exception.
         */
        ResultSupplier CANCELLED = () -> { throw InvocationMessages.msg.invocationCancelled(); };
    }

    /**
     * A handle for cancellation of an asynchronous invocation request.
     */
    interface CancellationHandle {

        /**
         * Attempt to cancel the in-progress invocation.  If the invocation is past the point where cancellation is
         * possible, the method has no effect.  The invocation may not support cancellation, in which case the method
         * has no effect.
         *
         * @param aggressiveCancelRequested {@code false} to only cancel if the method invocation has not yet begun, {@code true} to
         * attempt to cancel even if the method is running
         */
        void cancel(boolean aggressiveCancelRequested);

        /**
         * The null cancellation handle, which does nothing.
         */
        CancellationHandle NULL = aggressiveCancelRequested -> {};
    }
}
