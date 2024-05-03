/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

final class ChainBuilder<E> {
    private final LinkedList<E> list = new LinkedList();
    private final Map<Class<?>, E> uniqueClasses = new HashMap();

    private void ensureUnique(E e) {
        E previous = this.uniqueClasses.remove(e.getClass());
        if (previous != null) {
            this.list.remove(previous);
        }
        this.uniqueClasses.put(e.getClass(), e);
    }

    public ChainBuilder<E> addFirst(E e) {
        if (e == null) {
            return this;
        }
        this.ensureUnique(e);
        this.list.addFirst(e);
        return this;
    }

    public ChainBuilder<E> addLast(E e) {
        if (e == null) {
            return this;
        }
        this.ensureUnique(e);
        this.list.addLast(e);
        return this;
    }

    public ChainBuilder<E> addAllFirst(Collection<E> c) {
        if (c == null) {
            return this;
        }
        for (E e : c) {
            this.addFirst(e);
        }
        return this;
    }

    @SafeVarargs
    public final ChainBuilder<E> addAllFirst(E ... c) {
        if (c == null) {
            return this;
        }
        for (E e : c) {
            this.addFirst(e);
        }
        return this;
    }

    public ChainBuilder<E> addAllLast(Collection<E> c) {
        if (c == null) {
            return this;
        }
        for (E e : c) {
            this.addLast(e);
        }
        return this;
    }

    @SafeVarargs
    public final ChainBuilder<E> addAllLast(E ... c) {
        if (c == null) {
            return this;
        }
        for (E e : c) {
            this.addLast(e);
        }
        return this;
    }

    public LinkedList<E> build() {
        return new LinkedList<E>(this.list);
    }
}

