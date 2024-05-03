/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import java.util.Arrays;

final class LongPriorityQueue {
    public static final int NO_VALUE = -1;
    private long[] array = new long[9];
    private int size;

    LongPriorityQueue() {
    }

    public void offer(long handle) {
        if (handle == -1L) {
            throw new IllegalArgumentException("The NO_VALUE (-1) cannot be added to the queue.");
        }
        ++this.size;
        if (this.size == this.array.length) {
            this.array = Arrays.copyOf(this.array, 1 + (this.array.length - 1) * 2);
        }
        this.array[this.size] = handle;
        this.lift(this.size);
    }

    public void remove(long value) {
        for (int i = 1; i <= this.size; ++i) {
            if (this.array[i] != value) continue;
            this.array[i] = this.array[this.size--];
            this.lift(i);
            this.sink(i);
            return;
        }
    }

    public long peek() {
        if (this.size == 0) {
            return -1L;
        }
        return this.array[1];
    }

    public long poll() {
        if (this.size == 0) {
            return -1L;
        }
        long val2 = this.array[1];
        this.array[1] = this.array[this.size];
        this.array[this.size] = 0L;
        --this.size;
        this.sink(1);
        return val2;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private void lift(int index) {
        int parentIndex;
        while (index > 1 && this.subord(parentIndex = index >> 1, index)) {
            this.swap(index, parentIndex);
            index = parentIndex;
        }
    }

    private void sink(int index) {
        int child;
        while ((child = index << 1) <= this.size) {
            if (child < this.size && this.subord(child, child + 1)) {
                ++child;
            }
            if (!this.subord(index, child)) break;
            this.swap(index, child);
            index = child;
        }
    }

    private boolean subord(int a, int b) {
        return this.array[a] > this.array[b];
    }

    private void swap(int a, int b) {
        long value = this.array[a];
        this.array[a] = this.array[b];
        this.array[b] = value;
    }
}

