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

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.Messages;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CancellationException;

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

    // id = 3
    // unused (reserved indefinitely)

    // id = 4
    // "Null value specified for serialized field %s"

    @Message(id = 5, value = "The given interceptor instance is of the wrong type")
    IllegalArgumentException wrongInterceptorType();

    // id = 6
    // unused (reserved indefinitely)

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

    @Message(id = 12, value = "Invocation cancelled")
    CancellationException invocationCancelled();

    @Message(id = 13, value = "Invocation already complete")
    IllegalStateException invocationAlreadyComplete();

    @Message(id = 14, value = "No asynchronous result supplier is set")
    IllegalStateException noAsynchronousResultSupplierSet();
}
