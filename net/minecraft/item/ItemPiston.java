/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemPiston
extends ItemBlock {
    public ItemPiston(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int damage) {
        return 7;
    }
}

