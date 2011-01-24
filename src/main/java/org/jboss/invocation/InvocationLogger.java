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

import org.jboss.logging.Cause;

/**
 * Invocation message logger.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
// @MessageLogger(projectCode = "INV")
interface InvocationLogger {

//    InvocationLogger log = Logger.getMessageLogger(InvocationLogger.class, "org.jboss.invocation");
    InvocationLogger log = new InvocationLogger() {
        public InvocationException invocationException(@Cause final Throwable cause) {
            return new InvocationException("Invocation failed", cause);
        }

        public IllegalStateException cannotProceed() {
            return new IllegalStateException("Invocation cannot proceed (end of interceptor chain has been hit)");
        }
    };

    // @Message(id = 1, value = "Invocation failed")
    InvocationException invocationException(@Cause Throwable cause);

    // @Message(id = 2, value = "Invocation cannot proceed (end of interceptor chain has been hit)")
    IllegalStateException cannotProceed();
}
