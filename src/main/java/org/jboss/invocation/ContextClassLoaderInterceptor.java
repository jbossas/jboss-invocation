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

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * An interceptor which sets the thread context class loader for the duration of an invocation.
 * <p/>
 * Note that this interceptor is only serializable if the given class loader is serializable.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ContextClassLoaderInterceptor implements Interceptor, Serializable {

    private static final long serialVersionUID = 3727922476337147374L;

    private final ClassLoader classLoader;

    /**
     * Construct a new instance.
     *
     * @param classLoader the class loader to use
     */
    public ContextClassLoaderInterceptor(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * {@inheritDoc}
     */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        final ClassLoader old;
        Thread thread = Thread.currentThread();
        if (System.getSecurityManager() == null) {
            old = thread.getContextClassLoader();
            thread.setContextClassLoader(classLoader);
        } else {
            old = AccessController.doPrivileged(new SetContextClassLoader(classLoader));
        }
        try {
            return context.proceed();
        } finally {
            if (System.getSecurityManager() == null) {
                thread.setContextClassLoader(old);
            } else {
                AccessController.doPrivileged(new SetContextClassLoader(old));
            }
        }
    }


    private static class SetContextClassLoader implements PrivilegedAction<ClassLoader> {

        private final ClassLoader classLoader;

        private SetContextClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public ClassLoader run() {
            Thread thread = Thread.currentThread();
            ClassLoader old = thread.getContextClassLoader();
            thread.setContextClassLoader(classLoader);
            return old;
        }
    }
}
