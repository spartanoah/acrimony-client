/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.client;

import Acrimony.util.IMinecraft;
import net.minecraft.item.ItemStack;

public class SlotSpoofHandler
implements IMinecraft {
    private int spoofedSlot;
    private boolean spoofing;

    public void startSpoofing(int slot) {
        this.spoofing = true;
        this.spoofedSlot = slot;
    }

    public void stopSpoofing() {
        this.spoofing = false;
    }

    public int getSpoofedSlot() {
        return this.spoofing ? this.spoofedSlot : SlotSpoofHandler.mc.thePlayer.inventory.currentItem;
    }

    public ItemStack getSpoofedStack() {
        return this.spoofing ? SlotSpoofHandler.mc.thePlayer.inventory.getStackInSlot(this.spoofedSlot) : SlotSpoofHandler.mc.thePlayer.inventory.getCurrentItem();
    }

    public boolean isSpoofing() {
        return this.spoofing;
    }
}

