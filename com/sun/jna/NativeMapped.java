/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.FromNativeContext;

public interface NativeMapped {
    public Object fromNative(Object var1, FromNativeContext var2);

    public Object toNative();

    public Class nativeType();
}

