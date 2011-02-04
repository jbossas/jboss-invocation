/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import org.jboss.marshalling.cloner.ClassLoaderClassCloner;
import org.jboss.marshalling.cloner.ClonerConfiguration;
import org.jboss.marshalling.cloner.ObjectCloner;
import org.jboss.marshalling.cloner.ObjectClonerFactory;
import org.jboss.marshalling.cloner.ObjectCloners;

/**
 * An invocation processor which passes the invocation (possibly by value) to a target class loader.  Invocations will be
 * cloned to the target class loader; replies will be cloned to the caller's thread context class loader.  The target
 * interceptor is responsible for security concerns such as authorization as well as setting up environmental context
 * such as transaction propagation, naming context, context class loader, etc.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class InVMRemoteInterceptor implements Interceptor {

    private static final PrivilegedAction<ClassLoader> GET_CLASS_LOADER_ACTION = new PrivilegedAction<ClassLoader>() {
        public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
        }
    };

    private final ClonerConfiguration configuration;

    /**
     * Construct a new instance.
     *
     * @param targetClassLoader the target class loader
     *
     */
    public InVMRemoteInterceptor(final ClassLoader targetClassLoader) {
        configuration = new ClonerConfiguration();
        configuration.setClassCloner(new ClassLoaderClassCloner(targetClassLoader));
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        final Object[] parameters = context.getParameters();
        final ObjectClonerFactory clonerFactory = ObjectCloners.getSerializingObjectClonerFactory();
        final ObjectCloner cloner = clonerFactory.createCloner(configuration);
        final Object[] newParameters;
        final int len = parameters.length;
        newParameters = new Object[len];
        for (int i = 0; i < len; i++) {
             newParameters[i] = cloner.clone(parameters[i]);
        }
        context.setParameters(newParameters);
        try {
            final Object result = context.proceed();
            if (result == null) {
                return null;
            }
            final ClonerConfiguration copyBackConfiguration = new ClonerConfiguration();
            copyBackConfiguration.setClassCloner(new ClassLoaderClassCloner(getContextClassLoader()));
            return clonerFactory.createCloner(copyBackConfiguration).clone(result);
        } finally {
            context.setParameters(parameters);
        }
    }

    private ClassLoader getContextClassLoader() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController.doPrivileged(GET_CLASS_LOADER_ACTION);
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
