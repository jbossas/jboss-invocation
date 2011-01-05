/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

/**
 * Represents some exceptional circumstance which has occurred while carrying out an invocation.  The underlying problem is
 * represented in the wrapped cause, which is required at construction.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @version $Revision: $
 */
public class InvocationException extends Exception {
    //-------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    //-------------------------------------------------------------------------------------||
    // Constructors -----------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * Constructs a new {@link InvocationException}
     *
     * @param message
     * @param cause
     *
     * @throws IllegalArgumentException If no cause has been specified
     */
    public InvocationException(final String message, final Throwable cause) {
        super(message, cause);
        // Assert invariants
        assertCauseSpecified();
    }

    /**
     * Constructs a new {@link InvocationException}
     *
     * @param cause
     *
     * @throws IllegalArgumentException If no cause has been specified
     */
    public InvocationException(final Throwable cause) {
        super(cause);
        // Assert invariants
        assertCauseSpecified();
    }
    //-------------------------------------------------------------------------------------||
    // Internal Helper Methods ------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * Ensures that the cause of this Exception has been specified; typically called from construction
     *
     * @throws IllegalArgumentException If no cause exists
     */
    private void assertCauseSpecified() {
        // Assert invariants
        if (getCause() == null) {
            throw new IllegalArgumentException("Cause must be specified");
        }
    }
}
