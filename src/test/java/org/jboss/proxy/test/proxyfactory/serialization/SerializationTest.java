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
package org.jboss.proxy.test.proxyfactory.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.jboss.proxy.DefaultSerializableProxy;
import org.jboss.proxy.ProxyFactory;
import org.jboss.proxy.ProxyInstance;
import org.junit.Test;

public class SerializationTest {

    @Test
    public void simpleSerializationTest() throws InstantiationException, IllegalAccessException, IOException,
            ClassNotFoundException {
        ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(SerializableClass.class);
        SerializableInvocationDispatcher dispatcher = new SerializableInvocationDispatcher();
        SerializableClass proxy = proxyFactory.newInstance(dispatcher);
        proxy.invoke(10);
        Assert.assertEquals(10, dispatcher.getState());
        proxy.state = 100;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bytes);
        outputStream.writeObject(proxy);

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        SerializableClass deserializedProxy = (SerializableClass) inputStream.readObject();
        Assert.assertEquals(100, deserializedProxy.state);
        Assert.assertEquals(10, ((SerializableInvocationDispatcher) ((ProxyInstance) deserializedProxy)
                ._getProxyInvocationDispatcher()).getState());
    }

    @Test
    public void defaultSerializableProxyTest() throws InstantiationException, IllegalAccessException, IOException,
            ClassNotFoundException {
        ProxyFactory<SerializableClass> proxyFactory = new ProxyFactory<SerializableClass>(SerializableClass.class);
        proxyFactory.setSerializableProxyClass(DefaultSerializableProxy.class);
        SerializableInvocationDispatcher dispatcher = new SerializableInvocationDispatcher();
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
        Assert.assertEquals(10, ((SerializableInvocationDispatcher) ((ProxyInstance) deserializedProxy)
                ._getProxyInvocationDispatcher()).getState());
    }

}
