/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ITileEntityProvider {
    public TileEntity createNewTileEntity(World var1, int var2);
}

