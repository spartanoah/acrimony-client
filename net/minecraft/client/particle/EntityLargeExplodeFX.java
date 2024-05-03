/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityLargeExplodeFX
extends EntityFX {
    private static final ResourceLocation EXPLOSION_TEXTURE = new ResourceLocation("textures/entity/explosion.png");
    private static final VertexFormat field_181549_az = new VertexFormat().func_181721_a(DefaultVertexFormats.field_181713_m).func_181721_a(DefaultVertexFormats.field_181715_o).func_181721_a(DefaultVertexFormats.field_181714_n).func_181721_a(DefaultVertexFormats.field_181716_p).func_181721_a(DefaultVertexFormats.field_181717_q).func_181721_a(DefaultVertexFormats.field_181718_r);
    private int field_70581_a;
    private int field_70584_aq;
    private TextureManager theRenderEngine;
    private float field_70582_as;

    protected EntityLargeExplodeFX(TextureManager renderEngine, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i1213_9_, double p_i1213_11_, double p_i1213_13_) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0, 0.0, 0.0);
        this.theRenderEngine = renderEngine;
        this.field_70584_aq = 6 + this.rand.nextInt(4);
        this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.6f + 0.4f;
        this.particleRed = this.particleBlue;
        this.field_70582_as = 1.0f - (float)p_i1213_9_ * 0.5f;
    }

    @Override
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
        int i = (int)(((float)this.field_70581_a + partialTicks) * 15.0f / (float)this.field_70584_aq);
        if (i <= 15) {
            this.theRenderEngine.bindTexture(EXPLOSION_TEXTURE);
            float f = (float)(i % 4) / 4.0f;
            float f1 = f + 0.24975f;
            float f2 = (float)(i / 4) / 4.0f;
            float f3 = f2 + 0.24975f;
            float f4 = 2.0f * this.field_70582_as;
            float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
            float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
            float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            worldRendererIn.func_181668_a(7, field_181549_az);
            worldRendererIn.func_181662_b(f5 - p_180434_4_ * f4 - p_180434_7_ * f4, f6 - p_180434_5_ * f4, f7 - p_180434_6_ * f4 - p_180434_8_ * f4).func_181673_a(f1, f3).func_181666_a(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).func_181671_a(0, 240).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
            worldRendererIn.func_181662_b(f5 - p_180434_4_ * f4 + p_180434_7_ * f4, f6 + p_180434_5_ * f4, f7 - p_180434_6_ * f4 + p_180434_8_ * f4).func_181673_a(f1, f2).func_181666_a(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).func_181671_a(0, 240).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
            worldRendererIn.func_181662_b(f5 + p_180434_4_ * f4 + p_180434_7_ * f4, f6 + p_180434_5_ * f4, f7 + p_180434_6_ * f4 + p_180434_8_ * f4).func_181673_a(f, f2).func_181666_a(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).func_181671_a(0, 240).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
            worldRendererIn.func_181662_b(f5 + p_180434_4_ * f4 - p_180434_7_ * f4, f6 - p_180434_5_ * f4, f7 + p_180434_6_ * f4 - p_180434_8_ * f4).func_181673_a(f, f3).func_181666_a(this.particleRed, this.particleGreen, this.particleBlue, 1.0f).func_181671_a(0, 240).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
            Tessellator.getInstance().draw();
            GlStateManager.enableLighting();
        }
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 61680;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.field_70581_a;
        if (this.field_70581_a == this.field_70584_aq) {
            this.setDead();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    public static class Factory
    implements IParticleFactory {
        @Override
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int ... p_178902_15_) {
            return new EntityLargeExplodeFX(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}

