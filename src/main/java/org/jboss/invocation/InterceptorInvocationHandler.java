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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A {@link Proxy} {@code InvocationHandler} which delegates invocations to an {@code Interceptor}.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class InterceptorInvocationHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -7550306900997519378L;

    /**
     * The invocation dispatcher which should handle the invocation.
     *
     * @serial
     */
    private final Interceptor interceptor;

    /**
     * Construct a new instance.
     *
     * @param interceptor the interceptor to send invocations through
     */
    public InterceptorInvocationHandler(final Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Handle a proxy method invocation.
     *
     * @param proxy the proxy instance
     * @param method the invoked method
     * @param args the method arguments
     * @return the result of the method call
     * @throws Throwable the exception to thrown from the method invocation on the proxy instance, if any
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        InterceptorContext context = new InterceptorContext();
        context.setParameters(args);
        context.setMethod(method);
        return interceptor.processInvocation(context);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "interceptor invocation handler";
    }
}
