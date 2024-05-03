/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

public class NativeLong
extends IntegerType {
    public static final int SIZE = Native.LONG_SIZE;

    public NativeLong() {
        this(0L);
    }

    public NativeLong(long value) {
        super(SIZE, value);
    }
}

