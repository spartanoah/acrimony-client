/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

public final class MutableAnimation {
    private double value;
    private double lastTarget;
    private float speed;
    private long lastMS;

    public MutableAnimation(double value, float speed) {
        this.value = value;
        this.speed = speed;
    }

    public void interpolate(double target) {
        long delta = System.currentTimeMillis() - this.lastMS;
        this.lastMS = System.currentTimeMillis();
        if (this.value != target) {
            this.value = this.move(target, this.value, delta, this.speed);
        }
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return this.value == this.lastTarget;
    }

    public double move(double target, double current, long delta, float speed) {
        this.lastTarget = target;
        if (delta < 1L) {
            delta = 1L;
        }
        boolean dir = target > current;
        current += (target - current) / 50.0 * (double)((float)delta * speed) + (double)(dir ? 0.001f : -0.001f);
        if (dir && current > target) {
            current = target;
        } else if (!dir && current < target) {
            current = target;
        }
        return current;
    }
}

