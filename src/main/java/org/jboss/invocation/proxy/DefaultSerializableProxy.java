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

package org.jboss.invocation.proxy;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationHandler;
import java.security.PrivilegedAction;

/**
 * Serialized representation of a proxy.
 * 
 * @author Stuart Douglas
 * 
 */
public class DefaultSerializableProxy implements SerializableProxy {

    private InvocationHandler handler;
    private String proxyClassName;

    public void setProxyInstance(Object proxy) {
        this.proxyClassName = proxy.getClass().getName();
        this.handler = ProxyFactory.getInvocationHandlerStatic(proxy);
    }

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

    protected Class<?> getProxyClass() throws ClassNotFoundException {
        ClassLoader classLoader = getProxyClassLoader();
        return Class.forName(proxyClassName, false, classLoader);
    }

    protected ClassLoader getProxyClassLoader()  {
        return new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        }.run();
    }

}
