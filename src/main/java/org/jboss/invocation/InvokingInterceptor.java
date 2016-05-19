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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.jboss.invocation.InvocationMessages.msg;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class InvokingInterceptor implements Interceptor, Serializable {

    static final Interceptor INSTANCE = new InvokingInterceptor();
    static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    private static final long serialVersionUID = 175221411434392097L;

    public Object processInvocation(final InterceptorContext context) throws Exception {
        final Method method = context.getMethod();
        if (method == null) {
            return null;
        }
        try {
            Object target = context.getTarget();
            if (target == null) {
                throw msg.nullProperty("target", context);
            }
            return method.invoke(target, context.getParameters());
        } catch (IllegalAccessException e) {
            final IllegalAccessError n = new IllegalAccessError(e.getMessage());
            n.setStackTrace(e.getStackTrace());
            throw n;
        } catch (InvocationTargetException e) {
            throw Interceptors.rethrow(e.getCause());
        }
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public String toString() {
        return "invoking interceptor";
    }
}
