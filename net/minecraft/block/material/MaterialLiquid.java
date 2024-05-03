/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialLiquid
extends Material {
    public MaterialLiquid(MapColor color) {
        super(color);
        this.setReplaceable();
        this.setNoPushMobility();
    }

    @Override
    public boolean isLiquid() {
        return true;
    }

    @Override
    public boolean blocksMovement() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}

