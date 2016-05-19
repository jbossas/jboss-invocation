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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An asynchronous interceptor/invocation context object.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class AsynchronousInterceptorContext extends AbstractInterceptorContext implements Cloneable {
    private static final AsynchronousInterceptor[] EMPTY = new AsynchronousInterceptor[0];

    private AsynchronousInterceptor[] interceptors = EMPTY;
    private int interceptorPosition = 0;
    private AsynchronousInterceptor.ResultSupplier resultSupplier;
    private final Consumer<AsynchronousInterceptorContext> consumer;

    /**
     * Construct a new instance.  The given consumer is called when the invocation is complete.  The {@link #getResult()}
     * method may be called to read the invocation result.
     *
     * @param consumer the result consumer (must not be {@code null})
     */
    public AsynchronousInterceptorContext(final Consumer<AsynchronousInterceptorContext> consumer) {
        this.consumer = consumer;
    }

    public Object getTarget() {
        return super.getTarget();
    }

    public void setTarget(final Object target) {
        super.setTarget(target);
    }

    public Method getMethod() {
        return super.getMethod();
    }

    public void setMethod(final Method method) {
        super.setMethod(method);
    }

    public Constructor<?> getConstructor() {
        return super.getConstructor();
    }

    public void setConstructor(final Constructor<?> constructor) {
        super.setConstructor(constructor);
    }

    public Object[] getParameters() {
        return super.getParameters();
    }

    public void setParameters(final Object[] parameters) {
        super.setParameters(parameters);
    }

    public Map<String, Object> getContextData() throws IllegalStateException {
        return super.getContextData();
    }

    public void setContextData(final Map<String, Object> contextData) {
        super.setContextData(contextData);
    }

    public Object getTimer() {
        return super.getTimer();
    }

    public void setTimer(final Object timer) {
        super.setTimer(timer);
    }

    public <T> T getPrivateData(final Class<T> type) {
        return super.getPrivateData(type);
    }

    public Object getPrivateData(final Object key) {
        return super.getPrivateData(key);
    }

    public <T> T putPrivateData(final Class<T> type, final T value) {
        return super.putPrivateData(type, value);
    }

    public Object putPrivateData(final Object key, final Object value) {
        return super.putPrivateData(key, value);
    }

    /**
     * Get the current interceptors.  The returned array should not be modified.
     *
     * @return the interceptors
     */
    public AsynchronousInterceptor[] getInterceptors() {
        return interceptors;
    }

    /**
     * Returns the next interceptor index.
     *
     * @return the next interceptor index
     */
    public int getNextInterceptorIndex() {
        return interceptorPosition;
    }

    /**
     * Set the interceptor iterator.
     *
     * @param interceptors the interceptor list
     */
    public void setInterceptors(final AsynchronousInterceptor[] interceptors) {
        setInterceptors(interceptors, 0);
    }

    /**
     * Set the interceptor array from a list.
     *
     * @param interceptorList the interceptor list
     */
    public void setInterceptors(final List<AsynchronousInterceptor> interceptorList) {
        setInterceptors(interceptorList.toArray(new AsynchronousInterceptor[interceptorList.size()]), 0);
    }

    /**
     * Set the interceptors, with a starting index to proceed from.
     *
     * @param interceptors the interceptor array
     * @param nextIndex the next index to proceed
     */
    public void setInterceptors(final AsynchronousInterceptor[] interceptors, int nextIndex) {
        if (interceptors == null) {
            throw new IllegalArgumentException("interceptors is null");
        }
        this.interceptors = interceptors;
        this.interceptorPosition = nextIndex;
    }

    /**
     * Set the interceptors, with a starting index to proceed from.
     *
     * @param interceptorList the interceptor list
     * @param nextIndex the next index to proceed
     */
    public void setInterceptors(final List<AsynchronousInterceptor> interceptorList, int nextIndex) {
        setInterceptors(interceptorList.toArray(new AsynchronousInterceptor[interceptorList.size()]), nextIndex);
    }

    public boolean isBlockingCaller() {
        return super.isBlockingCaller();
    }

    public void setBlockingCaller(final boolean blockingCaller) {
        super.setBlockingCaller(blockingCaller);
    }

    /**
     * Set the result supplier for this invocation.
     *
     * @param resultSupplier the result supplier for this invocation
     */
    public void setResultSupplier(final AsynchronousInterceptor.ResultSupplier resultSupplier) {
        this.resultSupplier = resultSupplier;
    }

    /**
     * Get the currently set result supplier for this invocation.
     *
     * @return the currently set result supplier for this invocation
     */
    public AsynchronousInterceptor.ResultSupplier getResultSupplier() {
        return resultSupplier;
    }

    /**
     * Get the invocation result from the next interceptor in the chain.
     *
     * @return the invocation result
     * @throws Exception the invocation exception
     */
    public Object getResult() throws Exception {
        final AsynchronousInterceptor[] interceptors = this.interceptors;
        int interceptorPosition = this.interceptorPosition;
        if (interceptorPosition < interceptors.length) {
            this.interceptorPosition = interceptorPosition + 1;
            return interceptors[interceptorPosition].processResult(this);
        } else {
            final AsynchronousInterceptor.ResultSupplier resultSupplier = this.resultSupplier;
            if (resultSupplier == null) {
                throw msg.noAsynchronousResultSupplierSet();
            }
            return resultSupplier.get();
        }
    }

    /**
     * Terminate the invocation result chain by discarding the current result.
     */
    public void discardResult() {
        AsynchronousInterceptor.ResultSupplier resultSupplier = this.resultSupplier;
        if (resultSupplier == null) {
            throw msg.noAsynchronousResultSupplierSet();
        }
        this.resultSupplier = null;
        resultSupplier.discard();
    }

    /**
     * Pass the invocation on to the next or previous step in the chain (depending on whether a result has been set).
     */
    public void proceed() throws Exception {
        final AsynchronousInterceptor[] interceptors = this.interceptors;
        int interceptorPosition = this.interceptorPosition;
        if (interceptorPosition < interceptors.length) {
            this.interceptorPosition = interceptorPosition + 1;
            interceptors[interceptorPosition].processInvocation(this);
        } else {
            // complete() should have been called by now
            throw msg.cannotProceed();
        }
    }

    /**
     * Indicate that the interceptor chain should stop request processing and begin result processing.
     */
    public void complete() {
        this.interceptorPosition = 0;
        consumer.accept(this);
    }

    InterceptorContext toSynchronous() {
        return new InterceptorContext(this);
    }
}
