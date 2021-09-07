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

import junit.framework.Assert;
import org.jboss.invocation.proxy.ProxyConfiguration;
import org.jboss.invocation.proxy.ProxyFactory;
import org.junit.Test;

import java.lang.reflect.Method;

public class BridgeMethodTest {

    @Test
    public void testBridgeMethods() throws InstantiationException, IllegalAccessException {
        final ProxyConfiguration<BridgeMethodChild> proxyConfiguration = new ProxyConfiguration<BridgeMethodChild>()
                .setSuperClass(BridgeMethodChild.class)
                .setProxyName(getClass().getPackage(),"BridgeMethodChildProxy")
                .setClassLoader(BridgeMethodChild.class.getClassLoader());
        ProxyFactory<BridgeMethodChild> proxyFactory = new ProxyFactory<BridgeMethodChild>(proxyConfiguration);
        BridgeMethodChild instance = proxyFactory.newInstance(new BridgeMethodInvocationHandler());
        Method result = instance.getResult();
        Assert.assertEquals(Method.class, result.getReturnType());
        Assert.assertFalse(result.isBridge());

    }

    public void testParent(BridgeMethodParent parent) {
        Method result = (Method) parent.getResult();
        Assert.assertEquals(Object.class, result.getReturnType());
        Assert.assertTrue(result.isBridge());
    }

    public void testParentMethodProxied() throws IllegalAccessException, InstantiationException {
        final ProxyConfiguration<BridgeMethodChild> proxyConfiguration = new ProxyConfiguration<BridgeMethodChild>()
                .setSuperClass(BridgeMethodChild.class)
                .setProxyName(getClass().getPackage(),"BridgeMethodChildProxy2")
                .setClassLoader(BridgeMethodChild.class.getClassLoader());
        ProxyFactory<BridgeMethodChild> proxyFactory = new ProxyFactory<BridgeMethodChild>(proxyConfiguration);
        BridgeMethodChild instance = proxyFactory.newInstance(new BridgeMethodInvocationHandler());
        Assert.assertEquals(20, instance.proxyMethod());
    }

}
