/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.type.CancellableEvent;

public class JumpEvent
extends CancellableEvent {
    private double motionY;
    private float yaw;
    private boolean boosting;
    private float boostAmount;

    public JumpEvent(double motionY, float yaw, boolean boosting, float boostAmount) {
        this.motionY = motionY;
        this.yaw = yaw;
        this.boosting = boosting;
        this.boostAmount = boostAmount;
    }

    public double getMotionY() {
        return this.motionY;
    }

    public float getYaw() {
        return this.yaw;
    }

    public boolean isBoosting() {
        return this.boosting;
    }

    public float getBoostAmount() {
        return this.boostAmount;
    }

    public void setMotionY(double motionY) {
        this.motionY = motionY;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setBoosting(boolean boosting) {
        this.boosting = boosting;
    }

    public void setBoostAmount(float boostAmount) {
        this.boostAmount = boostAmount;
    }
}

