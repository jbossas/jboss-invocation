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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.jboss.invocation.Interceptors.rethrow;

/**
 * An interceptor which always invokes one specific method on one specific object given the parameters from
 * the invocation context.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInvokingInterceptor implements Interceptor {
    private final Object target;
    private final Method method;

    /**
     * Construct a new instance.
     *
     * @param target the object upon which invocation should take place
     * @param method the method to invoke
     */
    public MethodInvokingInterceptor(final Object target, final Method method) {
        this.target = target;
        this.method = method;
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        try {
            return method.invoke(target, (Object[]) context.getParameters());
        }
        catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        }
    }
}
