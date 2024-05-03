/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.Config;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public abstract class Render<T extends Entity>
implements IEntityRenderer {
    private static final ResourceLocation shadowTextures = new ResourceLocation("textures/misc/shadow.png");
    protected final RenderManager renderManager;
    public float shadowSize;
    protected float shadowOpaque = 1.0f;
    private Class entityClass = null;
    private ResourceLocation locationTextureCustom = null;

    protected Render(RenderManager renderManager) {
        this.renderManager = renderManager;
    }

    public boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ) {
        AxisAlignedBB axisalignedbb = ((Entity)livingEntity).getEntityBoundingBox();
        if (axisalignedbb.func_181656_b() || axisalignedbb.getAverageEdgeLength() == 0.0) {
            axisalignedbb = new AxisAlignedBB(((Entity)livingEntity).posX - 2.0, ((Entity)livingEntity).posY - 2.0, ((Entity)livingEntity).posZ - 2.0, ((Entity)livingEntity).posX + 2.0, ((Entity)livingEntity).posY + 2.0, ((Entity)livingEntity).posZ + 2.0);
        }
        return ((Entity)livingEntity).isInRangeToRender3d(camX, camY, camZ) && (((Entity)livingEntity).ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.renderName(entity, x, y, z);
    }

    protected void renderName(T entity, double x, double y, double z) {
        if (this.canRenderName(entity)) {
            this.renderLivingLabel(entity, ((Entity)entity).getDisplayName().getFormattedText(), x, y, z, 64);
        }
    }

    protected boolean canRenderName(T entity) {
        return ((Entity)entity).getAlwaysRenderNameTagForRender() && ((Entity)entity).hasCustomName();
    }

    protected void renderOffsetLivingLabel(T entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
        this.renderLivingLabel(entityIn, str, x, y, z, 64);
    }

    protected abstract ResourceLocation getEntityTexture(T var1);

    protected boolean bindEntityTexture(T entity) {
        ResourceLocation resourcelocation = this.getEntityTexture(entity);
        if (this.locationTextureCustom != null) {
            resourcelocation = this.locationTextureCustom;
        }
        if (resourcelocation == null) {
            return false;
        }
        this.bindTexture(resourcelocation);
        return true;
    }

    public void bindTexture(ResourceLocation location) {
        this.renderManager.renderEngine.bindTexture(location);
    }

    private void renderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks) {
        GlStateManager.disableLighting();
        TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
        TextureAtlasSprite textureatlassprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        float f = entity.width * 1.4f;
        GlStateManager.scale(f, f, f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f1 = 0.5f;
        float f2 = 0.0f;
        float f3 = entity.height / f;
        float f4 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(0.0f, 0.0f, -0.3f + (float)((int)f3) * 0.02f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        float f5 = 0.0f;
        int i = 0;
        boolean flag = Config.isMultiTexture();
        if (flag) {
            worldrenderer.setBlockLayer(EnumWorldBlockLayer.SOLID);
        }
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        while (f3 > 0.0f) {
            TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
            worldrenderer.setSprite(textureatlassprite2);
            this.bindTexture(TextureMap.locationBlocksTexture);
            float f6 = textureatlassprite2.getMinU();
            float f7 = textureatlassprite2.getMinV();
            float f8 = textureatlassprite2.getMaxU();
            float f9 = textureatlassprite2.getMaxV();
            if (i / 2 % 2 == 0) {
                float f10 = f8;
                f8 = f6;
                f6 = f10;
            }
            worldrenderer.func_181662_b(f1 - f2, 0.0f - f4, f5).func_181673_a(f8, f9).func_181675_d();
            worldrenderer.func_181662_b(-f1 - f2, 0.0f - f4, f5).func_181673_a(f6, f9).func_181675_d();
            worldrenderer.func_181662_b(-f1 - f2, 1.4f - f4, f5).func_181673_a(f6, f7).func_181675_d();
            worldrenderer.func_181662_b(f1 - f2, 1.4f - f4, f5).func_181673_a(f8, f7).func_181675_d();
            f3 -= 0.45f;
            f4 -= 0.45f;
            f1 *= 0.9f;
            f5 += 0.03f;
            ++i;
        }
        tessellator.draw();
        if (flag) {
            worldrenderer.setBlockLayer(null);
            GlStateManager.bindCurrentTexture();
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private void renderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        if (!Config.isShaders() || !Shaders.shouldSkipDefaultShadow) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            this.renderManager.renderEngine.bindTexture(shadowTextures);
            World world = this.getWorldFromRenderManager();
            GlStateManager.depthMask(false);
            float f = this.shadowSize;
            if (entityIn instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving)entityIn;
                f *= entityliving.getRenderSizeModifier();
                if (entityliving.isChild()) {
                    f *= 0.5f;
                }
            }
            double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
            double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
            double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
            int i = MathHelper.floor_double(d5 - (double)f);
            int j = MathHelper.floor_double(d5 + (double)f);
            int k = MathHelper.floor_double(d0 - (double)f);
            int l = MathHelper.floor_double(d0);
            int i1 = MathHelper.floor_double(d1 - (double)f);
            int j1 = MathHelper.floor_double(d1 + (double)f);
            double d2 = x - d5;
            double d3 = y - d0;
            double d4 = z - d1;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
            for (BlockPos blockPos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
                Block block = world.getBlockState(blockPos.down()).getBlock();
                if (block.getRenderType() == -1 || world.getLightFromNeighbors(blockPos) <= 3) continue;
                this.func_180549_a(block, x, y, z, blockPos, shadowAlpha, f, d2, d3, d4);
            }
            tessellator.draw();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    private World getWorldFromRenderManager() {
        return this.renderManager.worldObj;
    }

    private void func_180549_a(Block blockIn, double p_180549_2_, double p_180549_4_, double p_180549_6_, BlockPos pos, float p_180549_9_, float p_180549_10_, double p_180549_11_, double p_180549_13_, double p_180549_15_) {
        if (blockIn.isFullCube()) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            double d0 = ((double)p_180549_9_ - (p_180549_4_ - ((double)pos.getY() + p_180549_13_)) / 2.0) * 0.5 * (double)this.getWorldFromRenderManager().getLightBrightness(pos);
            if (d0 >= 0.0) {
                if (d0 > 1.0) {
                    d0 = 1.0;
                }
                double d1 = (double)pos.getX() + blockIn.getBlockBoundsMinX() + p_180549_11_;
                double d2 = (double)pos.getX() + blockIn.getBlockBoundsMaxX() + p_180549_11_;
                double d3 = (double)pos.getY() + blockIn.getBlockBoundsMinY() + p_180549_13_ + 0.015625;
                double d4 = (double)pos.getZ() + blockIn.getBlockBoundsMinZ() + p_180549_15_;
                double d5 = (double)pos.getZ() + blockIn.getBlockBoundsMaxZ() + p_180549_15_;
                float f = (float)((p_180549_2_ - d1) / 2.0 / (double)p_180549_10_ + 0.5);
                float f1 = (float)((p_180549_2_ - d2) / 2.0 / (double)p_180549_10_ + 0.5);
                float f2 = (float)((p_180549_6_ - d4) / 2.0 / (double)p_180549_10_ + 0.5);
                float f3 = (float)((p_180549_6_ - d5) / 2.0 / (double)p_180549_10_ + 0.5);
                worldrenderer.func_181662_b(d1, d3, d4).func_181673_a(f, f2).func_181666_a(1.0f, 1.0f, 1.0f, (float)d0).func_181675_d();
                worldrenderer.func_181662_b(d1, d3, d5).func_181673_a(f, f3).func_181666_a(1.0f, 1.0f, 1.0f, (float)d0).func_181675_d();
                worldrenderer.func_181662_b(d2, d3, d5).func_181673_a(f1, f3).func_181666_a(1.0f, 1.0f, 1.0f, (float)d0).func_181675_d();
                worldrenderer.func_181662_b(d2, d3, d4).func_181673_a(f1, f2).func_181666_a(1.0f, 1.0f, 1.0f, (float)d0).func_181675_d();
            }
        }
    }

    public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z) {
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        worldrenderer.setTranslation(x, y, z);
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181708_h);
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.minZ).func_181663_c(0.0f, 0.0f, -1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(0.0f, 0.0f, 1.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.minZ).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).func_181663_c(0.0f, -1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.minX, boundingBox.minY, boundingBox.minZ).func_181663_c(-1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        worldrenderer.func_181662_b(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        tessellator.draw();
        worldrenderer.setTranslation(0.0, 0.0, 0.0);
        GlStateManager.enableTexture2D();
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            double d0;
            float f;
            if (this.renderManager.options.field_181151_V && this.shadowSize > 0.0f && !entityIn.isInvisible() && this.renderManager.isRenderShadow() && (f = (float)((1.0 - (d0 = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ)) / 256.0) * (double)this.shadowOpaque)) > 0.0f) {
                this.renderShadow(entityIn, x, y, z, f, partialTicks);
            }
            if (!(!entityIn.canRenderOnFire() || entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).isSpectator())) {
                this.renderEntityOnFire(entityIn, x, y, z, partialTicks);
            }
        }
    }

    public FontRenderer getFontRendererFromRenderManager() {
        return this.renderManager.getFontRenderer();
    }

    protected void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance) {
        double d0 = ((Entity)entityIn).getDistanceSqToEntity(this.renderManager.livingPlayer);
        if (d0 <= (double)(maxDistance * maxDistance)) {
            FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
            float f = 1.6f;
            float f1 = 0.016666668f * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x + 0.0f, (float)y + ((Entity)entityIn).height + 0.5f, (float)z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-f1, -f1, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 0;
            if (str.equals("deadmau5")) {
                i = -10;
            }
            int j = fontrenderer.getStringWidth(str) / 2;
            GlStateManager.disableTexture2D();
            worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181706_f);
            worldrenderer.func_181662_b(-j - 1, -1 + i, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.25f).func_181675_d();
            worldrenderer.func_181662_b(-j - 1, 8 + i, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.25f).func_181675_d();
            worldrenderer.func_181662_b(j + 1, 8 + i, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.25f).func_181675_d();
            worldrenderer.func_181662_b(j + 1, -1 + i, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.25f).func_181675_d();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 0x20FFFFFF);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    public RenderManager getRenderManager() {
        return this.renderManager;
    }

    public boolean isMultipass() {
        return false;
    }

    public void renderMultipass(T p_renderMultipass_1_, double p_renderMultipass_2_, double p_renderMultipass_4_, double p_renderMultipass_6_, float p_renderMultipass_8_, float p_renderMultipass_9_) {
    }

    @Override
    public Class getEntityClass() {
        return this.entityClass;
    }

    @Override
    public void setEntityClass(Class p_setEntityClass_1_) {
        this.entityClass = p_setEntityClass_1_;
    }

    @Override
    public ResourceLocation getLocationTextureCustom() {
        return this.locationTextureCustom;
    }

    @Override
    public void setLocationTextureCustom(ResourceLocation p_setLocationTextureCustom_1_) {
        this.locationTextureCustom = p_setLocationTextureCustom_1_;
    }

    public static void setModelBipedMain(RenderBiped p_setModelBipedMain_0_, ModelBiped p_setModelBipedMain_1_) {
        p_setModelBipedMain_0_.modelBipedMain = p_setModelBipedMain_1_;
    }
}

