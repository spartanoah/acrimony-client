/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_16_3to1_16_4.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public class PlayerHandStorage
implements StorableObject {
    private int currentHand;

    public int getCurrentHand() {
        return this.currentHand;
    }

    public void setCurrentHand(int currentHand) {
        this.currentHand = currentHand;
    }
}

