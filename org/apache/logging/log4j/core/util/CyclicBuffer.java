/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.lang.reflect.Array;

public final class CyclicBuffer<T> {
    private final T[] ring;
    private int first = 0;
    private int last = 0;
    private int numElems = 0;
    private final Class<T> clazz;

    public CyclicBuffer(Class<T> clazz, int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("The maxSize argument (" + size + ") cannot be negative.");
        }
        this.ring = this.makeArray(clazz, size);
        this.clazz = clazz;
    }

    private T[] makeArray(Class<T> cls, int size) {
        return (Object[])Array.newInstance(cls, size);
    }

    public synchronized void add(T item) {
        if (this.ring.length > 0) {
            this.ring[this.last] = item;
            if (++this.last == this.ring.length) {
                this.last = 0;
            }
            if (this.numElems < this.ring.length) {
                ++this.numElems;
            } else if (++this.first == this.ring.length) {
                this.first = 0;
            }
        }
    }

    public synchronized T[] removeAll() {
        T[] array = this.makeArray(this.clazz, this.numElems);
        int index = 0;
        while (this.numElems > 0) {
            --this.numElems;
            array[index++] = this.ring[this.first];
            this.ring[this.first] = null;
            if (++this.first != this.ring.length) continue;
            this.first = 0;
        }
        return array;
    }

    public boolean isEmpty() {
        return 0 == this.numElems;
    }
}

