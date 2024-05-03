/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;

@GwtCompatible
public interface Predicate<T> {
    public boolean apply(@Nullable T var1);

    public boolean equals(@Nullable Object var1);
}

