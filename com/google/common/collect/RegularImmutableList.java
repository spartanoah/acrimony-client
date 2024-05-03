/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableListIterator;
import javax.annotation.Nullable;

@GwtCompatible(serializable=true, emulated=true)
class RegularImmutableList<E>
extends ImmutableList<E> {
    private final transient int offset;
    private final transient int size;
    private final transient Object[] array;

    RegularImmutableList(Object[] array, int offset, int size) {
        this.offset = offset;
        this.size = size;
        this.array = array;
    }

    RegularImmutableList(Object[] array) {
        this(array, 0, array.length);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    boolean isPartialView() {
        return this.size != this.array.length;
    }

    @Override
    int copyIntoArray(Object[] dst, int dstOff) {
        System.arraycopy(this.array, this.offset, dst, dstOff, this.size);
        return dstOff + this.size;
    }

    @Override
    public E get(int index) {
        Preconditions.checkElementIndex(index, this.size);
        return (E)this.array[index + this.offset];
    }

    @Override
    public int indexOf(@Nullable Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = 0; i < this.size; ++i) {
            if (!this.array[this.offset + i].equals(object)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(@Nullable Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = this.size - 1; i >= 0; --i) {
            if (!this.array[this.offset + i].equals(object)) continue;
            return i;
        }
        return -1;
    }

    @Override
    ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
        return new RegularImmutableList<E>(this.array, this.offset + fromIndex, toIndex - fromIndex);
    }

    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        return Iterators.forArray(this.array, this.offset, this.size, index);
    }
}

