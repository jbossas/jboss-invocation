/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
public class ChainedInterceptorTestCase {
    public String echo(final String msg) {
        return "Echo " + msg;
    }

    @Test
    public void test1() throws Exception {
        Method method = ChainedInterceptorTestCase.class.getMethod("echo", String.class);
        InterceptorContext context = new InterceptorContext();
        context.setMethod(method);
        context.setTarget(this);
        context.setParameters(new Object[] { "test1" });

        Interceptor interceptor1 = Interceptors.getChainedInterceptor(createMyInterceptor("1"), createMyInterceptor("2"), Interceptors.getInvokingInterceptor());
        Interceptor interceptor2 = Interceptors.getChainedInterceptor(createMyInterceptor("3"), createMyInterceptor("4"), interceptor1);

        String result = (String) interceptor2.processInvocation(context);
        String expected = "3#4#1#2#Echo test1";
        assertEquals(expected, result);
    }
}
