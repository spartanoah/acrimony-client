/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

public class LayerSaddle
implements LayerRenderer<EntityPig> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/pig/pig_saddle.png");
    private final RenderPig pigRenderer;
    private final ModelPig pigModel = new ModelPig(0.5f);

    public LayerSaddle(RenderPig pigRendererIn) {
        this.pigRenderer = pigRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPig entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        if (entitylivingbaseIn.getSaddled()) {
            this.pigRenderer.bindTexture(TEXTURE);
            this.pigModel.setModelAttributes(this.pigRenderer.getMainModel());
            this.pigModel.render(entitylivingbaseIn, p_177141_2_, p_177141_3_, p_177141_5_, p_177141_6_, p_177141_7_, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}

