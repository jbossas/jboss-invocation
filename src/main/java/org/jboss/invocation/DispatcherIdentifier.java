/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, JBoss Inc., and individual contributors as indicated
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

package org.jboss.invocation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.jboss.marshalling.FieldSetter;

/**
 * A unique identifier for a dispatcher within a single node.
 */
public final class DispatcherIdentifier implements Serializable {

    private static final long serialVersionUID = -1228682891715016792L;

    private final String contextName;
    private final String dispatcherName;
    private transient final int hashCode;

    private static final FieldSetter hashCodeSetter = FieldSetter.get(DispatcherIdentifier.class, "hashCode");

    public DispatcherIdentifier(final String contextName, final String dispatcherName) {
        if (contextName == null) {
            throw new IllegalArgumentException("contextName is null");
        }
        if (dispatcherName == null) {
            throw new IllegalArgumentException("dispatcherName is null");
        }
        this.contextName = contextName;
        this.dispatcherName = dispatcherName;
        hashCode = hashCode(contextName, dispatcherName);
    }

    public String getContextName() {
        return contextName;
    }

    public String getDispatcherName() {
        return dispatcherName;
    }

    private static int hashCode(String contextName, String dispatcherName) {
        return contextName.hashCode() * 31 + dispatcherName.hashCode();
    }

    public boolean equals(final Object obj) {
        return obj instanceof DispatcherIdentifier && equals((DispatcherIdentifier) obj);
    }

    public boolean equals(final DispatcherIdentifier other) {
        return other == this || other != null && hashCode == other.hashCode && contextName.equals(other.contextName) && dispatcherName.equals(other.dispatcherName);
    }

    public int hashCode() {
        return hashCode;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (contextName == null) {
            throw new InvalidObjectException("contextName is null");
        }
        if (dispatcherName == null) {
            throw new InvalidObjectException("dispatcherName is null");
        }
        hashCodeSetter.setInt(this, hashCode(contextName, dispatcherName));
    }

    public String toString() {
        return String.format("Dispatcher identifier {context=\"%s\", name=\"%s\"}", contextName, dispatcherName);
    }
}
