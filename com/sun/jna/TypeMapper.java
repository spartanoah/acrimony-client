/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeConverter;

public interface TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class var1);

    public ToNativeConverter getToNativeConverter(Class var1);
}

