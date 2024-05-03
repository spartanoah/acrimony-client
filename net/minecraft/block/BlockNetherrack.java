/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNetherrack
extends Block {
    public BlockNetherrack() {
        super(Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public MapColor getMapColor(IBlockState state) {
        return MapColor.netherrackColor;
    }
}

