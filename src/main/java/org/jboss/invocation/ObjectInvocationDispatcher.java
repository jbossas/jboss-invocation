/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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
import java.lang.reflect.InvocationTargetException;

/**
 * An {@code InvocationDispatcher} which executes the invocation method on a target object.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ObjectInvocationDispatcher implements Serializable, InvocationDispatcher {

    private static final long serialVersionUID = 149324822317622879L;

    private final Object target;

    /**
     * Construct a new instance.
     *
     * @param target the target for invocations
     */
    public ObjectInvocationDispatcher(final Object target) {
        this.target = target;
    }

    /** {@inheritDoc} */
    public InvocationReply dispatch(final Invocation invocation) throws InvocationException {
        try {
            return new InvocationReply(invocation.getMethodIdentifier().getPublicMethod(target.getClass()).invoke(target, invocation.getArgs()));
        } catch (IllegalAccessException e) {
            throw new InvocationException(new IllegalAccessError(e.getMessage()));
        } catch (InvocationTargetException e) {
            throw new InvocationException(e.getCause());
        } catch (ClassNotFoundException e) {
            throw new InvocationException(e);
        } catch (NoSuchMethodException e) {
            throw new InvocationException(e);
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (! target.getClass().getProtectionDomain().implies(Invocation.INVOCATION_PERMISSION)) {
                throw new InvalidObjectException("Target object does not have invoke permission");
            }
        }
    }

    public String toString() {
        return "dispatcher to " + target;
    }
}
