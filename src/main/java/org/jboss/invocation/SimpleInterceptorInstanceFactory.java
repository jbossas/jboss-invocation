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
