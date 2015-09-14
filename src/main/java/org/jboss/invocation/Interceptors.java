/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
