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

import org.wildfly.security.manager.WildFlySecurityManager;

import java.security.PrivilegedActionException;

/**
 * An interceptor which enables access checking for the duration of the invocation.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class AccessCheckingInterceptor implements Interceptor {
    private static final AccessCheckingInterceptor INSTANCE = new AccessCheckingInterceptor();
    private static final InterceptorFactory FACTORY = new ImmediateInterceptorFactory(INSTANCE);

    private AccessCheckingInterceptor() {
    }

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance
     */
    public static AccessCheckingInterceptor getInstance() {
        return INSTANCE;
    }

    /**
     * Get the singleton factory instance.
     *
     * @return the singleton factory instance
     */
    public static InterceptorFactory getFactory() {
        return FACTORY;
    }

    public Object processInvocation(final InterceptorContext context) throws Exception {
        try {
            return WildFlySecurityManager.doChecked(context);
        } catch (PrivilegedActionException e) {
            throw e.getException();
        }
    }
}
