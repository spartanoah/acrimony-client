/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCocoa
extends BlockDirectional
implements IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 2);

    public BlockCocoa() {
        super(Material.plants);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(AGE, 0));
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int i;
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlock(worldIn, pos, state);
        } else if (worldIn.rand.nextInt(5) == 0 && (i = state.getValue(AGE).intValue()) < 2) {
            worldIn.setBlockState(pos, state.withProperty(AGE, i + 1), 2);
        }
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState iblockstate = worldIn.getBlockState(pos = pos.offset(state.getValue(FACING)));
        return iblockstate.getBlock() == Blocks.log && iblockstate.getValue(BlockPlanks.VARIANT) == BlockPlanks.EnumType.JUNGLE;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(FACING);
        int i = iblockstate.getValue(AGE);
        int j = 4 + i * 2;
        int k = 5 + i * 2;
        float f = (float)j / 2.0f;
        switch (enumfacing) {
            case SOUTH: {
                this.setBlockBounds((8.0f - f) / 16.0f, (12.0f - (float)k) / 16.0f, (15.0f - (float)j) / 16.0f, (8.0f + f) / 16.0f, 0.75f, 0.9375f);
                break;
            }
            case NORTH: {
                this.setBlockBounds((8.0f - f) / 16.0f, (12.0f - (float)k) / 16.0f, 0.0625f, (8.0f + f) / 16.0f, 0.75f, (1.0f + (float)j) / 16.0f);
                break;
            }
            case WEST: {
                this.setBlockBounds(0.0625f, (12.0f - (float)k) / 16.0f, (8.0f - f) / 16.0f, (1.0f + (float)j) / 16.0f, 0.75f, (8.0f + f) / 16.0f);
                break;
            }
            case EAST: {
                this.setBlockBounds((15.0f - (float)j) / 16.0f, (12.0f - (float)k) / 16.0f, (8.0f - f) / 16.0f, 0.9375f, 0.75f, (8.0f + f) / 16.0f);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing enumfacing = EnumFacing.fromAngle(placer.rotationYaw);
        worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (!facing.getAxis().isHorizontal()) {
            facing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, facing.getOpposite()).withProperty(AGE, 0);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlock(worldIn, pos, state);
        }
    }

    private void dropBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, Blocks.air.getDefaultState(), 3);
        this.dropBlockAsItem(worldIn, pos, state, 0);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        int i = state.getValue(AGE);
        int j = 1;
        if (i >= 2) {
            j = 3;
        }
        for (int k = 0; k < j; ++k) {
            BlockCocoa.spawnAsEntity(worldIn, pos, new ItemStack(Items.dye, 1, EnumDyeColor.BROWN.getDyeDamage()));
        }
    }

    @Override
    public Item getItem(World worldIn, BlockPos pos) {
        return Items.dye;
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos) {
        return EnumDyeColor.BROWN.getDyeDamage();
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, state.withProperty(AGE, state.getValue(AGE) + 1), 2);
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(AGE, (meta & 0xF) >> 2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i |= state.getValue(FACING).getHorizontalIndex();
        return i |= state.getValue(AGE) << 2;
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, AGE);
    }
}

