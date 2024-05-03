/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.dispenser;

import net.minecraft.dispenser.ILocatableSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

public interface IBlockSource
extends ILocatableSource {
    @Override
    public double getX();

    @Override
    public double getY();

    @Override
    public double getZ();

    public BlockPos getBlockPos();

    public int getBlockMetadata();

    public <T extends TileEntity> T getBlockTileEntity();
}

