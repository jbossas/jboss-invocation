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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.wildfly.common.Assert;

/**
 * An invocation context which simply delegates to another invocation context.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class DelegatingInvocationContext implements InvocationContext {
    private final InvocationContext delegate;

    /**
     * Construct a new instance.
     *
     * @param delegate the delegate context
     */
    public DelegatingInvocationContext(final InvocationContext delegate) {
        Assert.checkNotNullParam("delegate", delegate);
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return delegate.getTarget();
    }

    /** {@inheritDoc} */
    public Method getMethod() {
        return delegate.getMethod();
    }

    /** {@inheritDoc} */
    public Object[] getParameters() {
        return delegate.getParameters();
    }

    /** {@inheritDoc} */
    public void setParameters(final Object[] params) {
        delegate.setParameters(params);
    }

    /** {@inheritDoc} */
    public Map<String, Object> getContextData() {
        return delegate.getContextData();
    }

    /** {@inheritDoc} */
    public Object getTimer() {
        return delegate.getTimer();
    }

    /** {@inheritDoc} */
    public Object proceed() throws Exception {
        return delegate.proceed();
    }

    /** {@inheritDoc} */
    @Override
    public Constructor<?> getConstructor() {
        return delegate.getConstructor();
    }
}
