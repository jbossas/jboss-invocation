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

import org.wildfly.common.function.ExceptionSupplier;

/**
 * An asynchronous interceptor, which can optionally defer execution to another thread or later time without blocking.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface AsynchronousInterceptor {

    /**
     * Process an invocation.  The invocation can be handled directly, or passed on to the next processor in the
     * chain.  This method <b>must</b> eventually call either {@link AsynchronousInterceptorContext#proceed()}
     * or {@link AsynchronousInterceptorContext#complete()}, otherwise the invocation may hang.
     *
     * @param context the interceptor context (not {@code null})
     * @throws Exception if an invocation exception occurred
     */
    void processInvocation(AsynchronousInterceptorContext context) throws Exception;

    /**
     * Handle the invocation result.  The default implementation delegates to {@link
     * AsynchronousInterceptorContext#getResult()} immediately.  Custom implementations should
     * perform any post-invocation cleanup task in a finally block.  This method <b>must</b> eventually call either
     * {@link AsynchronousInterceptorContext#getResult()} or {@link AsynchronousInterceptorContext#discardResult()}
     * to avoid possible resource leaks.
     *
     * @param context the interceptor context (not {@code null})
     * @return the invocation result
     * @throws Exception if an invocation exception occurred
     */
    default Object processResult(AsynchronousInterceptorContext context) throws Exception {
        return context.getResult();
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
}
