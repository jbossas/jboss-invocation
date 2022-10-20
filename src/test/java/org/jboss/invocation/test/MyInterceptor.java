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

package org.jboss.invocation.test;

import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public abstract class MyInterceptor implements Interceptor {
    protected abstract String getName();

    public static Interceptor createMyInterceptor(final String name) {
        return new MyInterceptor() {
            @Override
            protected String getName() {
                return name;
            }
        };
    }

    @Override
    public Object processInvocation(final InterceptorContext context) throws Exception {
        return getName() + "#" + context.proceed();
    }
}
