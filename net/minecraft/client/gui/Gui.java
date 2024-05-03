/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Gui {
    public static final ResourceLocation optionsBackground = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation statIcons = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation icons = new ResourceLocation("textures/gui/icons.png");
    protected float zLevel;

    protected void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }
        Gui.drawRect(startX, y, endX + 1, y + 1, color);
    }

    protected void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }
        Gui.drawRect(x, startY + 1, x + 1, endY, color);
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181705_e);
        worldrenderer.func_181662_b(left, bottom, 0.0).func_181675_d();
        worldrenderer.func_181662_b(right, bottom, 0.0).func_181675_d();
        worldrenderer.func_181662_b(right, top, 0.0).func_181675_d();
        worldrenderer.func_181662_b(left, top, 0.0).func_181675_d();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        worldrenderer.func_181662_b(right, top, this.zLevel).func_181666_a(f1, f2, f3, f).func_181675_d();
        worldrenderer.func_181662_b(left, top, this.zLevel).func_181666_a(f1, f2, f3, f).func_181675_d();
        worldrenderer.func_181662_b(left, bottom, this.zLevel).func_181666_a(f5, f6, f7, f4).func_181675_d();
        worldrenderer.func_181662_b(right, bottom, this.zLevel).func_181666_a(f5, f6, f7, f4).func_181675_d();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
    }

    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x, y, color);
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        float f = 0.00390625f;
        float f1 = 0.00390625f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(x + 0, y + height, this.zLevel).func_181673_a((float)(textureX + 0) * f, (float)(textureY + height) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y + height, this.zLevel).func_181673_a((float)(textureX + width) * f, (float)(textureY + height) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y + 0, this.zLevel).func_181673_a((float)(textureX + width) * f, (float)(textureY + 0) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + 0, y + 0, this.zLevel).func_181673_a((float)(textureX + 0) * f, (float)(textureY + 0) * f1).func_181675_d();
        tessellator.draw();
    }

    public void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV) {
        float f = 0.00390625f;
        float f1 = 0.00390625f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(xCoord + 0.0f, yCoord + (float)maxV, this.zLevel).func_181673_a((float)(minU + 0) * f, (float)(minV + maxV) * f1).func_181675_d();
        worldrenderer.func_181662_b(xCoord + (float)maxU, yCoord + (float)maxV, this.zLevel).func_181673_a((float)(minU + maxU) * f, (float)(minV + maxV) * f1).func_181675_d();
        worldrenderer.func_181662_b(xCoord + (float)maxU, yCoord + 0.0f, this.zLevel).func_181673_a((float)(minU + maxU) * f, (float)(minV + 0) * f1).func_181675_d();
        worldrenderer.func_181662_b(xCoord + 0.0f, yCoord + 0.0f, this.zLevel).func_181673_a((float)(minU + 0) * f, (float)(minV + 0) * f1).func_181675_d();
        tessellator.draw();
    }

    public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(xCoord + 0, yCoord + heightIn, this.zLevel).func_181673_a(textureSprite.getMinU(), textureSprite.getMaxV()).func_181675_d();
        worldrenderer.func_181662_b(xCoord + widthIn, yCoord + heightIn, this.zLevel).func_181673_a(textureSprite.getMaxU(), textureSprite.getMaxV()).func_181675_d();
        worldrenderer.func_181662_b(xCoord + widthIn, yCoord + 0, this.zLevel).func_181673_a(textureSprite.getMaxU(), textureSprite.getMinV()).func_181675_d();
        worldrenderer.func_181662_b(xCoord + 0, yCoord + 0, this.zLevel).func_181673_a(textureSprite.getMinU(), textureSprite.getMinV()).func_181675_d();
        tessellator.draw();
    }

    public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
        float f = 1.0f / textureWidth;
        float f1 = 1.0f / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(x, y + height, 0.0).func_181673_a(u * f, (v + (float)height) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y + height, 0.0).func_181673_a((u + (float)width) * f, (v + (float)height) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y, 0.0).func_181673_a((u + (float)width) * f, v * f1).func_181675_d();
        worldrenderer.func_181662_b(x, y, 0.0).func_181673_a(u * f, v * f1).func_181675_d();
        tessellator.draw();
    }

    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0f / tileWidth;
        float f1 = 1.0f / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(x, y + height, 0.0).func_181673_a(u * f, (v + (float)vHeight) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y + height, 0.0).func_181673_a((u + (float)uWidth) * f, (v + (float)vHeight) * f1).func_181675_d();
        worldrenderer.func_181662_b(x + width, y, 0.0).func_181673_a((u + (float)uWidth) * f, v * f1).func_181675_d();
        worldrenderer.func_181662_b(x, y, 0.0).func_181673_a(u * f, v * f1).func_181675_d();
        tessellator.draw();
    }
}

