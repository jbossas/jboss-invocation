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
import org.jboss.invocation.Interceptors;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.jboss.invocation.test.MyInterceptor.createMyInterceptor;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class WeavedInterceptorTestCase {
    public String echo(final String msg) {
        return "Echo " + msg;
    }

    @Test
    public void test1() throws Exception {
        Method method = WeavedInterceptorTestCase.class.getMethod("echo", String.class);
        InterceptorContext context = new InterceptorContext();
        context.setMethod(method);
        context.setTarget(this);
        context.setParameters(new Object[] { "test1" });

        Interceptor interceptor2 = Interceptors.getWeavedInterceptor(createMyInterceptor("3"), createMyInterceptor("4"));
        Interceptor interceptor1 = Interceptors.getChainedInterceptor(interceptor2, createMyInterceptor("1"), createMyInterceptor("2"), Interceptors.getInvokingInterceptor());

        String result = (String) interceptor1.processInvocation(context);
        String expected = "3#4#1#2#Echo test1";
        assertEquals(expected, result);
    }
}
