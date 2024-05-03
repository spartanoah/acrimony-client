/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T>
implements Iterator<T>,
Iterable<T> {
    private final T[] _a;
    private int _index;

    public ArrayIterator(T[] a) {
        this._a = a;
        this._index = 0;
    }

    @Override
    public boolean hasNext() {
        return this._index < this._a.length;
    }

    @Override
    public T next() {
        if (this._index >= this._a.length) {
            throw new NoSuchElementException();
        }
        return this._a[this._index++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}

