/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.util.MathHelper;

public class GuiSimpleScrolledSelectionListProxy
extends GuiSlot {
    private final RealmsSimpleScrolledSelectionList field_178050_u;

    public GuiSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList p_i45525_1_, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.field_178050_u = p_i45525_1_;
    }

    @Override
    protected int getSize() {
        return this.field_178050_u.getItemCount();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.field_178050_u.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return this.field_178050_u.isSelectedItem(slotIndex);
    }

    @Override
    protected void drawBackground() {
        this.field_178050_u.renderBackground();
    }

    @Override
    protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
        this.field_178050_u.renderItem(entryID, p_180791_2_, p_180791_3_, p_180791_4_, mouseXIn, mouseYIn);
    }

    public int getWidth() {
        return this.width;
    }

    public int getMouseY() {
        return this.mouseY;
    }

    public int getMouseX() {
        return this.mouseX;
    }

    @Override
    protected int getContentHeight() {
        return this.field_178050_u.getMaxPosition();
    }

    @Override
    protected int getScrollBarX() {
        return this.field_178050_u.getScrollbarPosition();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {
        if (this.field_178041_q) {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;
            if (this.hasListHeader) {
                this.drawListHeader(k, l, tessellator);
            }
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
            GlStateManager.disableDepth();
            int i1 = 4;
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            int j1 = this.func_148135_f();
            if (j1 > 0) {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - (k1 = MathHelper.clamp_int(k1, 32, this.bottom - this.top - 8))) / j1 + this.top;
                if (l1 < this.top) {
                    l1 = this.top;
                }
                worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
                worldrenderer.func_181662_b(i, this.bottom, 0.0).func_181673_a(0.0, 1.0).func_181669_b(0, 0, 0, 255).func_181675_d();
                worldrenderer.func_181662_b(j, this.bottom, 0.0).func_181673_a(1.0, 1.0).func_181669_b(0, 0, 0, 255).func_181675_d();
                worldrenderer.func_181662_b(j, this.top, 0.0).func_181673_a(1.0, 0.0).func_181669_b(0, 0, 0, 255).func_181675_d();
                worldrenderer.func_181662_b(i, this.top, 0.0).func_181673_a(0.0, 0.0).func_181669_b(0, 0, 0, 255).func_181675_d();
                tessellator.draw();
                worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
                worldrenderer.func_181662_b(i, l1 + k1, 0.0).func_181673_a(0.0, 1.0).func_181669_b(128, 128, 128, 255).func_181675_d();
                worldrenderer.func_181662_b(j, l1 + k1, 0.0).func_181673_a(1.0, 1.0).func_181669_b(128, 128, 128, 255).func_181675_d();
                worldrenderer.func_181662_b(j, l1, 0.0).func_181673_a(1.0, 0.0).func_181669_b(128, 128, 128, 255).func_181675_d();
                worldrenderer.func_181662_b(i, l1, 0.0).func_181673_a(0.0, 0.0).func_181669_b(128, 128, 128, 255).func_181675_d();
                tessellator.draw();
                worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
                worldrenderer.func_181662_b(i, l1 + k1 - 1, 0.0).func_181673_a(0.0, 1.0).func_181669_b(192, 192, 192, 255).func_181675_d();
                worldrenderer.func_181662_b(j - 1, l1 + k1 - 1, 0.0).func_181673_a(1.0, 1.0).func_181669_b(192, 192, 192, 255).func_181675_d();
                worldrenderer.func_181662_b(j - 1, l1, 0.0).func_181673_a(1.0, 0.0).func_181669_b(192, 192, 192, 255).func_181675_d();
                worldrenderer.func_181662_b(i, l1, 0.0).func_181673_a(0.0, 0.0).func_181669_b(192, 192, 192, 255).func_181675_d();
                tessellator.draw();
            }
            this.func_148142_b(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }
}

