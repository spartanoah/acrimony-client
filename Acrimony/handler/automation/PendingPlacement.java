/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.automation;

import net.minecraft.util.MathHelper;

public class PendingPlacement {
    public final int x;
    public final int y;
    public final int z;

    public PendingPlacement(double x, double y, double z) {
        this.x = MathHelper.floor_double(x);
        this.y = MathHelper.floor_double(y);
        this.z = MathHelper.floor_double(z);
    }
}

