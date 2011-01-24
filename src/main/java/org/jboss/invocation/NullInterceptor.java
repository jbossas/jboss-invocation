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

import java.io.Serializable;

import javax.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationLogger.log;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class NullInterceptor implements Interceptor, Serializable {

    static final Interceptor INSTANCE = new NullInterceptor();
    static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    private static final long serialVersionUID = -2792151547173027051L;

    public Object processInvocation(final InvocationContext context) throws InvocationException, IllegalArgumentException {
        try {
            return context.proceed();
        } catch (Exception e) {
            throw log.invocationException(e);
        }
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public String toString() {
        return "null interceptor";
    }
}
