/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import com.google.common.base.Predicate;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockPumpkin
extends BlockDirectional {
    private BlockPattern snowmanBasePattern;
    private BlockPattern snowmanPattern;
    private BlockPattern golemBasePattern;
    private BlockPattern golemPattern;
    private static final Predicate<IBlockState> field_181085_Q = new Predicate<IBlockState>(){

        @Override
        public boolean apply(IBlockState p_apply_1_) {
            return p_apply_1_ != null && (p_apply_1_.getBlock() == Blocks.pumpkin || p_apply_1_.getBlock() == Blocks.lit_pumpkin);
        }
    };

    protected BlockPumpkin() {
        super(Material.gourd, MapColor.adobeColor);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        this.trySpawnGolem(worldIn, pos);
    }

    public boolean canDispenserPlace(World worldIn, BlockPos pos) {
        return this.getSnowmanBasePattern().match(worldIn, pos) != null || this.getGolemBasePattern().match(worldIn, pos) != null;
    }

    private void trySpawnGolem(World worldIn, BlockPos pos) {
        block9: {
            BlockPattern.PatternHelper blockpattern$patternhelper;
            block8: {
                blockpattern$patternhelper = this.getSnowmanPattern().match(worldIn, pos);
                if (blockpattern$patternhelper == null) break block8;
                for (int i = 0; i < this.getSnowmanPattern().getThumbLength(); ++i) {
                    BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(0, i, 0);
                    worldIn.setBlockState(blockworldstate.getPos(), Blocks.air.getDefaultState(), 2);
                }
                EntitySnowman entitysnowman = new EntitySnowman(worldIn);
                BlockPos blockpos1 = blockpattern$patternhelper.translateOffset(0, 2, 0).getPos();
                entitysnowman.setLocationAndAngles((double)blockpos1.getX() + 0.5, (double)blockpos1.getY() + 0.05, (double)blockpos1.getZ() + 0.5, 0.0f, 0.0f);
                worldIn.spawnEntityInWorld(entitysnowman);
                for (int j = 0; j < 120; ++j) {
                    worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double)blockpos1.getX() + worldIn.rand.nextDouble(), (double)blockpos1.getY() + worldIn.rand.nextDouble() * 2.5, (double)blockpos1.getZ() + worldIn.rand.nextDouble(), 0.0, 0.0, 0.0, new int[0]);
                }
                for (int i1 = 0; i1 < this.getSnowmanPattern().getThumbLength(); ++i1) {
                    BlockWorldState blockworldstate1 = blockpattern$patternhelper.translateOffset(0, i1, 0);
                    worldIn.notifyNeighborsRespectDebug(blockworldstate1.getPos(), Blocks.air);
                }
                break block9;
            }
            blockpattern$patternhelper = this.getGolemPattern().match(worldIn, pos);
            if (blockpattern$patternhelper == null) break block9;
            for (int k = 0; k < this.getGolemPattern().getPalmLength(); ++k) {
                for (int l = 0; l < this.getGolemPattern().getThumbLength(); ++l) {
                    worldIn.setBlockState(blockpattern$patternhelper.translateOffset(k, l, 0).getPos(), Blocks.air.getDefaultState(), 2);
                }
            }
            BlockPos blockpos = blockpattern$patternhelper.translateOffset(1, 2, 0).getPos();
            EntityIronGolem entityirongolem = new EntityIronGolem(worldIn);
            entityirongolem.setPlayerCreated(true);
            entityirongolem.setLocationAndAngles((double)blockpos.getX() + 0.5, (double)blockpos.getY() + 0.05, (double)blockpos.getZ() + 0.5, 0.0f, 0.0f);
            worldIn.spawnEntityInWorld(entityirongolem);
            for (int j1 = 0; j1 < 120; ++j1) {
                worldIn.spawnParticle(EnumParticleTypes.SNOWBALL, (double)blockpos.getX() + worldIn.rand.nextDouble(), (double)blockpos.getY() + worldIn.rand.nextDouble() * 3.9, (double)blockpos.getZ() + worldIn.rand.nextDouble(), 0.0, 0.0, 0.0, new int[0]);
            }
            for (int k1 = 0; k1 < this.getGolemPattern().getPalmLength(); ++k1) {
                for (int l1 = 0; l1 < this.getGolemPattern().getThumbLength(); ++l1) {
                    BlockWorldState blockworldstate2 = blockpattern$patternhelper.translateOffset(k1, l1, 0);
                    worldIn.notifyNeighborsRespectDebug(blockworldstate2.getPos(), Blocks.air);
                }
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState((BlockPos)pos).getBlock().blockMaterial.isReplaceable() && World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }

    protected BlockPattern getSnowmanBasePattern() {
        if (this.snowmanBasePattern == null) {
            this.snowmanBasePattern = FactoryBlockPattern.start().aisle(" ", "#", "#").where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.snow))).build();
        }
        return this.snowmanBasePattern;
    }

    protected BlockPattern getSnowmanPattern() {
        if (this.snowmanPattern == null) {
            this.snowmanPattern = FactoryBlockPattern.start().aisle("^", "#", "#").where('^', BlockWorldState.hasState(field_181085_Q)).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.snow))).build();
        }
        return this.snowmanPattern;
    }

    protected BlockPattern getGolemBasePattern() {
        if (this.golemBasePattern == null) {
            this.golemBasePattern = FactoryBlockPattern.start().aisle("~ ~", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.iron_block))).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        }
        return this.golemBasePattern;
    }

    protected BlockPattern getGolemPattern() {
        if (this.golemPattern == null) {
            this.golemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~").where('^', BlockWorldState.hasState(field_181085_Q)).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.iron_block))).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        }
        return this.golemPattern;
    }
}

