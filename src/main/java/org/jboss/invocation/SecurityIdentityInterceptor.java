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

import java.security.PrivilegedActionException;

import org.wildfly.security.auth.server.SecurityIdentity;

/**
 * An interceptor which executes under the invocation's {@link SecurityIdentity}.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class SecurityIdentityInterceptor implements Interceptor {
    private static final SecurityIdentityInterceptor INSTANCE = new SecurityIdentityInterceptor();
    private static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    private SecurityIdentityInterceptor() {
    }

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance
     */
    public static SecurityIdentityInterceptor getInstance() {
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

    /** {@inheritDoc} */
    public Object processInvocation(final InterceptorContext context) throws Exception {
        final SecurityIdentity identity = context.getPrivateData(SecurityIdentity.class);
        if (identity != null) try {
            return identity.runAs(context);
        } catch (PrivilegedActionException e) {
            throw e.getException();
        } else {
            return context.proceed();
        }
    }
}
