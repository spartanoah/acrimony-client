/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.dispenser;

import net.minecraft.dispenser.IPosition;

public class PositionImpl
implements IPosition {
    protected final double x;
    protected final double y;
    protected final double z;

    public PositionImpl(double xCoord, double yCoord, double zCoord) {
        this.x = xCoord;
        this.y = yCoord;
        this.z = zCoord;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }
}

