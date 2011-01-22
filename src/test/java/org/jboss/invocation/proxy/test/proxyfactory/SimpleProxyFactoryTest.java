/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.proxy.test.proxyfactory;

import junit.framework.Assert;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.proxy.ProxyFactory;
import org.jboss.invocation.proxy.ProxyInstance;
import org.junit.Test;

public class SimpleProxyFactoryTest {

    @Test
    public void testSimpleProxy() throws InstantiationException, IllegalAccessException {
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(SimpleClass.class);
        SimpleClass instance = proxyFactory.newInstance(new SimpleDispatcher());
        ((ProxyInstance) instance)._setProxyInvocationDispatcher(new SimpleDispatcher());
        Invocation invocation = instance.method1();
        Assert.assertEquals("method1", invocation.getMethodIdentifier().getName());
        Assert.assertEquals(0, invocation.getMethodIdentifier().getParameterTypes().length);
        Assert.assertEquals(SimpleClass.class, SimpleDispatcher.declaringClass);
    }

}
