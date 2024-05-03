/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class RenderBiped<T extends EntityLiving>
extends RenderLiving<T> {
    private static final ResourceLocation DEFAULT_RES_LOC = new ResourceLocation("textures/entity/steve.png");
    protected ModelBiped modelBipedMain;
    protected float field_77070_b;

    public RenderBiped(RenderManager renderManagerIn, ModelBiped modelBipedIn, float shadowSize) {
        this(renderManagerIn, modelBipedIn, shadowSize, 1.0f);
        this.addLayer(new LayerHeldItem(this));
    }

    public RenderBiped(RenderManager renderManagerIn, ModelBiped modelBipedIn, float shadowSize, float p_i46169_4_) {
        super(renderManagerIn, modelBipedIn, shadowSize);
        this.modelBipedMain = modelBipedIn;
        this.field_77070_b = p_i46169_4_;
        this.addLayer(new LayerCustomHead(modelBipedIn.bipedHead));
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return DEFAULT_RES_LOC;
    }

    @Override
    public void transformHeldFull3DItemLayer() {
        GlStateManager.translate(0.0f, 0.1875f, 0.0f);
    }
}

