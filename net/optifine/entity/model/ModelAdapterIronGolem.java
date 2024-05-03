/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderIronGolem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.entity.model.ModelAdapter;

public class ModelAdapterIronGolem
extends ModelAdapter {
    public ModelAdapterIronGolem() {
        super(EntityIronGolem.class, "iron_golem", 0.5f);
    }

    @Override
    public ModelBase makeModel() {
        return new ModelIronGolem();
    }

    @Override
    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (!(model instanceof ModelIronGolem)) {
            return null;
        }
        ModelIronGolem modelirongolem = (ModelIronGolem)model;
        return modelPart.equals("head") ? modelirongolem.ironGolemHead : (modelPart.equals("body") ? modelirongolem.ironGolemBody : (modelPart.equals("left_arm") ? modelirongolem.ironGolemLeftArm : (modelPart.equals("right_arm") ? modelirongolem.ironGolemRightArm : (modelPart.equals("left_leg") ? modelirongolem.ironGolemLeftLeg : (modelPart.equals("right_leg") ? modelirongolem.ironGolemRightLeg : null)))));
    }

    @Override
    public String[] getModelRendererNames() {
        return new String[]{"head", "body", "right_arm", "left_arm", "left_leg", "right_leg"};
    }

    @Override
    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        RenderIronGolem renderirongolem = new RenderIronGolem(rendermanager);
        renderirongolem.mainModel = modelBase;
        renderirongolem.shadowSize = shadowSize;
        return renderirongolem;
    }
}

