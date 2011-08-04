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
package org.jboss.invocation.proxy.test.proxyfactory;

import junit.framework.Assert;
import org.jboss.invocation.proxy.ProxyFactory;
import org.junit.Test;

import java.lang.reflect.Method;

public class SimpleProxyFactoryTest {

    @Test
    public void testSimpleProxy() throws InstantiationException, IllegalAccessException {
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(SimpleClass.class);
        SimpleClass instance = proxyFactory.newInstance(new SimpleInvocationHandler());
        Object result = instance.method2(10, 0, this, new int[0]);
        Assert.assertTrue(result.getClass().isArray());
        Object[] array = (Object[]) result;
        Assert.assertEquals(10L, array[0]);
        Assert.assertEquals(0.0, array[1]);

    }

    @Test
    public void testMultipleClassLoaders() throws InstantiationException, IllegalAccessException {

        ClassLoader cl1 = new ClassLoader() {

        };
        ClassLoader cl2 = new ClassLoader() {

        };
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(
                "org.jboss.invocation.proxy.test.proxyfactory.SimpleClass$$Proxy", SimpleClass.class, cl1);
        SimpleClass instance = proxyFactory.newInstance(new SimpleInvocationHandler());
    }

    @Test
    public void testRetrievingCachedMethods() {
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(SimpleClass.class);
        Method[] methods = proxyFactory.getCachedMethods();
        Assert.assertEquals(5, methods.length);
        Method method1 = null;
        for (Method m : methods) {
            if (m.getName().equals("method1")) {
                method1 = m;
                break;
            }
        }
        Assert.assertNotNull(method1);
        Assert.assertEquals(SimpleClass.class, method1.getDeclaringClass());

    }

}
