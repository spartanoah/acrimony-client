/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.util.EnumChatFormatting;

public abstract class GuiResourcePackList
extends GuiListExtended {
    protected final Minecraft mc;
    protected final List<ResourcePackListEntry> field_148204_l;

    public GuiResourcePackList(Minecraft mcIn, int p_i45055_2_, int p_i45055_3_, List<ResourcePackListEntry> p_i45055_4_) {
        super(mcIn, p_i45055_2_, p_i45055_3_, 32, p_i45055_3_ - 55 + 4, 36);
        this.mc = mcIn;
        this.field_148204_l = p_i45055_4_;
        this.field_148163_i = false;
        this.setHasListHeader(true, (int)((float)mcIn.fontRendererObj.FONT_HEIGHT * 1.5f));
    }

    @Override
    protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
        String s = (Object)((Object)EnumChatFormatting.UNDERLINE) + "" + (Object)((Object)EnumChatFormatting.BOLD) + this.getListHeader();
        this.mc.fontRendererObj.drawString(s, p_148129_1_ + this.width / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2, Math.min(this.top + 3, p_148129_2_), 0xFFFFFF);
    }

    protected abstract String getListHeader();

    public List<ResourcePackListEntry> getList() {
        return this.field_148204_l;
    }

    @Override
    protected int getSize() {
        return this.getList().size();
    }

    @Override
    public ResourcePackListEntry getListEntry(int index) {
        return this.getList().get(index);
    }

    @Override
    public int getListWidth() {
        return this.width;
    }

    @Override
    protected int getScrollBarX() {
        return this.right - 6;
    }
}

