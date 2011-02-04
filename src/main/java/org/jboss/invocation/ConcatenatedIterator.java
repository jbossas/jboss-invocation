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

import java.util.ListIterator;

/**
 * A concatenated iterator.
 *
 * @param <T> the element type
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class ConcatenatedIterator<T> implements ListIterator<T> {
    private final ListIterator<T> first;
    private final ListIterator<T> second;

    ConcatenatedIterator(final ListIterator<T> first, final ListIterator<T> second) {
        this.first = first;
        this.second = second;
    }

    public boolean hasNext() {
        return first.hasNext() || second.hasNext();
    }

    public T next() {
        return first.hasNext() ? first.next() : second.next();
    }

    public boolean hasPrevious() {
        return second.hasPrevious() || first.hasPrevious();
    }

    public T previous() {
        return second.hasPrevious() ? second.previous() : first.previous();
    }

    public int nextIndex() {
        throw new UnsupportedOperationException();
    }

    public int previousIndex() {
        throw new UnsupportedOperationException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void set(final T t) {
        throw new UnsupportedOperationException();
    }

    public void add(final T t) {
        throw new UnsupportedOperationException();
    }
}
