/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockStoneBrick
extends Block {
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);
    public static final int DEFAULT_META = EnumType.DEFAULT.getMetadata();
    public static final int MOSSY_META = EnumType.MOSSY.getMetadata();
    public static final int CRACKED_META = EnumType.CRACKED.getMetadata();
    public static final int CHISELED_META = EnumType.CHISELED.getMetadata();

    public BlockStoneBrick() {
        super(Material.rock);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (EnumType blockstonebrick$enumtype : EnumType.values()) {
            list.add(new ItemStack(itemIn, 1, blockstonebrick$enumtype.getMetadata()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    public static enum EnumType implements IStringSerializable
    {
        DEFAULT(0, "stonebrick", "default"),
        MOSSY(1, "mossy_stonebrick", "mossy"),
        CRACKED(2, "cracked_stonebrick", "cracked"),
        CHISELED(3, "chiseled_stonebrick", "chiseled");

        private static final EnumType[] META_LOOKUP;
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }

        @Override
        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            META_LOOKUP = new EnumType[EnumType.values().length];
            EnumType[] enumTypeArray = EnumType.values();
            int n = enumTypeArray.length;
            for (int i = 0; i < n; ++i) {
                EnumType blockstonebrick$enumtype;
                EnumType.META_LOOKUP[blockstonebrick$enumtype.getMetadata()] = blockstonebrick$enumtype = enumTypeArray[i];
            }
        }
    }
}

