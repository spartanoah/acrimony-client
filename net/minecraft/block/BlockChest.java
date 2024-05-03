/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

public class BlockChest
extends BlockContainer {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public final int chestType;

    protected BlockChest(int type) {
        super(Material.wood);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.chestType = type;
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setBlockBounds(0.0625f, 0.0f, 0.0625f, 0.9375f, 0.875f, 0.9375f);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 2;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos.north()).getBlock() == this) {
            this.setBlockBounds(0.0625f, 0.0f, 0.0f, 0.9375f, 0.875f, 0.9375f);
        } else if (worldIn.getBlockState(pos.south()).getBlock() == this) {
            this.setBlockBounds(0.0625f, 0.0f, 0.0625f, 0.9375f, 0.875f, 1.0f);
        } else if (worldIn.getBlockState(pos.west()).getBlock() == this) {
            this.setBlockBounds(0.0f, 0.0f, 0.0625f, 0.9375f, 0.875f, 0.9375f);
        } else if (worldIn.getBlockState(pos.east()).getBlock() == this) {
            this.setBlockBounds(0.0625f, 0.0f, 0.0625f, 1.0f, 0.875f, 0.9375f);
        } else {
            this.setBlockBounds(0.0625f, 0.0f, 0.0625f, 0.9375f, 0.875f, 0.9375f);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.checkForSurroundingChests(worldIn, pos, state);
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(enumfacing);
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            if (iblockstate.getBlock() != this) continue;
            this.checkForSurroundingChests(worldIn, blockpos, iblockstate);
        }
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tileentity;
        boolean flag3;
        EnumFacing enumfacing = EnumFacing.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0f / 360.0f) + 0.5) & 3).getOpposite();
        state = state.withProperty(FACING, enumfacing);
        BlockPos blockpos = pos.north();
        BlockPos blockpos1 = pos.south();
        BlockPos blockpos2 = pos.west();
        BlockPos blockpos3 = pos.east();
        boolean flag = this == worldIn.getBlockState(blockpos).getBlock();
        boolean flag1 = this == worldIn.getBlockState(blockpos1).getBlock();
        boolean flag2 = this == worldIn.getBlockState(blockpos2).getBlock();
        boolean bl = flag3 = this == worldIn.getBlockState(blockpos3).getBlock();
        if (!(flag || flag1 || flag2 || flag3)) {
            worldIn.setBlockState(pos, state, 3);
        } else if (enumfacing.getAxis() != EnumFacing.Axis.X || !flag && !flag1) {
            if (enumfacing.getAxis() == EnumFacing.Axis.Z && (flag2 || flag3)) {
                if (flag2) {
                    worldIn.setBlockState(blockpos2, state, 3);
                } else {
                    worldIn.setBlockState(blockpos3, state, 3);
                }
                worldIn.setBlockState(pos, state, 3);
            }
        } else {
            if (flag) {
                worldIn.setBlockState(blockpos, state, 3);
            } else {
                worldIn.setBlockState(blockpos1, state, 3);
            }
            worldIn.setBlockState(pos, state, 3);
        }
        if (stack.hasDisplayName() && (tileentity = worldIn.getTileEntity(pos)) instanceof TileEntityChest) {
            ((TileEntityChest)tileentity).setCustomName(stack.getDisplayName());
        }
    }

    public IBlockState checkForSurroundingChests(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) {
            return state;
        }
        IBlockState iblockstate = worldIn.getBlockState(pos.north());
        IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
        IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
        IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
        EnumFacing enumfacing = state.getValue(FACING);
        Block block = iblockstate.getBlock();
        Block block1 = iblockstate1.getBlock();
        Block block2 = iblockstate2.getBlock();
        Block block3 = iblockstate3.getBlock();
        if (block != this && block1 != this) {
            boolean flag = block.isFullBlock();
            boolean flag1 = block1.isFullBlock();
            if (block2 == this || block3 == this) {
                BlockPos blockpos1 = block2 == this ? pos.west() : pos.east();
                IBlockState iblockstate6 = worldIn.getBlockState(blockpos1.north());
                IBlockState iblockstate7 = worldIn.getBlockState(blockpos1.south());
                enumfacing = EnumFacing.SOUTH;
                EnumFacing enumfacing2 = block2 == this ? iblockstate2.getValue(FACING) : iblockstate3.getValue(FACING);
                if (enumfacing2 == EnumFacing.NORTH) {
                    enumfacing = EnumFacing.NORTH;
                }
                Block block6 = iblockstate6.getBlock();
                Block block7 = iblockstate7.getBlock();
                if ((flag || block6.isFullBlock()) && !flag1 && !block7.isFullBlock()) {
                    enumfacing = EnumFacing.SOUTH;
                }
                if ((flag1 || block7.isFullBlock()) && !flag && !block6.isFullBlock()) {
                    enumfacing = EnumFacing.NORTH;
                }
            }
        } else {
            BlockPos blockpos = block == this ? pos.north() : pos.south();
            IBlockState iblockstate4 = worldIn.getBlockState(blockpos.west());
            IBlockState iblockstate5 = worldIn.getBlockState(blockpos.east());
            enumfacing = EnumFacing.EAST;
            EnumFacing enumfacing1 = block == this ? iblockstate.getValue(FACING) : iblockstate1.getValue(FACING);
            if (enumfacing1 == EnumFacing.WEST) {
                enumfacing = EnumFacing.WEST;
            }
            Block block4 = iblockstate4.getBlock();
            Block block5 = iblockstate5.getBlock();
            if ((block2.isFullBlock() || block4.isFullBlock()) && !block3.isFullBlock() && !block5.isFullBlock()) {
                enumfacing = EnumFacing.EAST;
            }
            if ((block3.isFullBlock() || block5.isFullBlock()) && !block2.isFullBlock() && !block4.isFullBlock()) {
                enumfacing = EnumFacing.WEST;
            }
        }
        state = state.withProperty(FACING, enumfacing);
        worldIn.setBlockState(pos, state, 3);
        return state;
    }

    public IBlockState correctFacing(World worldIn, BlockPos pos, IBlockState state) {
        EnumFacing enumfacing = null;
        for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
            IBlockState iblockstate = worldIn.getBlockState(pos.offset(enumfacing1));
            if (iblockstate.getBlock() == this) {
                return state;
            }
            if (!iblockstate.getBlock().isFullBlock()) continue;
            if (enumfacing != null) {
                enumfacing = null;
                break;
            }
            enumfacing = enumfacing1;
        }
        if (enumfacing != null) {
            return state.withProperty(FACING, enumfacing.getOpposite());
        }
        EnumFacing enumfacing2 = state.getValue(FACING);
        if (worldIn.getBlockState(pos.offset(enumfacing2)).getBlock().isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
        }
        if (worldIn.getBlockState(pos.offset(enumfacing2)).getBlock().isFullBlock()) {
            enumfacing2 = enumfacing2.rotateY();
        }
        if (worldIn.getBlockState(pos.offset(enumfacing2)).getBlock().isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
        }
        return state.withProperty(FACING, enumfacing2);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        int i = 0;
        BlockPos blockpos = pos.west();
        BlockPos blockpos1 = pos.east();
        BlockPos blockpos2 = pos.north();
        BlockPos blockpos3 = pos.south();
        if (worldIn.getBlockState(blockpos).getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos)) {
                return false;
            }
            ++i;
        }
        if (worldIn.getBlockState(blockpos1).getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos1)) {
                return false;
            }
            ++i;
        }
        if (worldIn.getBlockState(blockpos2).getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos2)) {
                return false;
            }
            ++i;
        }
        if (worldIn.getBlockState(blockpos3).getBlock() == this) {
            if (this.isDoubleChest(worldIn, blockpos3)) {
                return false;
            }
            ++i;
        }
        return i <= 1;
    }

    private boolean isDoubleChest(World worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).getBlock() != this) {
            return false;
        }
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() != this) continue;
            return true;
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityChest) {
            tileentity.updateContainingBlockInfo();
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)((Object)tileentity));
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }
        ILockableContainer ilockablecontainer = this.getLockableContainer(worldIn, pos);
        if (ilockablecontainer != null) {
            playerIn.displayGUIChest(ilockablecontainer);
            if (this.chestType == 0) {
                playerIn.triggerAchievement(StatList.field_181723_aa);
            } else if (this.chestType == 1) {
                playerIn.triggerAchievement(StatList.field_181737_U);
            }
        }
        return true;
    }

    public ILockableContainer getLockableContainer(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (!(tileentity instanceof TileEntityChest)) {
            return null;
        }
        ILockableContainer ilockablecontainer = (TileEntityChest)tileentity;
        if (this.isBlocked(worldIn, pos)) {
            return null;
        }
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(enumfacing);
            Block block = worldIn.getBlockState(blockpos).getBlock();
            if (block != this) continue;
            if (this.isBlocked(worldIn, blockpos)) {
                return null;
            }
            TileEntity tileentity1 = worldIn.getTileEntity(blockpos);
            if (!(tileentity1 instanceof TileEntityChest)) continue;
            if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH) {
                ilockablecontainer = new InventoryLargeChest("container.chestDouble", ilockablecontainer, (TileEntityChest)tileentity1);
                continue;
            }
            ilockablecontainer = new InventoryLargeChest("container.chestDouble", (TileEntityChest)tileentity1, ilockablecontainer);
        }
        return ilockablecontainer;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityChest();
    }

    @Override
    public boolean canProvidePower() {
        return this.chestType == 1;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        if (!this.canProvidePower()) {
            return 0;
        }
        int i = 0;
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityChest) {
            i = ((TileEntityChest)tileentity).numPlayersUsing;
        }
        return MathHelper.clamp_int(i, 0, 15);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        return side == EnumFacing.UP ? this.isProvidingWeakPower(worldIn, pos, state, side) : 0;
    }

    private boolean isBlocked(World worldIn, BlockPos pos) {
        return this.isBelowSolidBlock(worldIn, pos) || this.isOcelotSittingOnChest(worldIn, pos);
    }

    private boolean isBelowSolidBlock(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    private boolean isOcelotSittingOnChest(World worldIn, BlockPos pos) {
        for (Entity entity : worldIn.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            EntityOcelot entityocelot = (EntityOcelot)entity;
            if (!entityocelot.isSitting()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory(this.getLockableContainer(worldIn, pos));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }
}

