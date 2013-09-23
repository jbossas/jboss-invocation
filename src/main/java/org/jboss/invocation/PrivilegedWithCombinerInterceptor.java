/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
import java.security.PrivilegedActionException;

/**
 * An interceptor which runs the invocation in a privileged access control context while preserving any domain
 * combiner that is set on the caller access control context.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PrivilegedWithCombinerInterceptor implements Interceptor {

    private static final PrivilegedWithCombinerInterceptor INSTANCE = new PrivilegedWithCombinerInterceptor();
    private static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    private static final RuntimePermission PERMISSION = new RuntimePermission("getPrivilegedWithCombinerInterceptor");

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance
     */
    public static PrivilegedWithCombinerInterceptor getInstance() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(PERMISSION);
        }
        return INSTANCE;
    }

    /**
     * Get a factory which returns the singleton instance.
     *
     * @return a factory which returns the singleton instance
     */
    public static InterceptorFactory getFactory() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(PERMISSION);
        }
        return FACTORY;
    }

    private PrivilegedWithCombinerInterceptor() {
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                return AccessController.doPrivilegedWithCombiner(context);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        } else {
            return context.run();
        }
    }
}
