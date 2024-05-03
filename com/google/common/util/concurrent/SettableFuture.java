/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.util.concurrent;

import com.google.common.util.concurrent.AbstractFuture;
import javax.annotation.Nullable;

public final class SettableFuture<V>
extends AbstractFuture<V> {
    public static <V> SettableFuture<V> create() {
        return new SettableFuture<V>();
    }

    private SettableFuture() {
    }

    @Override
    public boolean set(@Nullable V value) {
        return super.set(value);
    }

    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }
}

