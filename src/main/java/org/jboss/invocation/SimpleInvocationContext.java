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

import static org.jboss.invocation.InvocationMessages.msg;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * A base class for invocation contexts.  Also can act as the last interceptor in a chain which
 * does not proceed.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class SimpleInvocationContext implements InvocationContext {

    private static final Object[] NO_OBJECTS = new Object[0];

    private final Object target;
    private final Method method;
    private final Constructor<?> constructor;
    private Object[] parameters;
    private final Map<String, Object> contextData;
    private final Object timer;

    /**
     * Construct a new instance.
     *
     * @param target the target object instance
     * @param method the invocation method (may be {@code null})
     * @param parameters the invocation parameters (may be {@code null})
     * @param contextData the context data map to use
     * @param timer the associated timer (may be {@code null})
     */
    public SimpleInvocationContext(final Object target, final Method method, final Object[] parameters, final Map<String, Object> contextData, final Object timer, Constructor<?> constructor) {
        this.target = target;
        this.method = method;
        this.parameters = parameters;
        this.contextData = contextData;
        this.timer = timer;
        this.constructor = constructor;
    }

    /**
     * Construct a new instance.
     *
     * @param target the target object instance
     * @param method the invocation method (may be {@code null})
     * @param parameters the invocation parameters (may be {@code null})
     * @param timer the associated timer (may be {@code null})
     */
    public SimpleInvocationContext(final Object target, final Method method, final Object[] parameters, final Object timer) {
        this(target, method, parameters, new HashMap<String, Object>(), timer, null);
    }

    /**
     * Construct a new instance.
     *
     * @param target the target object instance
     * @param method the invocation method (may be {@code null})
     * @param parameters the invocation parameters (may be {@code null})
     */
    public SimpleInvocationContext(final Object target, final Method method, final Object[] parameters) {
        this(target, method, parameters, null);
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return target;
    }

    /** {@inheritDoc} */
    public Method getMethod() {
        return method;
    }

    /** {@inheritDoc} */
    public Object[] getParameters() {
        return parameters;
    }

    /** {@inheritDoc} */
    public void setParameters(final Object[] parameters) {
        this.parameters = parameters == null ? NO_OBJECTS : parameters;
    }

    /** {@inheritDoc} */
    public Map<String, Object> getContextData() {
        return contextData;
    }

    /** {@inheritDoc} */
    public Object getTimer() {
        return timer;
    }

    /**
     * Throw an exception indicating that the end of the interceptor chain was reached without an invocation
     * being performed.  This method should be overridden to provide a specific implementation.  Though this method
     * always throws {@link CannotProceedException}, it is declared to throw {@code Exception} so that subclasses
     * can override this method in a spec-compliant way.
     *
     * @return nothing
     * @throws Exception always (in particular, {@link CannotProceedException})
     */
    public Object proceed() throws Exception {
        throw msg.cannotProceed();
    }

    /** {@inheritDoc} */
    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }
}
