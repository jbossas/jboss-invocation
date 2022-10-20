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

import java.lang.reflect.UndeclaredThrowableException;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.invocation.Interceptors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author John Bailey
 */
public class InitialInterceptorTestCase {

    private class ExceptionalInterceptor implements Interceptor {
        public Object processInvocation(InterceptorContext context) throws Exception {
            throw new Exception("Ahhh");
        }
    }

    private class EvenMoreExceptionalInterceptor implements Interceptor {
        public Object processInvocation(InterceptorContext context) throws Exception {
            throw new ChildException();
        }
    }

    @Test
    public void testSuccess() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("test"));
        context.setTarget(this);
        assertEquals("test", interceptor.processInvocation(context));
    }

    @Test
    public void testException() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), new ExceptionalInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("test"));
        context.setTarget(this);
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown UndeclaredThrowableException");
        } catch (UndeclaredThrowableException expected) {
        }
    }

    @Test
    public void testChildException() throws Exception {
        final Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getInitialInterceptor(), new EvenMoreExceptionalInterceptor(), Interceptors.getInvokingInterceptor());
        final InterceptorContext context = new InterceptorContext();
        context.setMethod(InitialInterceptorTestCase.class.getMethod("other"));
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown ChildException");
        } catch (ChildException expected) {
        }
    }

    public String test() {
        return "test";
    }

    public String other() throws ParentException {
        return "test";
    }


    public String throwsThrowable() throws Throwable {
        throw new Throwable();
    }

    private class ParentException extends Exception {
    }

    private class ChildException extends ParentException {
    }
}
