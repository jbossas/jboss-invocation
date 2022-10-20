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
package org.jboss.invocation.proxy.test.proxyfactory.serialization;

import junit.framework.Assert;
import org.jboss.invocation.proxy.DefaultSerializableProxy;
import org.jboss.invocation.proxy.ProxyConfiguration;
import org.jboss.invocation.proxy.ProxyFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

public class SerializationTest {

    @Test
    public void simpleSerializationTest() throws InstantiationException, IllegalAccessException, IOException,
            ClassNotFoundException {
        final ProxyConfiguration<SerializableClass> proxyConfiguration = new ProxyConfiguration<SerializableClass>()
                .setSuperClass(SerializableClass.class)
                .setProxyName(getClass().getPackage(),"SerializableClassProxy")
                .setClassLoader(SerializableClass.class.getClassLoader());

        ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(proxyConfiguration);
        SerializableInvocationHandler handler = new SerializableInvocationHandler();
        SerializableClass proxy = proxyFactory.newInstance(handler);
        proxy.invoke(10);
        Assert.assertEquals(10, handler.getState());
        proxy.state = 100;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bytes);
        outputStream.writeObject(proxy);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SerializableClass deserializedProxy = (SerializableClass) inputStream.readObject();
        Assert.assertEquals(100, deserializedProxy.state);
        Assert.assertEquals(10,
                ((SerializableInvocationHandler) ProxyFactory.getInvocationHandlerStatic(deserializedProxy)).getState());
    }

    @Test
    public void defaultSerializableProxyTest() throws InstantiationException, IllegalAccessException, IOException,
            ClassNotFoundException {
        final ProxyConfiguration<SerializableClass> proxyConfiguration = new ProxyConfiguration<SerializableClass>()
                .setSuperClass(SerializableClass.class)
                .setProxyName(getClass().getPackage(),"SerializableClassProxy2")
                .setClassLoader(SerializableClass.class.getClassLoader());

        ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(proxyConfiguration);
        proxyFactory.setSerializableProxyClass(DefaultSerializableProxy.class);
        SerializableInvocationHandler dispatcher = new SerializableInvocationHandler();
        SerializableClass proxy = proxyFactory.newInstance(dispatcher);
        proxy.invoke(10);
        Assert.assertEquals(10, dispatcher.getState());
        proxy.state = 100;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bytes);
        outputStream.writeObject(proxy);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SerializableClass deserializedProxy = (SerializableClass) inputStream.readObject();
        Assert.assertEquals(0, deserializedProxy.state);
        Assert.assertEquals(10,
                ((SerializableInvocationHandler) (ProxyFactory.getInvocationHandlerStatic(deserializedProxy))).getState());
    }

    @Test
    public void serializableProxyDifferentClassloadTest() throws InstantiationException, IllegalAccessException, IOException,
            ClassNotFoundException {

        ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
        };
        final ProxyConfiguration<SerializableClass> proxyConfiguration = new ProxyConfiguration<SerializableClass>()
                .setSuperClass(SerializableClass.class)
                .setProxyName("org.jboss.proxy.test.SomeProxy")
                .setClassLoader(classLoader);

        ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(proxyConfiguration);
        proxyFactory.setSerializableProxyClass(TestSerializableProxy.class);
        SerializableInvocationHandler dispatcher = new SerializableInvocationHandler();
        SerializableClass proxy = proxyFactory.newInstance(dispatcher);
        Class<?> proxyClass = proxyFactory.defineClass();
        proxy.invoke(10);
        Assert.assertEquals(10, dispatcher.getState());
        proxy.state = 100;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bytes);
        outputStream.writeObject(proxy);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SerializableClass deserializedProxy = (SerializableClass) inputStream.readObject();
        Assert.assertEquals(0, deserializedProxy.state);
        Assert.assertEquals(10,
                ((SerializableInvocationHandler) ProxyFactory.getInvocationHandlerStatic(deserializedProxy)).getState());
        Assert.assertNotSame(proxyFactory.defineClass(), deserializedProxy.getClass());
        Assert.assertEquals(deserializedProxy.getClass().getClassLoader(), getClass().getClassLoader());
    }

    public static class TestSerializableProxy extends DefaultSerializableProxy {
        @Override
        protected Class<?> getProxyClass() throws ClassNotFoundException {

            final ProxyConfiguration<SerializableClass> proxyConfiguration = new ProxyConfiguration<SerializableClass>()
                    .setSuperClass(SerializableClass.class)
                    .setProxyName("org.jboss.proxy.test.SomeProxy")
                    .setClassLoader(getClass().getClassLoader());

            ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(proxyConfiguration);
            return proxyFactory.defineClass();
        }

        @Override
        protected Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }
    }

}
