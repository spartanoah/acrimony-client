/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class ItemRenderEvent
extends Event {
    private boolean renderBlocking;

    public boolean shouldRenderBlocking() {
        return this.renderBlocking;
    }

    public void setRenderBlocking(boolean renderBlocking) {
        this.renderBlocking = renderBlocking;
    }

    public ItemRenderEvent(boolean renderBlocking) {
        this.renderBlocking = renderBlocking;
    }
}

