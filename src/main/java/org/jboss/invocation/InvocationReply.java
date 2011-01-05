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
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * An invocation reply.  Includes the return value, along with any attachments that may have been set along the way.
 */
public final class InvocationReply implements Serializable {

    private static final long serialVersionUID = 4330152364952496586L;

    /**
     * The method return value.
     *
     * @serial
     */
    private final Object reply;
    /**
     * The invocation properties.
     */
    private transient volatile InvocationProperties properties;

    /**
     * Construct a new instance.
     *
     * @param reply the reply
     */
    public InvocationReply(final Object reply) {
        this.reply = reply;
    }

    /**
     * Construct a new instance.
     *
     * @param reply the reply
     * @param properties the initial invocation properties
     */
    public InvocationReply(final Object reply, final InvocationProperties properties) {
        this.reply = reply;
        this.properties = properties;
    }

    /**
     * Get the reply.
     *
     * @return the reply
     */
    public Object getReply() {
        return reply;
    }

    /**
     * Get the invocation properties.
     *
     * @return the invocation properties
     */
    public InvocationProperties getProperties() {
        return properties;
    }

    /**
     * Replace the invocation properties.
     *
     * @param properties the invocation properties
     */
    public void setProperties(final InvocationProperties properties) {
        this.properties = properties;
    }

    /**
     * Get the string representation of this object.
     *
     * @return the string representation
     */
    public String toString() {
        return "Invocation reply with value (" + reply + ")";
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        properties = defaulted((InvocationProperties) ois.readObject(), InvocationProperties.EMPTY);
    }

    private static <T> T defaulted(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
