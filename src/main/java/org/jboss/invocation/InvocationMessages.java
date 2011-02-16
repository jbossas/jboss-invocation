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
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

import java.io.InvalidObjectException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Invocation message logger.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@MessageBundle(projectCode = "INV")
interface InvocationMessages {

    InvocationMessages msg = Messages.getBundle(InvocationMessages.class);

    @Message(id = 1, value = "Undeclared Throwable thrown")
    UndeclaredThrowableException undeclaredThrowable(@Cause Throwable cause);

    @Message(id = 2, value = "Invocation cannot proceed (end of interceptor chain has been hit)")
    CannotProceedException cannotProceed();

    @Message(id = 3, value = "Null value passed in for parameter %s")
    IllegalArgumentException nullParameter(String param);

    @Message(id = 4, value = "Null value specified for serialized field %s")
    InvalidObjectException nullField(String param);

    @Message(id = 5, value = "The given interceptor instance is of the wrong type")
    IllegalArgumentException wrongInterceptorType();

    @Message(id = 6, value = "Target method must not be static")
    IllegalArgumentException targetIsStatic();

    @Message(id = 7, value = "Method interceptor for inaccessible method")
    SecurityException interceptorInaccessible();

    @Message(id = 8, value = "Target method must accept a single parameter")
    IllegalArgumentException interceptorTargetOneParam();

    @Message(id = 9, value = "Target method's sole parameter must be assignable from %s")
    IllegalArgumentException interceptorTargetAssignableFrom(Class<?> type);

    @Message(id = 10, value = "Target method must return an object type")
    IllegalArgumentException interceptorReturnObject();

    @Message(id = 11, value = "Null value for property %s of %s")
    NullPointerException nullProperty(String propertyName, Object obj);
}
