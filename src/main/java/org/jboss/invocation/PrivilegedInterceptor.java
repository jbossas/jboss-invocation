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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * An interceptor which runs the invocation in a privileged or privileged and restricted access control context.  If
 * an {@link AccessControlContext} is attached to the interceptor context, it is used; otherwise, the invocation proceeds
 * normally.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PrivilegedInterceptor implements Interceptor {

    private static final PrivilegedInterceptor INSTANCE = new PrivilegedInterceptor();
    private static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance
     */
    public static PrivilegedInterceptor getInstance() {
        return INSTANCE;
    }

    /**
     * Get a factory which returns the singleton instance.
     *
     * @return a factory which returns the singleton instance
     */
    public static InterceptorFactory getFactory() {
        return FACTORY;
    }

    private PrivilegedInterceptor() {
    }

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        try {
            return AccessController.doPrivileged(context, context.getPrivateData(AccessControlContext.class));
        } catch (PrivilegedActionException e) {
            throw e.getException();
        }
    }
}
