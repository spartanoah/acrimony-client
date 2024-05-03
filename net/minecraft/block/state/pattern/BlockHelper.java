/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class BlockHelper
implements Predicate<IBlockState> {
    private final Block block;

    private BlockHelper(Block blockType) {
        this.block = blockType;
    }

    public static BlockHelper forBlock(Block blockType) {
        return new BlockHelper(blockType);
    }

    @Override
    public boolean apply(IBlockState p_apply_1_) {
        return p_apply_1_ != null && p_apply_1_.getBlock() == this.block;
    }
}

