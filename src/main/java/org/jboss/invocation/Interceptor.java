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

import javax.interceptor.InvocationContext;

/**
 * A processor for invocations.  May perform some action, including but not limited to handling the invocation, before
 * or in lieu of passing it on to the dispatcher or another processor.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface Interceptor {

    /**
     * Process an invocation.  The invocation can be handled directly, or passed on to the next processor in the
     * chain via {@code context}.
     *
     * @param context the invocation context
     * @return the result of the invocation
     * @throws InvocationException If the underlying invocation resulted in some Exception; the original exception may be
     * obtained via {@link InvocationException#getCause()}
     * @throws IllegalArgumentException If the invocation or dispatcher is not specified (i.e. {@code null})
     */
    Object processInvocation(InvocationContext context) throws InvocationException, IllegalArgumentException;
}
