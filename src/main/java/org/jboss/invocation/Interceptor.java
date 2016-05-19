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
     * chain.
     *
     * @param context the interceptor context
     * @return the result of the invocation
     * @throws Exception If the underlying invocation resulted in some exception
     */
    Object processInvocation(InterceptorContext context) throws Exception;
}
