/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public class PlayerSneakStorage
implements StorableObject {
    private boolean sneaking;

    public boolean isSneaking() {
        return this.sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }
}

