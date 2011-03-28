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
