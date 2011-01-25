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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * An interceptor factory for interceptor objects which call a specific method via reflection.  If this factory is
 * called more than once in the same context, it will return the same interceptor instance.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInterceptorFactory implements InterceptorFactory {
    private final InterceptorInstanceFactory instanceFactory;
    private final Method interceptorMethod;

    /**
     * Construct a new instance.
     *
     * @param instanceFactory the instance factory for the interceptor instance
     * @param interceptorMethod the interceptor method
     */
    public MethodInterceptorFactory(final InterceptorInstanceFactory instanceFactory, final Method interceptorMethod) {
        this.instanceFactory = instanceFactory;
        this.interceptorMethod = interceptorMethod;
    }

    /** {@inheritDoc} */
    public Interceptor create(final InterceptorFactoryContext context) {
        final Map<Object,Object> map = context.getContextData();
        if (map.containsKey(this)) {
            return (Interceptor) map.get(this);
        } else {
            final MethodInterceptor interceptor = new MethodInterceptor(instanceFactory.createInstance(context), interceptorMethod);
            map.put(this, interceptor);
            return interceptor;
        }
    }
}
