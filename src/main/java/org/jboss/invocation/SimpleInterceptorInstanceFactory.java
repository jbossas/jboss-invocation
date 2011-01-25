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

import java.util.Map;

/**
 * Simple instance factory which just uses reflection to create an instance of the given class.  Only one
 * instance of the given class will be created per factory context.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class SimpleInterceptorInstanceFactory implements InterceptorInstanceFactory {
    private final Class<?> instanceClass;

    /**
     * Construct a new instance.
     *
     * @param instanceClass the instance class
     */
    public SimpleInterceptorInstanceFactory(final Class<?> instanceClass) {
        this.instanceClass = instanceClass;
    }

    /** {@inheritDoc} */
    public Object createInstance(final InterceptorFactoryContext context) {
        final Map<Object,Object> map = context.getContextData();
        final Class<?> instanceClass = this.instanceClass;
        if (map.containsKey(instanceClass)) {
            return map.get(instanceClass);
        }
        final Object instance;
        try {
            instance = instanceClass.newInstance();
        } catch (InstantiationException e) {
            final InstantiationError error = new InstantiationError(e.getMessage());
            error.setStackTrace(e.getStackTrace());
            throw error;
        } catch (IllegalAccessException e) {
            final IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.setStackTrace(e.getStackTrace());
            throw error;
        }
        map.put(instanceClass, instance);
        return instance;
    }
}
