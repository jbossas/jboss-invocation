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

public class ConstructedGuardTest {

    @Test
    public void testConstructedGuard() throws InstantiationException, IllegalAccessException {
        final ProxyConfiguration<ConstructedGuardClass> proxyConfiguration = new ProxyConfiguration<ConstructedGuardClass>()
                .setSuperClass(ConstructedGuardClass.class)
                .setProxyName(getClass().getPackage(),"ConstructedGuardClassProxy")
                .setClassLoader(ConstructedGuardClass.class.getClassLoader());
        ProxyFactory<ConstructedGuardClass> proxyFactory = new ProxyFactory<ConstructedGuardClass>(proxyConfiguration);
        // if there is no guard we will get a NPE here
        // as the proxy attempts to delegate to a null method
        ConstructedGuardClass instance = proxyFactory.newInstance();
        Assert.assertEquals(1, instance.count);
    }
}
