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
