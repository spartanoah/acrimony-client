/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna.ptr;

import com.sun.jna.ptr.ByReference;

public class IntByReference
extends ByReference {
    public IntByReference() {
        this(0);
    }

    public IntByReference(int value) {
        super(4);
        this.setValue(value);
    }

    public void setValue(int value) {
        this.getPointer().setInt(0L, value);
    }

    public int getValue() {
        return this.getPointer().getInt(0L);
    }
}

