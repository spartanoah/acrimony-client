/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryLanDetected;
import net.minecraft.client.gui.ServerListEntryLanScan;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;

public class ServerSelectionList
extends GuiListExtended {
    private final GuiMultiplayer owner;
    private final List<ServerListEntryNormal> field_148198_l = Lists.newArrayList();
    private final List<ServerListEntryLanDetected> field_148199_m = Lists.newArrayList();
    private final GuiListExtended.IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
    private int selectedSlotIndex = -1;

    public ServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.owner = ownerIn;
    }

    @Override
    public GuiListExtended.IGuiListEntry getListEntry(int index) {
        if (index < this.field_148198_l.size()) {
            return this.field_148198_l.get(index);
        }
        if ((index -= this.field_148198_l.size()) == 0) {
            return this.lanScanEntry;
        }
        return this.field_148199_m.get(--index);
    }

    @Override
    protected int getSize() {
        return this.field_148198_l.size() + 1 + this.field_148199_m.size();
    }

    public void setSelectedSlotIndex(int selectedSlotIndexIn) {
        this.selectedSlotIndex = selectedSlotIndexIn;
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return slotIndex == this.selectedSlotIndex;
    }

    public int func_148193_k() {
        return this.selectedSlotIndex;
    }

    public void func_148195_a(ServerList p_148195_1_) {
        this.field_148198_l.clear();
        for (int i = 0; i < p_148195_1_.countServers(); ++i) {
            this.field_148198_l.add(new ServerListEntryNormal(this.owner, p_148195_1_.getServerData(i)));
        }
    }

    public void func_148194_a(List<LanServerDetector.LanServer> p_148194_1_) {
        this.field_148199_m.clear();
        for (LanServerDetector.LanServer lanserverdetector$lanserver : p_148194_1_) {
            this.field_148199_m.add(new ServerListEntryLanDetected(this.owner, lanserverdetector$lanserver));
        }
    }

    @Override
    protected int getScrollBarX() {
        return super.getScrollBarX() + 30;
    }

    @Override
    public int getListWidth() {
        return super.getListWidth() + 85;
    }
}

