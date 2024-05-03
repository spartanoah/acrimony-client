/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class Render3DEvent
extends Event {
    private float partialTicks;

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}

