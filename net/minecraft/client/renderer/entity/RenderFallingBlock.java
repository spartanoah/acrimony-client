/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RenderFallingBlock
extends Render<EntityFallingBlock> {
    public RenderFallingBlock(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5f;
    }

    @Override
    public void doRender(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.getBlock() != null) {
            this.bindTexture(TextureMap.locationBlocksTexture);
            IBlockState iblockstate = entity.getBlock();
            Block block = iblockstate.getBlock();
            BlockPos blockpos = new BlockPos(entity);
            World world = entity.getWorldObj();
            if (iblockstate != world.getBlockState(blockpos) && block.getRenderType() != -1 && block.getRenderType() == 3) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)x, (float)y, (float)z);
                GlStateManager.disableLighting();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.func_181668_a(7, DefaultVertexFormats.BLOCK);
                int i = blockpos.getX();
                int j = blockpos.getY();
                int k = blockpos.getZ();
                worldrenderer.setTranslation((float)(-i) - 0.5f, -j, (float)(-k) - 0.5f);
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(iblockstate, world, null);
                blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
                worldrenderer.setTranslation(0.0, 0.0, 0.0);
                tessellator.draw();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFallingBlock entity) {
        return TextureMap.locationBlocksTexture;
    }
}

