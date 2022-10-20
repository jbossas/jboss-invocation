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

package org.jboss.invocation.proxy;

import java.io.Serializable;

/**
 * Serialized representation of a proxy. When a proxy that is using a {@link SerializableProxy} is serialized it is serialized
 * via the following mechanism:
 * 
 * <pre>
 * Object writeReplace() throws ObjectStreamException {
 *     SerializableProxy proxy = serializableClass.newInstance();
 *     proxy.setProxyInstance(this);
 *     return proxy;
 * }
 * </pre>
 * <p>
 * Implementors of this interface should store any state that is required to re-create the proxy in this class's serialized
 * form. Implementors also *MUST* implement an <code>Object readResolve() throws ObjectStreamException</code> method, the
 * returns the de-serialized proxy.
 * 
 * @see DefaultSerializableProxy
 * @author Stuart Douglas
 * 
 */
public interface SerializableProxy extends Serializable {

    /**
     * Set the proxy instance.
     *
     * @param proxy the proxy instance
     */
    void setProxyInstance(Object proxy);
}