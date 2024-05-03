/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class RaytraceEvent
extends Event {
    private float yaw;
    private float pitch;
    private float prevYaw;
    private float prevPitch;
    private float partialTicks;

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getPrevYaw() {
        return this.prevYaw;
    }

    public float getPrevPitch() {
        return this.prevPitch;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public RaytraceEvent(float yaw, float pitch, float prevYaw, float prevPitch, float partialTicks) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.prevYaw = prevYaw;
        this.prevPitch = prevPitch;
        this.partialTicks = partialTicks;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setPrevYaw(float prevYaw) {
        this.prevYaw = prevYaw;
    }

    public void setPrevPitch(float prevPitch) {
        this.prevPitch = prevPitch;
    }
}

