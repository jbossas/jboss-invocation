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

import java.io.Serializable;

import javax.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationLogger.log;

/**
 * An interceptor which passes invocations through a series of nested interceptors.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class ChainedInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = 7951017996430287249L;

    private final Interceptor[] interceptors;

    /**
     * Construct a new instance.
     *
     * @param interceptors the child interceptors
     */
    ChainedInterceptor(final Interceptor... interceptors) {
        if (interceptors == null) {
            throw new IllegalArgumentException("interceptors is null");
        }
        this.interceptors = interceptors;
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InvocationContext context) throws InvocationException, IllegalArgumentException {
        final InvocationContext childContext = new DelegatingInvocationContext(context) {
            private int index = 0;

            public Object proceed() throws Exception {
                if (index < interceptors.length) {
                    try {
                        return interceptors[index++].processInvocation(this);
                    } finally {
                        index--;
                    }
                } else {
                    return super.proceed();
                }
            }
        };
        try {
            return childContext.proceed();
        } catch (Exception e) {
            throw log.invocationException(e);
        }
    }
}
