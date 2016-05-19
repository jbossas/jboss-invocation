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

public class AnnotatedProxyFactoryTest {

    @Test
    public void testSimpleProxy() throws InstantiationException, IllegalAccessException, SecurityException,
            NoSuchMethodException {
        final ProxyConfiguration<AnnotatedClass> proxyConfiguration = new ProxyConfiguration<AnnotatedClass>()
                .setSuperClass(AnnotatedClass.class)
                .setProxyName(getClass().getPackage(),"AnnotatedClassProxy")
                .setClassLoader(AnnotatedClass.class.getClassLoader());
        ProxyFactory<AnnotatedClass> proxyFactory = new ProxyFactory<AnnotatedClass>(proxyConfiguration);
        Assert.assertTrue(proxyFactory.defineClass().isAnnotationPresent(MyAnnotation.class));
        Method method = proxyFactory.defineClass().getDeclaredMethod("method");
        Assert.assertTrue(method.isAnnotationPresent(MyAnnotation.class));
    }

}
