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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import static org.jboss.invocation.InvocationMessages.msg;

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
    private Object[] parameters;
    private final Map<String, Object> contextData = new HashMap<String, Object>();
    private final Object timer;

    /**
     * Construct a new instance.
     *
     * @param target the target object instance
     * @param method the invocation method (may be {@code null})
     * @param parameters the invocation parameters (may be {@code null})
     * @param timer the associated timer (may be {@code null})
     */
    public SimpleInvocationContext(final Object target, final Method method, final Object[] parameters, final Object timer) {
        this.target = target;
        this.method = method;
        this.parameters = parameters == null ? NO_OBJECTS : parameters;
        this.timer = timer;
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
        this.parameters = parameters;
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
     * being performed.  This method should be overridden to provide a specific implementation.
     *
     * @return nothing
     * @throws Exception always (in particular, {@link IllegalStateException})
     */
    public Object proceed() throws Exception {
        throw msg.cannotProceed();
    }
}
