/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

public class ModelArmorStandArmor
extends ModelBiped {
    public ModelArmorStandArmor() {
        this(0.0f);
    }

    public ModelArmorStandArmor(float modelSize) {
        this(modelSize, 64, 32);
    }

    protected ModelArmorStandArmor(float modelSize, int textureWidthIn, int textureHeightIn) {
        super(modelSize, 0.0f, textureWidthIn, textureHeightIn);
    }

    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn) {
        if (entityIn instanceof EntityArmorStand) {
            EntityArmorStand entityarmorstand = (EntityArmorStand)entityIn;
            this.bipedHead.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getHeadRotation().getX();
            this.bipedHead.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getHeadRotation().getY();
            this.bipedHead.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getHeadRotation().getZ();
            this.bipedHead.setRotationPoint(0.0f, 1.0f, 0.0f);
            this.bipedBody.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getBodyRotation().getX();
            this.bipedBody.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getBodyRotation().getY();
            this.bipedBody.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getBodyRotation().getZ();
            this.bipedLeftArm.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getLeftArmRotation().getX();
            this.bipedLeftArm.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getLeftArmRotation().getY();
            this.bipedLeftArm.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getLeftArmRotation().getZ();
            this.bipedRightArm.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getRightArmRotation().getX();
            this.bipedRightArm.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getRightArmRotation().getY();
            this.bipedRightArm.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getRightArmRotation().getZ();
            this.bipedLeftLeg.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getLeftLegRotation().getX();
            this.bipedLeftLeg.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getLeftLegRotation().getY();
            this.bipedLeftLeg.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getLeftLegRotation().getZ();
            this.bipedLeftLeg.setRotationPoint(1.9f, 11.0f, 0.0f);
            this.bipedRightLeg.rotateAngleX = (float)Math.PI / 180 * entityarmorstand.getRightLegRotation().getX();
            this.bipedRightLeg.rotateAngleY = (float)Math.PI / 180 * entityarmorstand.getRightLegRotation().getY();
            this.bipedRightLeg.rotateAngleZ = (float)Math.PI / 180 * entityarmorstand.getRightLegRotation().getZ();
            this.bipedRightLeg.setRotationPoint(-1.9f, 11.0f, 0.0f);
            ModelArmorStandArmor.copyModelAngles(this.bipedHead, this.bipedHeadwear);
        }
    }
}

