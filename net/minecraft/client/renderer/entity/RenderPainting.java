/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderPainting
extends Render<EntityPainting> {
    private static final ResourceLocation KRISTOFFER_PAINTING_TEXTURE = new ResourceLocation("textures/painting/paintings_kristoffer_zetterstrand.png");

    public RenderPainting(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityPainting entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180.0f - entityYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.enableRescaleNormal();
        this.bindEntityTexture(entity);
        EntityPainting.EnumArt entitypainting$enumart = entity.art;
        float f = 0.0625f;
        GlStateManager.scale(f, f, f);
        this.renderPainting(entity, entitypainting$enumart.sizeX, entitypainting$enumart.sizeY, entitypainting$enumart.offsetX, entitypainting$enumart.offsetY);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPainting entity) {
        return KRISTOFFER_PAINTING_TEXTURE;
    }

    private void renderPainting(EntityPainting painting, int width, int height, int textureU, int textureV) {
        float f = (float)(-width) / 2.0f;
        float f1 = (float)(-height) / 2.0f;
        float f2 = 0.5f;
        float f3 = 0.75f;
        float f4 = 0.8125f;
        float f5 = 0.0f;
        float f6 = 0.0625f;
        float f7 = 0.75f;
        float f8 = 0.8125f;
        float f9 = 0.001953125f;
        float f10 = 0.001953125f;
        float f11 = 0.7519531f;
        float f12 = 0.7519531f;
        float f13 = 0.0f;
        float f14 = 0.0625f;
        for (int i = 0; i < width / 16; ++i) {
            for (int j = 0; j < height / 16; ++j) {
                float f15 = f + (float)((i + 1) * 16);
                float f16 = f + (float)(i * 16);
                float f17 = f1 + (float)((j + 1) * 16);
                float f18 = f1 + (float)(j * 16);
                this.setLightmap(painting, (f15 + f16) / 2.0f, (f17 + f18) / 2.0f);
                float f19 = (float)(textureU + width - i * 16) / 256.0f;
                float f20 = (float)(textureU + width - (i + 1) * 16) / 256.0f;
                float f21 = (float)(textureV + height - j * 16) / 256.0f;
                float f22 = (float)(textureV + height - (j + 1) * 16) / 256.0f;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181710_j);
                worldrenderer.func_181662_b(f15, f18, -f2).func_181673_a(f20, f21).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, -f2).func_181673_a(f19, f21).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, -f2).func_181673_a(f19, f22).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, -f2).func_181673_a(f20, f22).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, f2).func_181673_a(f3, f5).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, f2).func_181673_a(f4, f5).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, f2).func_181673_a(f4, f6).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f18, f2).func_181673_a(f3, f6).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, -f2).func_181673_a(f7, f9).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, -f2).func_181673_a(f8, f9).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, f2).func_181673_a(f8, f10).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, f2).func_181673_a(f7, f10).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f18, f2).func_181673_a(f7, f9).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, f2).func_181673_a(f8, f9).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, -f2).func_181673_a(f8, f10).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f18, -f2).func_181673_a(f7, f10).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, f2).func_181673_a(f12, f13).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f18, f2).func_181673_a(f12, f14).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f18, -f2).func_181673_a(f11, f14).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f15, f17, -f2).func_181673_a(f11, f13).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, -f2).func_181673_a(f12, f13).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, -f2).func_181673_a(f12, f14).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f18, f2).func_181673_a(f11, f14).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
                worldrenderer.func_181662_b(f16, f17, f2).func_181673_a(f11, f13).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
                tessellator.draw();
            }
        }
    }

    private void setLightmap(EntityPainting painting, float p_77008_2_, float p_77008_3_) {
        int i = MathHelper.floor_double(painting.posX);
        int j = MathHelper.floor_double(painting.posY + (double)(p_77008_3_ / 16.0f));
        int k = MathHelper.floor_double(painting.posZ);
        EnumFacing enumfacing = painting.facingDirection;
        if (enumfacing == EnumFacing.NORTH) {
            i = MathHelper.floor_double(painting.posX + (double)(p_77008_2_ / 16.0f));
        }
        if (enumfacing == EnumFacing.WEST) {
            k = MathHelper.floor_double(painting.posZ - (double)(p_77008_2_ / 16.0f));
        }
        if (enumfacing == EnumFacing.SOUTH) {
            i = MathHelper.floor_double(painting.posX - (double)(p_77008_2_ / 16.0f));
        }
        if (enumfacing == EnumFacing.EAST) {
            k = MathHelper.floor_double(painting.posZ + (double)(p_77008_2_ / 16.0f));
        }
        int l = this.renderManager.worldObj.getCombinedLight(new BlockPos(i, j, k), 0);
        int i1 = l % 65536;
        int j1 = l / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i1, j1);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }
}

