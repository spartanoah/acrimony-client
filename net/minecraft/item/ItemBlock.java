/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.viamcp.fixes.FixedSoundEngine;

public class ItemBlock
extends Item {
    protected final Block block;

    public ItemBlock(Block block) {
        this.block = block;
    }

    @Override
    public ItemBlock setUnlocalizedName(String unlocalizedName) {
        super.setUnlocalizedName(unlocalizedName);
        return this;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        return FixedSoundEngine.onItemUse(this, stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
    }

    public static boolean setTileEntityNBT(World worldIn, EntityPlayer pos, BlockPos stack, ItemStack p_179224_3_) {
        TileEntity tileentity;
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        if (minecraftserver == null) {
            return false;
        }
        if (p_179224_3_.hasTagCompound() && p_179224_3_.getTagCompound().hasKey("BlockEntityTag", 10) && (tileentity = worldIn.getTileEntity(stack)) != null) {
            if (!worldIn.isRemote && tileentity.func_183000_F() && !minecraftserver.getConfigurationManager().canSendCommands(pos.getGameProfile())) {
                return false;
            }
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttagcompound.copy();
            tileentity.writeToNBT(nbttagcompound);
            NBTTagCompound nbttagcompound2 = (NBTTagCompound)p_179224_3_.getTagCompound().getTag("BlockEntityTag");
            nbttagcompound.merge(nbttagcompound2);
            nbttagcompound.setInteger("x", stack.getX());
            nbttagcompound.setInteger("y", stack.getY());
            nbttagcompound.setInteger("z", stack.getZ());
            if (!nbttagcompound.equals(nbttagcompound1)) {
                tileentity.readFromNBT(nbttagcompound);
                tileentity.markDirty();
                return true;
            }
        }
        return false;
    }

    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block == Blocks.snow_layer) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }
        return worldIn.canBlockBePlaced(this.block, pos, false, side, null, stack);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.block.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return this.block.getUnlocalizedName();
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return this.block.getCreativeTabToDisplayOn();
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        this.block.getSubBlocks(itemIn, tab, subItems);
    }

    public Block getBlock() {
        return this.block;
    }
}

