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
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
class WeavedInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = -2015905619563503718L;

    private final List<Interceptor> interceptors;

    WeavedInterceptor(final Interceptor... interceptors) {
        this.interceptors = Arrays.asList(interceptors);
    }

    @Override
    public Object processInvocation(InterceptorContext context) throws Exception {
        final int oldNext = context.getNextInterceptorIndex();
        final List<Interceptor> old = context.getInterceptors();
        final List<Interceptor> interceptors = new ArrayList<Interceptor>(this.interceptors);
        interceptors.addAll(old.subList(oldNext, old.size()));
        context.setInterceptors(interceptors);
        try {
            return context.proceed();
        }
        finally {
            context.setInterceptors(old, oldNext);
        }
    }
}
