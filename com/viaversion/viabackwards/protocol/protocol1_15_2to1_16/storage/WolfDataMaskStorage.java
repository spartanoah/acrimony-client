/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.storage;

public final class WolfDataMaskStorage {
    private byte tameableMask;

    public WolfDataMaskStorage(byte tameableMask) {
        this.tameableMask = tameableMask;
    }

    public void setTameableMask(byte tameableMask) {
        this.tameableMask = tameableMask;
    }

    public byte tameableMask() {
        return this.tameableMask;
    }
}

