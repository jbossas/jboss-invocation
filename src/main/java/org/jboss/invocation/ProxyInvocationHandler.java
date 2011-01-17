/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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

package org.jboss.invocation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A {@link Proxy} {@code InvocationHandler} which delegates invocations to an {@code InvocationDispatcher}.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ProxyInvocationHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -7550306900997519378L;

    private static final Method EQUALS;
    private static final Method HASH_CODE;
    private static final Method TO_STRING;

    static {
        Method equals = null;
        Method hashCode = null;
        Method toString = null;
        final Method[] methods = Object.class.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("equals")) {
                equals = method;
            } else if (method.getName().equals("hashCode")) {
                hashCode = method;
            } else if (method.getName().equals("toString")) {
                toString = method;
            }
        }
        assert equals != null;
        assert hashCode != null;
        assert toString != null;
        EQUALS = equals;
        HASH_CODE = hashCode;
        TO_STRING = toString;
    }

    /**
     * The invocation dispatcher which should handle the invocation.
     *
     * @serial
     */
    private final InvocationDispatcher dispatcher;

    /**
     * Construct a new instance.
     *
     * @param dispatcher the dispatcher to send invocations to
     */
    public ProxyInvocationHandler(final InvocationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Handle a proxy method invocation.
     *
     * @param proxy the proxy instance
     * @param method the invoked method
     * @param args the method arguments
     * @return the result of the method call
     * @throws Throwable the exception to thrown from the method invocation on the proxy instance, if any
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.equals(EQUALS)) {
            return Boolean.valueOf(proxy == args[0]);
        } else if (method.equals(HASH_CODE)) {
            return Integer.valueOf(System.identityHashCode(proxy));
        } else if (method.equals(TO_STRING)) {
            return "Proxy via " + dispatcher;
        }
        final MethodIdentifier id = MethodIdentifier.getIdentifierForMethod(method);
        try {
            InvocationReply reply = dispatcher.dispatch(new Invocation(method.getDeclaringClass(), id, (Object[]) args));
            return reply.getReply();
        } catch (InvocationException e) {
            throw e.getCause();
        }
    }

    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (! dispatcher.getClass().getProtectionDomain().implies(Invocation.INVOCATION_PERMISSION)) {
                throw new InvalidObjectException("Dispatcher does not have invoke permission");
            }
        }
    }
}
