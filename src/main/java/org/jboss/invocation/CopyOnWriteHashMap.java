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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @param <K> key type
 * @param <V> value type
 */
final class CopyOnWriteHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
    private volatile Map<K, V> map = Collections.emptyMap();

    public V putIfAbsent(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        synchronized (this) {
            final Map<K, V> map = this.map;
            if (map.containsKey(key)) {
                return map.get(key);
            }
            this.map = new FastCopyHashMap<K, V>(map, key, value);
            return null;
        }
    }

    public boolean remove(final Object key, final Object value) {
        if (key == null) return false;
        synchronized (this) {
            final Map<K, V> map = this.map;
            final V mapVal = map.get(key);
            if (value == null && mapVal == null || value != null && value.equals(mapVal)) {
                if (map.size() == 1) {
                    this.map = Collections.emptyMap();
                } else {
                    final FastCopyHashMap<K, V> newMap = new FastCopyHashMap<K, V>(map);
                    newMap.remove(key);
                    this.map = newMap;
                }
                return true;
            }
        }
        return false;
    }

    public boolean replace(final K key, final V oldValue, final V newValue) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        synchronized (this) {
            final Map<K, V> map = this.map;
            final V mapVal = map.get(key);
            if (oldValue == null && mapVal == null || oldValue != null && oldValue.equals(mapVal)) {
                this.map = new FastCopyHashMap<K, V>(map, key, newValue);
                return true;
            }
        }
        return false;
    }

    public V replace(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        synchronized (this) {
            final Map<K, V> map = this.map;
            if (map.containsKey(key)) {
                try {
                    return map.get(key);
                } finally {
                    this.map = new FastCopyHashMap<K, V>(map, key, value);
                }
            }
        }
        return null;
    }

    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    public V get(final Object key) {
        return map.get(key);
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    public void clear() {
        map = Collections.emptyMap();
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public Collection<V> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    public V put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        synchronized (this) {
            final Map<K, V> map = this.map;
            try {
                return map.get(key);
            } finally {
                this.map = new FastCopyHashMap<K,V>(map, key, value);
            }
        }
    }

    public V remove(final Object key) {
        if (key == null) return null;
        synchronized (this) {
            final Map<K, V> map = this.map;
            if (map.containsKey(key)) {
                if (map.size() == 1) {
                    try {
                        return map.get(key);
                    } finally {
                        this.map = Collections.emptyMap();
                    }
                } else {
                    final FastCopyHashMap<K, V> newMap = new FastCopyHashMap<K, V>(map);
                    try {
                        return newMap.remove(key);
                    } finally {
                        this.map = newMap;
                    }
                }
            } else {
                return null;
            }
        }
    }

    public void putAll(final Map<? extends K, ? extends V> m) {
        synchronized (this) {
            final FastCopyHashMap<K, V> newMap = new FastCopyHashMap<K, V>(map);
            newMap.putAll(m);
            map = newMap;
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected CopyOnWriteHashMap<K, V> clone() {
        try {
            final CopyOnWriteHashMap<K, V> clone = (CopyOnWriteHashMap<K, V>) super.clone();
            final Map<K, V> map = this.map;
            if (! map.isEmpty()) {
                clone.map = new FastCopyHashMap<K, V>(map);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }
}