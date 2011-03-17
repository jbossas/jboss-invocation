/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import junit.framework.Assert;
import org.jboss.invocation.InterceptorContext;
import org.junit.Test;

import javax.interceptor.InvocationContext;
import java.util.Map;

/**
 * Tests the return value of {@link javax.interceptor.InvocationContext#getContextData()}
 * 
 * @author Jaikiran Pai
 */
public class ContextDataTestCase {

    /**
     * The {@link javax.interceptor.InvocationContext#getContextData()} is expected to return a empty map when there is
     * no context data associated with the {@link InvocationContext}. This test makes sure that it works as expected.
     */
    @Test
    public void testContextData() {
        // create a InterceptorContext
        InterceptorContext interceptorContext = new InterceptorContext();
        // get the javax.interceptor.InvocationContext from the InterceptorContext
        InvocationContext javaxInvocationContext = interceptorContext.getInvocationContext();
        // fetch the context data
        Map<String, Object> contextData = javaxInvocationContext.getContextData();
        // test it!
        Assert.assertNotNull(InvocationContext.class.getSimpleName() + ".getContextData() is expected to return a non-null map", contextData);
        Assert.assertTrue(InvocationContext.class.getSimpleName() + ".getContextData() should have returned an empty map", contextData.isEmpty());
    }
}
