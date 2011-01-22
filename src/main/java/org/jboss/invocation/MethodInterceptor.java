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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.jboss.marshalling.FieldSetter;

import javax.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationLogger.log;

/**
 * A method interceptor.  The target method should be non-final, must be non-static, and must accept a single
 * parameter of type {@link InvocationContext} (or any supertype thereof).  The method must belong to the given
 * interceptor object's class or one of its supertypes.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MethodInterceptor implements Interceptor {

    private static final long serialVersionUID = 3951626966559917049L;

    private final Object interceptorInstance;
    private final MethodResolver methodResolver;
    private final transient Method interceptorMethod;

    private static final FieldSetter methodSetter = FieldSetter.get(MethodInterceptor.class, "method");

    /**
     * Construct a new instance.  The given method should be a proper interceptor method; otherwise invocation may fail.
     *
     * @param interceptorInstance the interceptor object instance
     * @param methodResolver the interceptor method resolver
     */
    public MethodInterceptor(final Object interceptorInstance, final MethodResolver methodResolver) {
        this.methodResolver = methodResolver;
        this.interceptorInstance = interceptorInstance;
        interceptorMethod = getMethod(methodResolver);
        if (! interceptorMethod.getDeclaringClass().isInstance(interceptorInstance)) {
            throw new IllegalArgumentException("The given interceptor instance is of the wrong type");
        }
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InvocationContext context) throws InvocationException, IllegalArgumentException {
        try {
            return interceptorMethod.invoke(interceptorInstance, context);
        } catch (IllegalAccessException e) {
            final IllegalAccessError n = new IllegalAccessError(e.getMessage());
            n.setStackTrace(e.getStackTrace());
            throw log.invocationException(n);
        } catch (InvocationTargetException e) {
            throw log.invocationException(e.getCause());
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        methodSetter.set(this, getMethod(methodResolver));
        if (! interceptorMethod.getDeclaringClass().isInstance(interceptorInstance)) {
            throw new IllegalArgumentException("The given interceptor instance is of the wrong type");
        }
    }

    private static Method getMethod(final MethodResolver methodResolver) {
        final Method interceptorMethod = methodResolver.getMethod();
        final int modifiers = interceptorMethod.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("Target method must not be static");
        }
        if (! Modifier.isPublic(modifiers) && ! interceptorMethod.isAccessible()) {
            throw new SecurityException("Method interceptor for inaccessible method");
        }
        final Class<?>[] parameterTypes = interceptorMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Target method must accept a single parameter");
        }
        // allow contravariant parameter types
        if (! parameterTypes[0].isAssignableFrom(InvocationContext.class)) {
            throw new IllegalArgumentException("Target method's sole parameter must be assignable from " + InvocationContext.class);
        }
        // allow covariant return types (but not primitives, which are not Objects); also allow void for lifecycle interceptors
        final Class<?> returnType = interceptorMethod.getReturnType();
        if (returnType != void.class && ! Object.class.isAssignableFrom(returnType)) {
            throw new IllegalArgumentException("Target method must have an Object return type");
        }
        return interceptorMethod;
    }
}
