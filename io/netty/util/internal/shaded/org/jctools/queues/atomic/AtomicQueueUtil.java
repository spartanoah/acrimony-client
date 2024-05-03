/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

final class AtomicQueueUtil {
    AtomicQueueUtil() {
    }

    static <E> E lvRefElement(AtomicReferenceArray<E> buffer, int offset) {
        return buffer.get(offset);
    }

    static <E> E lpRefElement(AtomicReferenceArray<E> buffer, int offset) {
        return buffer.get(offset);
    }

    static <E> void spRefElement(AtomicReferenceArray<E> buffer, int offset, E value) {
        buffer.lazySet(offset, value);
    }

    static void soRefElement(AtomicReferenceArray buffer, int offset, Object value) {
        buffer.lazySet(offset, value);
    }

    static <E> void svRefElement(AtomicReferenceArray<E> buffer, int offset, E value) {
        buffer.set(offset, value);
    }

    static int calcRefElementOffset(long index) {
        return (int)index;
    }

    static int calcCircularRefElementOffset(long index, long mask) {
        return (int)(index & mask);
    }

    static <E> AtomicReferenceArray<E> allocateRefArray(int capacity) {
        return new AtomicReferenceArray(capacity);
    }

    static void spLongElement(AtomicLongArray buffer, int offset, long e) {
        buffer.lazySet(offset, e);
    }

    static void soLongElement(AtomicLongArray buffer, int offset, long e) {
        buffer.lazySet(offset, e);
    }

    static long lpLongElement(AtomicLongArray buffer, int offset) {
        return buffer.get(offset);
    }

    static long lvLongElement(AtomicLongArray buffer, int offset) {
        return buffer.get(offset);
    }

    static int calcLongElementOffset(long index) {
        return (int)index;
    }

    static int calcCircularLongElementOffset(long index, int mask) {
        return (int)(index & (long)mask);
    }

    static AtomicLongArray allocateLongArray(int capacity) {
        return new AtomicLongArray(capacity);
    }

    static int length(AtomicReferenceArray<?> buf) {
        return buf.length();
    }

    static int modifiedCalcCircularRefElementOffset(long index, long mask) {
        return (int)(index & mask) >> 1;
    }

    static int nextArrayOffset(AtomicReferenceArray<?> curr) {
        return AtomicQueueUtil.length(curr) - 1;
    }
}

