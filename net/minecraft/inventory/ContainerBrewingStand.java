/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;

public class ContainerBrewingStand
extends Container {
    private IInventory tileBrewingStand;
    private final Slot theSlot;
    private int brewTime;

    public ContainerBrewingStand(InventoryPlayer playerInventory, IInventory tileBrewingStandIn) {
        this.tileBrewingStand = tileBrewingStandIn;
        this.addSlotToContainer(new Potion(playerInventory.player, tileBrewingStandIn, 0, 56, 46));
        this.addSlotToContainer(new Potion(playerInventory.player, tileBrewingStandIn, 1, 79, 53));
        this.addSlotToContainer(new Potion(playerInventory.player, tileBrewingStandIn, 2, 102, 46));
        this.theSlot = this.addSlotToContainer(new Ingredient(tileBrewingStandIn, 3, 79, 17));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void onCraftGuiOpened(ICrafting listener) {
        super.onCraftGuiOpened(listener);
        listener.func_175173_a(this, this.tileBrewingStand);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < this.crafters.size(); ++i) {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);
            if (this.brewTime == this.tileBrewingStand.getField(0)) continue;
            icrafting.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
        }
        this.brewTime = this.tileBrewingStand.getField(0);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        this.tileBrewingStand.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileBrewingStand.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if ((index < 0 || index > 2) && index != 3) {
                if (!this.theSlot.getHasStack() && this.theSlot.isItemValid(itemstack1) ? !this.mergeItemStack(itemstack1, 3, 4, false) : (Potion.canHoldPotion(itemstack) ? !this.mergeItemStack(itemstack1, 0, 3, false) : (index >= 4 && index < 31 ? !this.mergeItemStack(itemstack1, 31, 40, false) : (index >= 31 && index < 40 ? !this.mergeItemStack(itemstack1, 4, 31, false) : !this.mergeItemStack(itemstack1, 4, 40, false))))) {
                    return null;
                }
            } else {
                if (!this.mergeItemStack(itemstack1, 4, 40, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(playerIn, itemstack1);
        }
        return itemstack;
    }

    static class Potion
    extends Slot {
        private EntityPlayer player;

        public Potion(EntityPlayer playerIn, IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
            this.player = playerIn;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return Potion.canHoldPotion(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Override
        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
            if (stack.getItem() == Items.potionitem && stack.getMetadata() > 0) {
                this.player.triggerAchievement(AchievementList.potion);
            }
            super.onPickupFromSlot(playerIn, stack);
        }

        public static boolean canHoldPotion(ItemStack stack) {
            return stack != null && (stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle);
        }
    }

    class Ingredient
    extends Slot {
        public Ingredient(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null ? stack.getItem().isPotionIngredient(stack) : false;
        }

        @Override
        public int getSlotStackLimit() {
            return 64;
        }
    }
}

