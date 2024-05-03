/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumWorldBlockLayer;

public class BlockGlass
extends BlockBreakable {
    public BlockGlass(Material materialIn, boolean ignoreSimilarity) {
        super(materialIn, ignoreSimilarity);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }
}

