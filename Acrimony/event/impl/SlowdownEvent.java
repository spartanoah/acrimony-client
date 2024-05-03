/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class SlowdownEvent
extends Event {
    private float forward;
    private float strafe;
    private boolean allowedSprinting;

    public float getForward() {
        return this.forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public boolean isAllowedSprinting() {
        return this.allowedSprinting;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public void setAllowedSprinting(boolean allowedSprinting) {
        this.allowedSprinting = allowedSprinting;
    }

    public SlowdownEvent(float forward, float strafe, boolean allowedSprinting) {
        this.forward = forward;
        this.strafe = strafe;
        this.allowedSprinting = allowedSprinting;
    }
}

