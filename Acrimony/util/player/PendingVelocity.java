/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.player;

public class PendingVelocity {
    private double x;
    private double y;
    private double z;

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public PendingVelocity(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

