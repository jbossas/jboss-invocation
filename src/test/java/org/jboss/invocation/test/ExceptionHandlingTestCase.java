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

import org.jboss.invocation.InterceptorContext;
import org.jboss.invocation.MethodInvokingInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class ExceptionHandlingTestCase {
    public static class Wizard {
        public void cast(Throwable spell) throws Throwable {
            throw spell;
        }
    }

    @Test
    public void testError() throws Exception {
        final Wizard target = new Wizard();
        final Method method = Wizard.class.getMethod("cast", Throwable.class);
        final MethodInvokingInterceptor interceptor = new MethodInvokingInterceptor(target, method);
        final InterceptorContext context = new InterceptorContext();
        context.setParameters(new Object[] { new Error("BOOM!") });
        try {
            interceptor.processInvocation(context);
            fail("Expected Error BOOM!");
        }
        catch (Error e) {
            assertEquals("BOOM!", e.getMessage());
        }
    }

    @Test
    public void testException() throws Exception {
        final Wizard target = new Wizard();
        final Method method = Wizard.class.getMethod("cast", Throwable.class);
        final MethodInvokingInterceptor interceptor = new MethodInvokingInterceptor(target, method);
        final InterceptorContext context = new InterceptorContext();
        context.setParameters(new Object[] { new Exception("BOOM!") });
        try {
            interceptor.processInvocation(context);
            fail("Expected Error BOOM!");
        }
        catch (Exception e) {
            assertEquals("BOOM!", e.getMessage());
        }
    }
}
