/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

    public abstract void setProxyInstance(ProxyInstance proxy);

}