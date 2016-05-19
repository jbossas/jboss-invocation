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

import java.util.Collection;

import static org.jboss.invocation.InvocationMessages.msg;

/**
 * Interceptor utility and factory methods.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Interceptors {

    private Interceptors() {
    }

    /**
     * Get an interceptor that is always invoked first.  This interceptor is responsible for correctly handling any initialization
     * and cleanup for the interceptor chain.  For example, this interceptor is responsible for handling undeclared checked exceptions.
     *
     * @return the interceptor
     */
    public static Interceptor getInitialInterceptor() {
        return InitialInterceptor.INSTANCE;
    }

    /**
     * Get the interceptor factory for the initial interceptor.
     *
     * @return the factory
     */
    public static InterceptorFactory getInitialInterceptorFactory() {
        return InitialInterceptor.FACTORY;
    }

    /**
     * Get an interceptor which always returns {@code null}.
     *
     * @return the interceptor
     */
    public static Interceptor getTerminalInterceptor() {
        return TerminalInterceptor.INSTANCE;
    }

    /**
     * Get a factory which returns the terminal interceptor.
     *
     * @return the factory
     */
    public static InterceptorFactory getTerminalInterceptorFactory() {
        return TerminalInterceptor.FACTORY;
    }

    /**
     * Get an invoking interceptor which always terminates.  If the invoked method is {@code null}, this interceptor returns
     * {@code null}, making it suitable for terminating lifecycle interceptor chains as well as invocation
     * interceptor chains.
     *
     * @return the interceptor
     */
    public static Interceptor getInvokingInterceptor() {
        return InvokingInterceptor.INSTANCE;
    }

    /**
     * Get a factory which returns the invoking interceptor.
     *
     * @return the factory
     */
    public static InterceptorFactory getInvokingInterceptorFactory() {
        return InvokingInterceptor.FACTORY;
    }

    /**
     * Get a chained interceptor which passes the invocation through the given interceptors.
     *
     * @param instances the interceptors to pass through
     * @return the chained interceptor
     */
    public static Interceptor getChainedInterceptor(Interceptor... instances) {
        return instances.length == 1 ? instances[0] : new ChainedInterceptor(instances);
    }

    /**
     * Get a chained interceptor which passes the invocation through the given interceptors.
     *
     * @param instances the interceptors to pass through
     * @return the chained interceptor
     */
    public static Interceptor getChainedInterceptor(Collection<Interceptor> instances) {
        final int size = instances.size();
        return size == 1 ? instances.iterator().next() : new ChainedInterceptor(instances.toArray(new Interceptor[size]));
    }

    /**
     * Get a chained interceptor factory which builds a chained interceptor using the given factories.
     *
     * @param instances the interceptor factories to use
     * @return the chained interceptor factory
     */
    public static InterceptorFactory getChainedInterceptorFactory(InterceptorFactory... instances) {
        return instances.length == 1 ? instances[0] : new ChainedInterceptorFactory(instances);
    }

    /**
     * Get a chained interceptor which passes the invocation through the given interceptors.
     *
     * @param instances the interceptors to pass through
     * @return the chained interceptor
     */
    public static InterceptorFactory getChainedInterceptorFactory(Collection<InterceptorFactory> instances) {
        final int size = instances.size();
        return size == 1 ? instances.iterator().next() : new ChainedInterceptorFactory(instances.toArray(new InterceptorFactory[size]));
    }

    public static Interceptor getWeavedInterceptor(final Interceptor... interceptors) {
        return new WeavedInterceptor(interceptors);
    }

    /**
     * Convenience method to get a {@link Throwable} as an {@link Exception}.
     *
     * @param throwable the throwable
     * @return the exception to throw
     * @throws Error if the throwable is an error type
     */
    public static Exception rethrow(Throwable throwable) throws Error {
        try {
            throw throwable;
        } catch (Error error) {
            throw error;
        } catch (Exception exception) {
            return exception;
        } catch (Throwable throwable2) {
            return msg.undeclaredThrowable(throwable2);
        }
    }
}
