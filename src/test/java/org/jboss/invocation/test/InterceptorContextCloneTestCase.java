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

package org.jboss.invocation.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.invocation.Interceptors;
import static org.jboss.invocation.test.MyInterceptor.createMyInterceptor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test to verify context cloning.
 *
 * @author John Bailey
 */
public class InterceptorContextCloneTestCase {

    @Test
    public void test1() throws Exception {
        Method method = InterceptorContextCloneTestCase.class.getMethod("test", String.class);
        final InterceptorContext context = new InterceptorContext();
        context.setTarget(this);
        context.setMethod(method);
        context.setParameters(new Object[] { "test" });
        context.setContextData(new HashMap<String, Object>());
        context.putPrivateData(InterceptorContextCloneTestCase.class, this);
        context.getContextData().put("test", "value");

        final Interceptor interceptor1 = createMyInterceptor("1");
        final Interceptor interceptor2 = createMyInterceptor("2");
        final Interceptor interceptor3 = createMyInterceptor("3");
        final Interceptor invoking = Interceptors.getInvokingInterceptor();
        final DeferredInterceptor deferred = new DeferredInterceptor();


        context.setInterceptors(Arrays.asList(interceptor1, interceptor2, deferred, interceptor3, invoking));

        String result = (String)context.proceed();
        assertEquals("1#2#Deferred#", result);

        final InterceptorContext cloned = deferred.cloned;
        assertEquals(method, cloned.getMethod());
        assertEquals(this, cloned.getTarget());
        assertEquals(this, cloned.getPrivateData(InterceptorContextCloneTestCase.class));
        assertEquals("value", cloned.getContextData().get("test"));
        assertArrayEquals(new Object[]{"test"}, context.getParameters());

        result = (String)cloned.proceed();
        assertEquals("3#test", result);
    }

    public String test(final String input) {
        return input;
    }

    private class DeferredInterceptor implements Interceptor {
        private InterceptorContext cloned;
        public Object processInvocation(InterceptorContext context) throws Exception {
            cloned = context.clone();
            return "Deferred#";
        }
    }
}
