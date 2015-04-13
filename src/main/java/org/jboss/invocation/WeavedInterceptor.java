/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Weaves a series of interceptors into an existing interceptor chain.
 *
 * This interceptor is not very memory efficient, and should be avoided
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Deprecated
class WeavedInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = -2015905619563503718L;

    private final Interceptor[] interceptors;

    WeavedInterceptor(final Interceptor... interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public Object processInvocation(InterceptorContext context) throws Exception {
        final int oldNext = context.getNextInterceptorIndex();
        final Interceptor[] old = context.getInterceptors();
        final Interceptor[] interceptors = new Interceptor[this.interceptors.length + old.length - oldNext];
        System.arraycopy(this.interceptors, 0, interceptors, 0, this.interceptors.length);
        System.arraycopy(old, oldNext, interceptors, this.interceptors.length, old.length - oldNext);
        context.setInterceptors(interceptors);
        try {
            return context.proceed();
        }
        finally {
            context.setInterceptors(old, oldNext);
        }
    }
}
