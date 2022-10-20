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

package org.jboss.invocation.proxy;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationHandler;

/**
 * Serialized representation of a proxy.
 * <p>
 * Provides a simple default serialized representation, that saves the {@link InvocationHandler} state and loads the proxy into
 * the Thread Context Class Loader.
 * <p>
 * This class should not be used if a security manager is present that prevents access to the Thread Context Class Loader.
 * 
 * @author Stuart Douglas
 * 
 */
public class DefaultSerializableProxy implements SerializableProxy {

    private static final long serialVersionUID = -1296036574026839239L;

    private InvocationHandler handler;
    private String proxyClassName;

    /** {@inheritDoc} */
    public void setProxyInstance(Object proxy) {
        proxyClassName = proxy.getClass().getName();
        handler = ProxyFactory.getInvocationHandlerStatic(proxy);
    }

    /**
     * Resolve the serialized proxy to a real instance.
     *
     * @return the resolved instance
     * @throws ObjectStreamException if an error occurs
     */
    protected Object readResolve() throws ObjectStreamException {
        try {
            Class<?> proxyClass = getProxyClass();
            Object instance = proxyClass.newInstance();
            ProxyFactory.setInvocationHandlerStatic(instance, handler);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the associated proxy class.
     *
     * @return the proxy class
     * @throws ClassNotFoundException if the proxy class is not found
     */
    protected Class<?> getProxyClass() throws ClassNotFoundException {
        ClassLoader classLoader = getProxyClassLoader();
        return Class.forName(proxyClassName, false, classLoader);
    }

    /**
     * Get the proxy class loader.
     *
     * @return the proxy class loader
     */
    protected ClassLoader getProxyClassLoader()  {
        return Thread.currentThread().getContextClassLoader();
    }
}
