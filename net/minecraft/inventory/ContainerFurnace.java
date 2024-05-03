/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerFurnace
extends Container {
    private final IInventory tileFurnace;
    private int field_178152_f;
    private int field_178153_g;
    private int field_178154_h;
    private int field_178155_i;

    public ContainerFurnace(InventoryPlayer playerInventory, IInventory furnaceInventory) {
        this.tileFurnace = furnaceInventory;
        this.addSlotToContainer(new Slot(furnaceInventory, 0, 56, 17));
        this.addSlotToContainer(new SlotFurnaceFuel(furnaceInventory, 1, 56, 53));
        this.addSlotToContainer(new SlotFurnaceOutput(playerInventory.player, furnaceInventory, 2, 116, 35));
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
        listener.func_175173_a(this, this.tileFurnace);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < this.crafters.size(); ++i) {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);
            if (this.field_178152_f != this.tileFurnace.getField(2)) {
                icrafting.sendProgressBarUpdate(this, 2, this.tileFurnace.getField(2));
            }
            if (this.field_178154_h != this.tileFurnace.getField(0)) {
                icrafting.sendProgressBarUpdate(this, 0, this.tileFurnace.getField(0));
            }
            if (this.field_178155_i != this.tileFurnace.getField(1)) {
                icrafting.sendProgressBarUpdate(this, 1, this.tileFurnace.getField(1));
            }
            if (this.field_178153_g == this.tileFurnace.getField(3)) continue;
            icrafting.sendProgressBarUpdate(this, 3, this.tileFurnace.getField(3));
        }
        this.field_178152_f = this.tileFurnace.getField(2);
        this.field_178154_h = this.tileFurnace.getField(0);
        this.field_178155_i = this.tileFurnace.getField(1);
        this.field_178153_g = this.tileFurnace.getField(3);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        this.tileFurnace.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileFurnace.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 1 && index != 0 ? (FurnaceRecipes.instance().getSmeltingResult(itemstack1) != null ? !this.mergeItemStack(itemstack1, 0, 1, false) : (TileEntityFurnace.isItemFuel(itemstack1) ? !this.mergeItemStack(itemstack1, 1, 2, false) : (index >= 3 && index < 30 ? !this.mergeItemStack(itemstack1, 30, 39, false) : index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)))) : !this.mergeItemStack(itemstack1, 3, 39, false)) {
                return null;
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
}

