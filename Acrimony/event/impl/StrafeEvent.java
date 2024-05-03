/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class StrafeEvent
extends Event {
    private float forward;
    private float strafe;
    private float friction;
    private float attributeSpeed;
    private float yaw;

    public StrafeEvent(float forward, float strafe, float friction, float attributeSpeed, float yaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.attributeSpeed = attributeSpeed;
        this.yaw = yaw;
    }

    public float getForward() {
        return this.forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public float getFriction() {
        return this.friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getAttributeSpeed() {
        return this.attributeSpeed;
    }

    public void setAttributeSpeed(float attributeSpeed) {
        this.attributeSpeed = attributeSpeed;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}

