/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;
import net.minecraft.client.gui.ScaledResolution;

public class Render2DEvent
extends Event {
    public ScaledResolution sr;
    private int width;
    private int height;
    float partialTicks;

    public Render2DEvent(ScaledResolution sr, float ticks, int width, int height) {
        this.partialTicks = ticks;
        this.sr = sr;
        this.width = width;
        this.height = height;
    }

    public float getTicks() {
        return this.partialTicks;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ScaledResolution getScaledResolution() {
        return this.sr;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

