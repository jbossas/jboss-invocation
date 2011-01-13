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

/**
 * An invocation processor which passes the invocation by value to a target class loader.  Invocations will be
 * cloned to the target class loader; replies will be cloned to the current thread context class loader.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PassByValueInvocationProcessor implements InvocationProcessor {
    private final ClassLoader targetClassLoader;

    /**
     * Construct a new instance.
     *
     * @param loader the target class loader
     */
    public PassByValueInvocationProcessor(final ClassLoader loader) {
        targetClassLoader = loader;
    }

    /** {@inheritDoc} */
    public InvocationReply processInvocation(final InvocationProcessorContext context, final Invocation invocation) throws InvocationException, IllegalArgumentException {
        try {
            final ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
            final InvocationReply reply = context.invokeNext(invocation.cloneTo(targetClassLoader));
            try {
                return ((InvocationReply) reply).cloneTo(originalLoader);
            } catch (Exception e) {
                throw new InvocationException("Cannot pass result by value", e);
            }
        } catch (Exception e) {
            throw new InvocationException("Pass-by-value failed", e);
        }
    }
}
