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

import static org.junit.Assert.assertEquals;
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

        Interceptor interceptor = Interceptors.getChainedInterceptor(Interceptors.getNullInterceptor(), Interceptors.getNullInterceptor(), Interceptors.getInvokingInterceptor());
        try {
            interceptor.processInvocation(context);
            fail("Should have thrown an Exception");
        }
        catch(Exception e) {
            assertEquals("Hello world", e.getMessage());
        }
    }

    public void throwException() throws Exception {
        throw new Exception("Hello world");
    }
}
