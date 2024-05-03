/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiFlatPresets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;

public class GuiCreateFlatWorld
extends GuiScreen {
    private final GuiCreateWorld createWorldGui;
    private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
    private String flatWorldTitle;
    private String field_146394_i;
    private String field_146391_r;
    private Details createFlatWorldListSlotGui;
    private GuiButton field_146389_t;
    private GuiButton field_146388_u;
    private GuiButton field_146386_v;

    public GuiCreateFlatWorld(GuiCreateWorld createWorldGuiIn, String p_i1029_2_) {
        this.createWorldGui = createWorldGuiIn;
        this.func_146383_a(p_i1029_2_);
    }

    public String func_146384_e() {
        return this.theFlatGeneratorInfo.toString();
    }

    public void func_146383_a(String p_146383_1_) {
        this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(p_146383_1_);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.flatWorldTitle = I18n.format("createWorld.customize.flat.title", new Object[0]);
        this.field_146394_i = I18n.format("createWorld.customize.flat.tile", new Object[0]);
        this.field_146391_r = I18n.format("createWorld.customize.flat.height", new Object[0]);
        this.createFlatWorldListSlotGui = new Details();
        this.field_146389_t = new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer", new Object[0]) + " (NYI)");
        this.buttonList.add(this.field_146389_t);
        this.field_146388_u = new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer", new Object[0]) + " (NYI)");
        this.buttonList.add(this.field_146388_u);
        this.field_146386_v = new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer", new Object[0]));
        this.buttonList.add(this.field_146386_v);
        this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.field_146388_u.visible = false;
        this.field_146389_t.visible = false;
        this.theFlatGeneratorInfo.func_82645_d();
        this.func_146375_g();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.createFlatWorldListSlotGui.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        int i = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.field_148228_k - 1;
        if (button.id == 1) {
            this.mc.displayGuiScreen(this.createWorldGui);
        } else if (button.id == 0) {
            this.createWorldGui.chunkProviderSettingsJson = this.func_146384_e();
            this.mc.displayGuiScreen(this.createWorldGui);
        } else if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiFlatPresets(this));
        } else if (button.id == 4 && this.func_146382_i()) {
            this.theFlatGeneratorInfo.getFlatLayers().remove(i);
            this.createFlatWorldListSlotGui.field_148228_k = Math.min(this.createFlatWorldListSlotGui.field_148228_k, this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
        }
        this.theFlatGeneratorInfo.func_82645_d();
        this.func_146375_g();
    }

    public void func_146375_g() {
        boolean flag;
        this.field_146386_v.enabled = flag = this.func_146382_i();
        this.field_146388_u.enabled = flag;
        this.field_146388_u.enabled = false;
        this.field_146389_t.enabled = false;
    }

    private boolean func_146382_i() {
        return this.createFlatWorldListSlotGui.field_148228_k > -1 && this.createFlatWorldListSlotGui.field_148228_k < this.theFlatGeneratorInfo.getFlatLayers().size();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.flatWorldTitle, this.width / 2, 8, 0xFFFFFF);
        int i = this.width / 2 - 92 - 16;
        this.drawString(this.fontRendererObj, this.field_146394_i, i, 32, 0xFFFFFF);
        this.drawString(this.fontRendererObj, this.field_146391_r, i + 2 + 213 - this.fontRendererObj.getStringWidth(this.field_146391_r), 32, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class Details
    extends GuiSlot {
        public int field_148228_k;

        public Details() {
            super(GuiCreateFlatWorld.this.mc, GuiCreateFlatWorld.this.width, GuiCreateFlatWorld.this.height, 43, GuiCreateFlatWorld.this.height - 60, 24);
            this.field_148228_k = -1;
        }

        private void func_148225_a(int p_148225_1_, int p_148225_2_, ItemStack p_148225_3_) {
            this.func_148226_e(p_148225_1_ + 1, p_148225_2_ + 1);
            GlStateManager.enableRescaleNormal();
            if (p_148225_3_ != null && p_148225_3_.getItem() != null) {
                RenderHelper.enableGUIStandardItemLighting();
                GuiCreateFlatWorld.this.itemRender.renderItemIntoGUI(p_148225_3_, p_148225_1_ + 2, p_148225_2_ + 2);
                RenderHelper.disableStandardItemLighting();
            }
            GlStateManager.disableRescaleNormal();
        }

        private void func_148226_e(int p_148226_1_, int p_148226_2_) {
            this.func_148224_c(p_148226_1_, p_148226_2_, 0, 0);
        }

        private void func_148224_c(int p_148224_1_, int p_148224_2_, int p_148224_3_, int p_148224_4_) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.mc.getTextureManager().bindTexture(Gui.statIcons);
            float f = 0.0078125f;
            float f1 = 0.0078125f;
            int i = 18;
            int j = 18;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
            worldrenderer.func_181662_b(p_148224_1_ + 0, p_148224_2_ + 18, GuiCreateFlatWorld.this.zLevel).func_181673_a((float)(p_148224_3_ + 0) * 0.0078125f, (float)(p_148224_4_ + 18) * 0.0078125f).func_181675_d();
            worldrenderer.func_181662_b(p_148224_1_ + 18, p_148224_2_ + 18, GuiCreateFlatWorld.this.zLevel).func_181673_a((float)(p_148224_3_ + 18) * 0.0078125f, (float)(p_148224_4_ + 18) * 0.0078125f).func_181675_d();
            worldrenderer.func_181662_b(p_148224_1_ + 18, p_148224_2_ + 0, GuiCreateFlatWorld.this.zLevel).func_181673_a((float)(p_148224_3_ + 18) * 0.0078125f, (float)(p_148224_4_ + 0) * 0.0078125f).func_181675_d();
            worldrenderer.func_181662_b(p_148224_1_ + 0, p_148224_2_ + 0, GuiCreateFlatWorld.this.zLevel).func_181673_a((float)(p_148224_3_ + 0) * 0.0078125f, (float)(p_148224_4_ + 0) * 0.0078125f).func_181675_d();
            tessellator.draw();
        }

        @Override
        protected int getSize() {
            return GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            this.field_148228_k = slotIndex;
            GuiCreateFlatWorld.this.func_146375_g();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == this.field_148228_k;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            String s;
            FlatLayerInfo flatlayerinfo = GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().get(GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
            IBlockState iblockstate = flatlayerinfo.func_175900_c();
            Block block = iblockstate.getBlock();
            Item item = Item.getItemFromBlock(block);
            ItemStack itemstack = block != Blocks.air && item != null ? new ItemStack(item, 1, block.getMetaFromState(iblockstate)) : null;
            String string = s = itemstack == null ? "Air" : item.getItemStackDisplayName(itemstack);
            if (item == null) {
                if (block != Blocks.water && block != Blocks.flowing_water) {
                    if (block == Blocks.lava || block == Blocks.flowing_lava) {
                        item = Items.lava_bucket;
                    }
                } else {
                    item = Items.water_bucket;
                }
                if (item != null) {
                    itemstack = new ItemStack(item, 1, block.getMetaFromState(iblockstate));
                    s = block.getLocalizedName();
                }
            }
            this.func_148225_a(p_180791_2_, p_180791_3_, itemstack);
            GuiCreateFlatWorld.this.fontRendererObj.drawString(s, p_180791_2_ + 18 + 5, p_180791_3_ + 3, 0xFFFFFF);
            String s1 = entryID == 0 ? I18n.format("createWorld.customize.flat.layer.top", flatlayerinfo.getLayerCount()) : (entryID == GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - 1 ? I18n.format("createWorld.customize.flat.layer.bottom", flatlayerinfo.getLayerCount()) : I18n.format("createWorld.customize.flat.layer", flatlayerinfo.getLayerCount()));
            GuiCreateFlatWorld.this.fontRendererObj.drawString(s1, p_180791_2_ + 2 + 213 - GuiCreateFlatWorld.this.fontRendererObj.getStringWidth(s1), p_180791_3_ + 3, 0xFFFFFF);
        }

        @Override
        protected int getScrollBarX() {
            return this.width - 70;
        }
    }
}

