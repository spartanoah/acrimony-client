/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDirt
extends Block {
    public static final PropertyEnum<DirtType> VARIANT = PropertyEnum.create("variant", DirtType.class);
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected BlockDirt() {
        super(Material.ground);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, DirtType.DIRT).withProperty(SNOWY, false));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public MapColor getMapColor(IBlockState state) {
        return state.getValue(VARIANT).func_181066_d();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(VARIANT) == DirtType.PODZOL) {
            Block block = worldIn.getBlockState(pos.up()).getBlock();
            state = state.withProperty(SNOWY, block == Blocks.snow || block == Blocks.snow_layer);
        }
        return state;
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(this, 1, DirtType.DIRT.getMetadata()));
        list.add(new ItemStack(this, 1, DirtType.COARSE_DIRT.getMetadata()));
        list.add(new ItemStack(this, 1, DirtType.PODZOL.getMetadata()));
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock() != this ? 0 : iblockstate.getValue(VARIANT).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, DirtType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT, SNOWY);
    }

    @Override
    public int damageDropped(IBlockState state) {
        DirtType blockdirt$dirttype = state.getValue(VARIANT);
        if (blockdirt$dirttype == DirtType.PODZOL) {
            blockdirt$dirttype = DirtType.DIRT;
        }
        return blockdirt$dirttype.getMetadata();
    }

    public static enum DirtType implements IStringSerializable
    {
        DIRT(0, "dirt", "default", MapColor.dirtColor),
        COARSE_DIRT(1, "coarse_dirt", "coarse", MapColor.dirtColor),
        PODZOL(2, "podzol", MapColor.obsidianColor);

        private static final DirtType[] METADATA_LOOKUP;
        private final int metadata;
        private final String name;
        private final String unlocalizedName;
        private final MapColor field_181067_h;

        private DirtType(int p_i46396_3_, String p_i46396_4_, MapColor p_i46396_5_) {
            this(p_i46396_3_, p_i46396_4_, p_i46396_4_, p_i46396_5_);
        }

        private DirtType(int p_i46397_3_, String p_i46397_4_, String p_i46397_5_, MapColor p_i46397_6_) {
            this.metadata = p_i46397_3_;
            this.name = p_i46397_4_;
            this.unlocalizedName = p_i46397_5_;
            this.field_181067_h = p_i46397_6_;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        public MapColor func_181066_d() {
            return this.field_181067_h;
        }

        public String toString() {
            return this.name;
        }

        public static DirtType byMetadata(int metadata) {
            if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
                metadata = 0;
            }
            return METADATA_LOOKUP[metadata];
        }

        @Override
        public String getName() {
            return this.name;
        }

        static {
            METADATA_LOOKUP = new DirtType[DirtType.values().length];
            DirtType[] dirtTypeArray = DirtType.values();
            int n = dirtTypeArray.length;
            for (int i = 0; i < n; ++i) {
                DirtType blockdirt$dirttype;
                DirtType.METADATA_LOOKUP[blockdirt$dirttype.getMetadata()] = blockdirt$dirttype = dirtTypeArray[i];
            }
        }
    }
}

