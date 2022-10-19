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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.interceptor.InvocationContext;

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
