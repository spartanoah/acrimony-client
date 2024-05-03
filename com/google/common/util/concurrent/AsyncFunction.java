/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.util.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

public interface AsyncFunction<I, O> {
    public ListenableFuture<O> apply(I var1) throws Exception;
}

