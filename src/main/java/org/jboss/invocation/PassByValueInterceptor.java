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
import java.lang.reflect.Method;
import org.jboss.marshalling.cloner.ClassCloner;
import org.jboss.marshalling.cloner.ClassLoaderClassCloner;
import org.jboss.marshalling.cloner.ClonerConfiguration;
import org.jboss.marshalling.cloner.ObjectCloner;
import org.jboss.marshalling.cloner.ObjectClonerFactory;
import org.jboss.marshalling.cloner.ObjectCloners;

import javax.interceptor.InvocationContext;

/**
 * An invocation processor which passes the invocation by value to a target class loader.  Invocations will be
 * cloned to the target class loader; replies will be cloned to the caller's thread context class loader.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PassByValueInterceptor implements Interceptor {

    private static final long serialVersionUID = 6414413097651380754L;
    private final ClassLoader targetClassLoader;
    private final ClassLoaderClassCloner classCloner;

    /**
     * Construct a new instance.
     *
     * @param loader the target class loader
     */
    public PassByValueInterceptor(final ClassLoader loader) {
        targetClassLoader = loader;
        classCloner = new ClassLoaderClassCloner(targetClassLoader);
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InvocationContext context) throws InvocationException, IllegalArgumentException {
        try {
            new DelegatingInvocationContext(context) {};
            final Method originalMethod = context.getMethod();
            final Class<?> declaringClass = originalMethod.getDeclaringClass();
            final Object[] parameters = context.getParameters();
            context.setParameters((Object[]) clone(parameters, classCloner));
            try {
                return clone(context.proceed(), new ClassLoaderClassCloner(declaringClass.getClassLoader()));
            } finally {
                context.setParameters(parameters);
            }
        } catch (Exception e) {
            throw new InvocationException("Pass-by-value failed", e);
        }
    }

    private Object clone(final Object original, final ClassCloner classCloner) throws ClassNotFoundException, IOException {
        final ObjectClonerFactory clonerFactory = ObjectCloners.getSerializingObjectClonerFactory();
        final ClonerConfiguration config = new ClonerConfiguration();
        config.setClassCloner(classCloner);
        final ObjectCloner cloner = clonerFactory.createCloner(config);
        return cloner.clone(original);
    }
}
