/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.tileentity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class TileEntityHopper
extends TileEntityLockable
implements IHopper,
ITickable {
    private ItemStack[] inventory = new ItemStack[5];
    private String customName;
    private int transferCooldown = -1;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.inventory = new ItemStack[this.getSizeInventory()];
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
        this.transferCooldown = compound.getInteger("TransferCooldown");
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            byte j = nbttagcompound.getByte("Slot");
            if (j < 0 || j >= this.inventory.length) continue;
            this.inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] == null) continue;
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.inventory[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("Items", nbttaglist);
        compound.setInteger("TransferCooldown", this.transferCooldown);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public int getSizeInventory() {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (this.inventory[index] != null) {
            if (this.inventory[index].stackSize <= count) {
                ItemStack itemstack1 = this.inventory[index];
                this.inventory[index] = null;
                return itemstack1;
            }
            ItemStack itemstack = this.inventory[index].splitStack(count);
            if (this.inventory[index].stackSize == 0) {
                this.inventory[index] = null;
            }
            return itemstack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (this.inventory[index] != null) {
            ItemStack itemstack = this.inventory[index];
            this.inventory[index] = null;
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory[index] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getCommandSenderName() {
        return this.hasCustomName() ? this.customName : "container.hopper";
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setCustomName(String customNameIn) {
        this.customName = customNameIn;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void update() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            --this.transferCooldown;
            if (!this.isOnTransferCooldown()) {
                this.setTransferCooldown(0);
                this.updateHopper();
            }
        }
    }

    public boolean updateHopper() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata())) {
                boolean flag = false;
                if (!this.isEmpty()) {
                    flag = this.transferItemsOut();
                }
                if (!this.isFull()) {
                    boolean bl = flag = TileEntityHopper.captureDroppedItems(this) || flag;
                }
                if (flag) {
                    this.setTransferCooldown(8);
                    this.markDirty();
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (itemstack == null) continue;
            return false;
        }
        return true;
    }

    private boolean isFull() {
        for (ItemStack itemstack : this.inventory) {
            if (itemstack != null && itemstack.stackSize == itemstack.getMaxStackSize()) continue;
            return false;
        }
        return true;
    }

    private boolean transferItemsOut() {
        IInventory iinventory = this.getInventoryForHopperTransfer();
        if (iinventory == null) {
            return false;
        }
        EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();
        if (this.isInventoryFull(iinventory, enumfacing)) {
            return false;
        }
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            if (this.getStackInSlot(i) == null) continue;
            ItemStack itemstack = this.getStackInSlot(i).copy();
            ItemStack itemstack1 = TileEntityHopper.putStackInInventoryAllSlots(iinventory, this.decrStackSize(i, 1), enumfacing);
            if (itemstack1 == null || itemstack1.stackSize == 0) {
                iinventory.markDirty();
                return true;
            }
            this.setInventorySlotContents(i, itemstack);
        }
        return false;
    }

    private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);
            for (int k = 0; k < aint.length; ++k) {
                ItemStack itemstack1 = isidedinventory.getStackInSlot(aint[k]);
                if (itemstack1 != null && itemstack1.stackSize == itemstack1.getMaxStackSize()) continue;
                return false;
            }
        } else {
            int i = inventoryIn.getSizeInventory();
            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = inventoryIn.getStackInSlot(j);
                if (itemstack != null && itemstack.stackSize == itemstack.getMaxStackSize()) continue;
                return false;
            }
        }
        return true;
    }

    private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);
            for (int i = 0; i < aint.length; ++i) {
                if (isidedinventory.getStackInSlot(aint[i]) == null) continue;
                return false;
            }
        } else {
            int j = inventoryIn.getSizeInventory();
            for (int k = 0; k < j; ++k) {
                if (inventoryIn.getStackInSlot(k) == null) continue;
                return false;
            }
        }
        return true;
    }

    public static boolean captureDroppedItems(IHopper p_145891_0_) {
        IInventory iinventory = TileEntityHopper.getHopperInventory(p_145891_0_);
        if (iinventory != null) {
            EnumFacing enumfacing = EnumFacing.DOWN;
            if (TileEntityHopper.isInventoryEmpty(iinventory, enumfacing)) {
                return false;
            }
            if (iinventory instanceof ISidedInventory) {
                ISidedInventory isidedinventory = (ISidedInventory)iinventory;
                int[] aint = isidedinventory.getSlotsForFace(enumfacing);
                for (int i = 0; i < aint.length; ++i) {
                    if (!TileEntityHopper.pullItemFromSlot(p_145891_0_, iinventory, aint[i], enumfacing)) continue;
                    return true;
                }
            } else {
                int j = iinventory.getSizeInventory();
                for (int k = 0; k < j; ++k) {
                    if (!TileEntityHopper.pullItemFromSlot(p_145891_0_, iinventory, k, enumfacing)) continue;
                    return true;
                }
            }
        } else {
            for (EntityItem entityitem : TileEntityHopper.func_181556_a(p_145891_0_.getWorld(), p_145891_0_.getXPos(), p_145891_0_.getYPos() + 1.0, p_145891_0_.getZPos())) {
                if (!TileEntityHopper.putDropInInventoryAllSlots(p_145891_0_, entityitem)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction) {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);
        if (itemstack != null && TileEntityHopper.canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = TileEntityHopper.putStackInInventoryAllSlots(hopper, inventoryIn.decrStackSize(index, 1), null);
            if (itemstack2 == null || itemstack2.stackSize == 0) {
                inventoryIn.markDirty();
                return true;
            }
            inventoryIn.setInventorySlotContents(index, itemstack1);
        }
        return false;
    }

    public static boolean putDropInInventoryAllSlots(IInventory p_145898_0_, EntityItem itemIn) {
        boolean flag = false;
        if (itemIn == null) {
            return false;
        }
        ItemStack itemstack = itemIn.getEntityItem().copy();
        ItemStack itemstack1 = TileEntityHopper.putStackInInventoryAllSlots(p_145898_0_, itemstack, null);
        if (itemstack1 != null && itemstack1.stackSize != 0) {
            itemIn.setEntityItemStack(itemstack1);
        } else {
            flag = true;
            itemIn.setDead();
        }
        return flag;
    }

    public static ItemStack putStackInInventoryAllSlots(IInventory inventoryIn, ItemStack stack, EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory && side != null) {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);
            for (int k = 0; k < aint.length && stack != null && stack.stackSize > 0; ++k) {
                stack = TileEntityHopper.insertStack(inventoryIn, stack, aint[k], side);
            }
        } else {
            int i = inventoryIn.getSizeInventory();
            for (int j = 0; j < i && stack != null && stack.stackSize > 0; ++j) {
                stack = TileEntityHopper.insertStack(inventoryIn, stack, j, side);
            }
        }
        if (stack != null && stack.stackSize == 0) {
            stack = null;
        }
        return stack;
    }

    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
        return !inventoryIn.isItemValidForSlot(index, stack) ? false : !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
    }

    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }

    private static ItemStack insertStack(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);
        if (TileEntityHopper.canInsertItemInSlot(inventoryIn, stack, index, side)) {
            boolean flag = false;
            if (itemstack == null) {
                inventoryIn.setInventorySlotContents(index, stack);
                stack = null;
                flag = true;
            } else if (TileEntityHopper.canCombine(itemstack, stack)) {
                int i = stack.getMaxStackSize() - itemstack.stackSize;
                int j = Math.min(stack.stackSize, i);
                stack.stackSize -= j;
                itemstack.stackSize += j;
                boolean bl = flag = j > 0;
            }
            if (flag) {
                if (inventoryIn instanceof TileEntityHopper) {
                    TileEntityHopper tileentityhopper = (TileEntityHopper)inventoryIn;
                    if (tileentityhopper.mayTransfer()) {
                        tileentityhopper.setTransferCooldown(8);
                    }
                    inventoryIn.markDirty();
                }
                inventoryIn.markDirty();
            }
        }
        return stack;
    }

    private IInventory getInventoryForHopperTransfer() {
        EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
        return TileEntityHopper.getInventoryAtPosition(this.getWorld(), this.pos.getX() + enumfacing.getFrontOffsetX(), this.pos.getY() + enumfacing.getFrontOffsetY(), this.pos.getZ() + enumfacing.getFrontOffsetZ());
    }

    public static IInventory getHopperInventory(IHopper hopper) {
        return TileEntityHopper.getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0, hopper.getZPos());
    }

    public static List<EntityItem> func_181556_a(World p_181556_0_, double p_181556_1_, double p_181556_3_, double p_181556_5_) {
        return p_181556_0_.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_181556_1_ - 0.5, p_181556_3_ - 0.5, p_181556_5_ - 0.5, p_181556_1_ + 0.5, p_181556_3_ + 0.5, p_181556_5_ + 0.5), EntitySelectors.selectAnything);
    }

    public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z) {
        List<Entity> list;
        TileEntity tileentity;
        int k;
        int j;
        IInventory iinventory = null;
        int i = MathHelper.floor_double(x);
        BlockPos blockpos = new BlockPos(i, j = MathHelper.floor_double(y), k = MathHelper.floor_double(z));
        Block block = worldIn.getBlockState(blockpos).getBlock();
        if (block.hasTileEntity() && (tileentity = worldIn.getTileEntity(blockpos)) instanceof IInventory && (iinventory = (IInventory)((Object)tileentity)) instanceof TileEntityChest && block instanceof BlockChest) {
            iinventory = ((BlockChest)block).getLockableContainer(worldIn, blockpos);
        }
        if (iinventory == null && (list = worldIn.getEntitiesInAABBexcluding(null, new AxisAlignedBB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntitySelectors.selectInventories)).size() > 0) {
            iinventory = (IInventory)((Object)list.get(worldIn.rand.nextInt(list.size())));
        }
        return iinventory;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() != stack2.getItem() ? false : (stack1.getMetadata() != stack2.getMetadata() ? false : (stack1.stackSize > stack1.getMaxStackSize() ? false : ItemStack.areItemStackTagsEqual(stack1, stack2)));
    }

    @Override
    public double getXPos() {
        return (double)this.pos.getX() + 0.5;
    }

    @Override
    public double getYPos() {
        return (double)this.pos.getY() + 0.5;
    }

    @Override
    public double getZPos() {
        return (double)this.pos.getZ() + 0.5;
    }

    public void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    public boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    public boolean mayTransfer() {
        return this.transferCooldown <= 1;
    }

    @Override
    public String getGuiID() {
        return "minecraft:hopper";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerHopper(playerInventory, this, playerIn);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.inventory.length; ++i) {
            this.inventory[i] = null;
        }
    }
}

