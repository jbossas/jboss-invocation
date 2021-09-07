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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * This test is simply to get some coverage.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleInterceptorTestCase {
    /**
     * The original exception must not be wrapped, regardless of the amount of interceptors.
     */
    @Test
    public void test1() throws NoSuchMethodException {
        Method method = SimpleInterceptorTestCase.class.getMethod("throwException");
        InterceptorContext context = new InterceptorContext();
        context.setMethod(method);
        context.setTarget(this);

        Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInvokingInterceptor());
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown an Exception");
        }
        catch(Exception e) {
            assertEquals("Hello world", e.getMessage());
        }
    }

    @Test
    public void test2() throws Exception {
        Method method = SimpleInterceptorTestCase.class.getMethod("throwException");
        InterceptorContext context = new InterceptorContext();
        context.setMethod(method);
        context.setTarget(this);

        Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getTerminalInterceptor());
        assertNull(interceptor.processInvocation(context));
    }

    public void throwException() throws Exception {
        throw new Exception("Hello world");
    }
}
