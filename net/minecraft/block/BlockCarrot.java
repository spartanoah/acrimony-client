/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import net.minecraft.block.BlockCrops;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class BlockCarrot
extends BlockCrops {
    @Override
    protected Item getSeed() {
        return Items.carrot;
    }

    @Override
    protected Item getCrop() {
        return Items.carrot;
    }
}

