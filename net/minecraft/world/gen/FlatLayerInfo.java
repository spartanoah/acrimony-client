/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class FlatLayerInfo {
    private final int field_175902_a;
    private IBlockState field_175901_b;
    private int layerCount = 1;
    private int layerMinimumY;

    public FlatLayerInfo(int p_i45467_1_, Block p_i45467_2_) {
        this(3, p_i45467_1_, p_i45467_2_);
    }

    public FlatLayerInfo(int p_i45627_1_, int p_i45627_2_, Block p_i45627_3_) {
        this.field_175902_a = p_i45627_1_;
        this.layerCount = p_i45627_2_;
        this.field_175901_b = p_i45627_3_.getDefaultState();
    }

    public FlatLayerInfo(int p_i45628_1_, int p_i45628_2_, Block p_i45628_3_, int p_i45628_4_) {
        this(p_i45628_1_, p_i45628_2_, p_i45628_3_);
        this.field_175901_b = p_i45628_3_.getStateFromMeta(p_i45628_4_);
    }

    public int getLayerCount() {
        return this.layerCount;
    }

    public IBlockState func_175900_c() {
        return this.field_175901_b;
    }

    private Block func_151536_b() {
        return this.field_175901_b.getBlock();
    }

    private int getFillBlockMeta() {
        return this.field_175901_b.getBlock().getMetaFromState(this.field_175901_b);
    }

    public int getMinY() {
        return this.layerMinimumY;
    }

    public void setMinY(int p_82660_1_) {
        this.layerMinimumY = p_82660_1_;
    }

    public String toString() {
        String s;
        if (this.field_175902_a >= 3) {
            ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.func_151536_b());
            String string = s = resourcelocation == null ? "null" : resourcelocation.toString();
            if (this.layerCount > 1) {
                s = this.layerCount + "*" + s;
            }
        } else {
            s = Integer.toString(Block.getIdFromBlock(this.func_151536_b()));
            if (this.layerCount > 1) {
                s = this.layerCount + "x" + s;
            }
        }
        int i = this.getFillBlockMeta();
        if (i > 0) {
            s = s + ":" + i;
        }
        return s;
    }
}

