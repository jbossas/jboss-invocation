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
package org.jboss.invocation.proxy.test.proxyfactory;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;
import org.jboss.invocation.proxy.ProxyConfiguration;
import org.jboss.invocation.proxy.ProxyFactory;
import org.junit.Test;

public class SimpleProxyFactoryTest {

    @Test
    public void testSimpleProxy() throws InstantiationException, IllegalAccessException {
        final ProxyConfiguration<SimpleClass> proxyConfiguration = new ProxyConfiguration<SimpleClass>()
                .setSuperClass(SimpleClass.class)
                .setProxyName(SimpleClass.class.getPackage(), "SimpleClass$$Proxy3")
                .setClassLoader(SimpleClass.class.getClassLoader());
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(proxyConfiguration);
        SimpleClass instance = proxyFactory.newInstance(new SimpleInvocationHandler());
        Object result = instance.method2(10, 0, this, new int[0]);
        Assert.assertTrue(result.getClass().isArray());
        Object[] array = (Object[]) result;
        Assert.assertEquals(10L, array[0]);
        Assert.assertEquals(0.0, array[1]);

    }

    /**
     * The final methods in {@code SimpleClass2} should not be overridden in the generated proxy class.
     */
    @Test
    public void testSimpleProxy2() throws InstantiationException, IllegalAccessException {
        final ProxyConfiguration<SimpleClass2> proxyConfiguration = new ProxyConfiguration<SimpleClass2>()
                .setSuperClass(SimpleClass2.class)
                .setProxyName(SimpleClass2.class.getPackage(), "SimpleClass2$$Proxy3")
                .setClassLoader(SimpleClass2.class.getClassLoader());
        ProxyFactory<SimpleClass2> proxyFactory = new ProxyFactory<SimpleClass2>(proxyConfiguration);
        SimpleClass2 instance = proxyFactory.newInstance(new SimpleInvocationHandler());
        instance.method1();
    }

    @Test
    public void testMultipleClassLoaders() throws InstantiationException, IllegalAccessException {

        ClassLoader cl1 = new ClassLoader() {

        };
        ClassLoader cl2 = new ClassLoader() {

        };
        final ProxyConfiguration<SimpleClass> proxyConfiguration = new ProxyConfiguration<SimpleClass>()
                .setSuperClass(SimpleClass.class)
                .setProxyName(SimpleClass.class.getPackage(), "SimpleClass$$Proxy")
                .setClassLoader(cl1);
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(proxyConfiguration);
        SimpleClass instance = proxyFactory.newInstance(new SimpleInvocationHandler());
    }

    @Test
    public void testRetrievingCachedMethods() {
        final ProxyConfiguration<SimpleClass> proxyConfiguration = new ProxyConfiguration<SimpleClass>()
                .setSuperClass(SimpleClass.class)
                .setProxyName(SimpleClass.class.getPackage(), "SimpleClass$$Proxy2")
                .setClassLoader(SimpleClass.class.getClassLoader());
        ProxyFactory<SimpleClass> proxyFactory = new ProxyFactory<SimpleClass>(proxyConfiguration);
        List<Method> methods = proxyFactory.getCachedMethods();
        Assert.assertEquals(7, methods.size());
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
