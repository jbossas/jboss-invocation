/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, JBoss Inc., and individual contributors as indicated
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

import java.io.Serializable;

/**
 * An invocation bound for a specific remote dispatcher.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class RemoteInvocation implements Serializable {

    private static final long serialVersionUID = 8108865965008810909L;

    private final String dispatcherName;
    private final Invocation invocation;

    /**
     * Construct a new instance.
     *
     * @param dispatcherName the dispatcher identifier
     * @param invocation the invocation
     */
    public RemoteInvocation(final String dispatcherName, final Invocation invocation) {
        if (dispatcherName == null) {
            throw new IllegalArgumentException("dispatcherIdentifier is null");
        }
        if (invocation == null) {
            throw new IllegalArgumentException("invocation is null");
        }
        this.dispatcherName = dispatcherName;
        this.invocation = invocation;
    }

    /**
     * Get the dispatcher name.
     *
     * @return the dispatcher name
     */
    public String getDispatcherName() {
        return dispatcherName;
    }

    /**
     * Get the invocation.
     *
     * @return the invocation
     */
    public Invocation getInvocation() {
        return invocation;
    }
}
