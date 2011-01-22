/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

/**
 * Interceptor utility and factory methods.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Interceptors {

    private Interceptors() {
    }

    /**
     * Get a no-operation interceptor which always proceeds.
     *
     * @return the interceptor
     */
    public static Interceptor getNullInterceptor() {
        return NullInterceptor.INSTANCE;
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
}
