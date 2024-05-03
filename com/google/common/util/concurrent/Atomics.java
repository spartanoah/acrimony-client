/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;

public final class Atomics {
    private Atomics() {
    }

    public static <V> AtomicReference<V> newReference() {
        return new AtomicReference();
    }

    public static <V> AtomicReference<V> newReference(@Nullable V initialValue) {
        return new AtomicReference<V>(initialValue);
    }

    public static <E> AtomicReferenceArray<E> newReferenceArray(int length) {
        return new AtomicReferenceArray(length);
    }

    public static <E> AtomicReferenceArray<E> newReferenceArray(E[] array) {
        return new AtomicReferenceArray<E>(array);
    }
}

