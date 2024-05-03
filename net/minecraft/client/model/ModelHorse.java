/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;

public class ModelHorse
extends ModelBase {
    private ModelRenderer head;
    private ModelRenderer field_178711_b;
    private ModelRenderer field_178712_c;
    private ModelRenderer horseLeftEar;
    private ModelRenderer horseRightEar;
    private ModelRenderer muleLeftEar;
    private ModelRenderer muleRightEar;
    private ModelRenderer neck;
    private ModelRenderer horseFaceRopes;
    private ModelRenderer mane;
    private ModelRenderer body;
    private ModelRenderer tailBase;
    private ModelRenderer tailMiddle;
    private ModelRenderer tailTip;
    private ModelRenderer backLeftLeg;
    private ModelRenderer backLeftShin;
    private ModelRenderer backLeftHoof;
    private ModelRenderer backRightLeg;
    private ModelRenderer backRightShin;
    private ModelRenderer backRightHoof;
    private ModelRenderer frontLeftLeg;
    private ModelRenderer frontLeftShin;
    private ModelRenderer frontLeftHoof;
    private ModelRenderer frontRightLeg;
    private ModelRenderer frontRightShin;
    private ModelRenderer frontRightHoof;
    private ModelRenderer muleLeftChest;
    private ModelRenderer muleRightChest;
    private ModelRenderer horseSaddleBottom;
    private ModelRenderer horseSaddleFront;
    private ModelRenderer horseSaddleBack;
    private ModelRenderer horseLeftSaddleRope;
    private ModelRenderer horseLeftSaddleMetal;
    private ModelRenderer horseRightSaddleRope;
    private ModelRenderer horseRightSaddleMetal;
    private ModelRenderer horseLeftFaceMetal;
    private ModelRenderer horseRightFaceMetal;
    private ModelRenderer horseLeftRein;
    private ModelRenderer horseRightRein;

    public ModelHorse() {
        this.textureWidth = 128;
        this.textureHeight = 128;
        this.body = new ModelRenderer(this, 0, 34);
        this.body.addBox(-5.0f, -8.0f, -19.0f, 10, 10, 24);
        this.body.setRotationPoint(0.0f, 11.0f, 9.0f);
        this.tailBase = new ModelRenderer(this, 44, 0);
        this.tailBase.addBox(-1.0f, -1.0f, 0.0f, 2, 2, 3);
        this.tailBase.setRotationPoint(0.0f, 3.0f, 14.0f);
        this.setBoxRotation(this.tailBase, -1.134464f, 0.0f, 0.0f);
        this.tailMiddle = new ModelRenderer(this, 38, 7);
        this.tailMiddle.addBox(-1.5f, -2.0f, 3.0f, 3, 4, 7);
        this.tailMiddle.setRotationPoint(0.0f, 3.0f, 14.0f);
        this.setBoxRotation(this.tailMiddle, -1.134464f, 0.0f, 0.0f);
        this.tailTip = new ModelRenderer(this, 24, 3);
        this.tailTip.addBox(-1.5f, -4.5f, 9.0f, 3, 4, 7);
        this.tailTip.setRotationPoint(0.0f, 3.0f, 14.0f);
        this.setBoxRotation(this.tailTip, -1.40215f, 0.0f, 0.0f);
        this.backLeftLeg = new ModelRenderer(this, 78, 29);
        this.backLeftLeg.addBox(-2.5f, -2.0f, -2.5f, 4, 9, 5);
        this.backLeftLeg.setRotationPoint(4.0f, 9.0f, 11.0f);
        this.backLeftShin = new ModelRenderer(this, 78, 43);
        this.backLeftShin.addBox(-2.0f, 0.0f, -1.5f, 3, 5, 3);
        this.backLeftShin.setRotationPoint(4.0f, 16.0f, 11.0f);
        this.backLeftHoof = new ModelRenderer(this, 78, 51);
        this.backLeftHoof.addBox(-2.5f, 5.1f, -2.0f, 4, 3, 4);
        this.backLeftHoof.setRotationPoint(4.0f, 16.0f, 11.0f);
        this.backRightLeg = new ModelRenderer(this, 96, 29);
        this.backRightLeg.addBox(-1.5f, -2.0f, -2.5f, 4, 9, 5);
        this.backRightLeg.setRotationPoint(-4.0f, 9.0f, 11.0f);
        this.backRightShin = new ModelRenderer(this, 96, 43);
        this.backRightShin.addBox(-1.0f, 0.0f, -1.5f, 3, 5, 3);
        this.backRightShin.setRotationPoint(-4.0f, 16.0f, 11.0f);
        this.backRightHoof = new ModelRenderer(this, 96, 51);
        this.backRightHoof.addBox(-1.5f, 5.1f, -2.0f, 4, 3, 4);
        this.backRightHoof.setRotationPoint(-4.0f, 16.0f, 11.0f);
        this.frontLeftLeg = new ModelRenderer(this, 44, 29);
        this.frontLeftLeg.addBox(-1.9f, -1.0f, -2.1f, 3, 8, 4);
        this.frontLeftLeg.setRotationPoint(4.0f, 9.0f, -8.0f);
        this.frontLeftShin = new ModelRenderer(this, 44, 41);
        this.frontLeftShin.addBox(-1.9f, 0.0f, -1.6f, 3, 5, 3);
        this.frontLeftShin.setRotationPoint(4.0f, 16.0f, -8.0f);
        this.frontLeftHoof = new ModelRenderer(this, 44, 51);
        this.frontLeftHoof.addBox(-2.4f, 5.1f, -2.1f, 4, 3, 4);
        this.frontLeftHoof.setRotationPoint(4.0f, 16.0f, -8.0f);
        this.frontRightLeg = new ModelRenderer(this, 60, 29);
        this.frontRightLeg.addBox(-1.1f, -1.0f, -2.1f, 3, 8, 4);
        this.frontRightLeg.setRotationPoint(-4.0f, 9.0f, -8.0f);
        this.frontRightShin = new ModelRenderer(this, 60, 41);
        this.frontRightShin.addBox(-1.1f, 0.0f, -1.6f, 3, 5, 3);
        this.frontRightShin.setRotationPoint(-4.0f, 16.0f, -8.0f);
        this.frontRightHoof = new ModelRenderer(this, 60, 51);
        this.frontRightHoof.addBox(-1.6f, 5.1f, -2.1f, 4, 3, 4);
        this.frontRightHoof.setRotationPoint(-4.0f, 16.0f, -8.0f);
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-2.5f, -10.0f, -1.5f, 5, 5, 7);
        this.head.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.head, 0.5235988f, 0.0f, 0.0f);
        this.field_178711_b = new ModelRenderer(this, 24, 18);
        this.field_178711_b.addBox(-2.0f, -10.0f, -7.0f, 4, 3, 6);
        this.field_178711_b.setRotationPoint(0.0f, 3.95f, -10.0f);
        this.setBoxRotation(this.field_178711_b, 0.5235988f, 0.0f, 0.0f);
        this.field_178712_c = new ModelRenderer(this, 24, 27);
        this.field_178712_c.addBox(-2.0f, -7.0f, -6.5f, 4, 2, 5);
        this.field_178712_c.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.field_178712_c, 0.5235988f, 0.0f, 0.0f);
        this.head.addChild(this.field_178711_b);
        this.head.addChild(this.field_178712_c);
        this.horseLeftEar = new ModelRenderer(this, 0, 0);
        this.horseLeftEar.addBox(0.45f, -12.0f, 4.0f, 2, 3, 1);
        this.horseLeftEar.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.horseLeftEar, 0.5235988f, 0.0f, 0.0f);
        this.horseRightEar = new ModelRenderer(this, 0, 0);
        this.horseRightEar.addBox(-2.45f, -12.0f, 4.0f, 2, 3, 1);
        this.horseRightEar.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.horseRightEar, 0.5235988f, 0.0f, 0.0f);
        this.muleLeftEar = new ModelRenderer(this, 0, 12);
        this.muleLeftEar.addBox(-2.0f, -16.0f, 4.0f, 2, 7, 1);
        this.muleLeftEar.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.muleLeftEar, 0.5235988f, 0.0f, 0.2617994f);
        this.muleRightEar = new ModelRenderer(this, 0, 12);
        this.muleRightEar.addBox(0.0f, -16.0f, 4.0f, 2, 7, 1);
        this.muleRightEar.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.muleRightEar, 0.5235988f, 0.0f, -0.2617994f);
        this.neck = new ModelRenderer(this, 0, 12);
        this.neck.addBox(-2.05f, -9.8f, -2.0f, 4, 14, 8);
        this.neck.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.neck, 0.5235988f, 0.0f, 0.0f);
        this.muleLeftChest = new ModelRenderer(this, 0, 34);
        this.muleLeftChest.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3);
        this.muleLeftChest.setRotationPoint(-7.5f, 3.0f, 10.0f);
        this.setBoxRotation(this.muleLeftChest, 0.0f, 1.5707964f, 0.0f);
        this.muleRightChest = new ModelRenderer(this, 0, 47);
        this.muleRightChest.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3);
        this.muleRightChest.setRotationPoint(4.5f, 3.0f, 10.0f);
        this.setBoxRotation(this.muleRightChest, 0.0f, 1.5707964f, 0.0f);
        this.horseSaddleBottom = new ModelRenderer(this, 80, 0);
        this.horseSaddleBottom.addBox(-5.0f, 0.0f, -3.0f, 10, 1, 8);
        this.horseSaddleBottom.setRotationPoint(0.0f, 2.0f, 2.0f);
        this.horseSaddleFront = new ModelRenderer(this, 106, 9);
        this.horseSaddleFront.addBox(-1.5f, -1.0f, -3.0f, 3, 1, 2);
        this.horseSaddleFront.setRotationPoint(0.0f, 2.0f, 2.0f);
        this.horseSaddleBack = new ModelRenderer(this, 80, 9);
        this.horseSaddleBack.addBox(-4.0f, -1.0f, 3.0f, 8, 1, 2);
        this.horseSaddleBack.setRotationPoint(0.0f, 2.0f, 2.0f);
        this.horseLeftSaddleMetal = new ModelRenderer(this, 74, 0);
        this.horseLeftSaddleMetal.addBox(-0.5f, 6.0f, -1.0f, 1, 2, 2);
        this.horseLeftSaddleMetal.setRotationPoint(5.0f, 3.0f, 2.0f);
        this.horseLeftSaddleRope = new ModelRenderer(this, 70, 0);
        this.horseLeftSaddleRope.addBox(-0.5f, 0.0f, -0.5f, 1, 6, 1);
        this.horseLeftSaddleRope.setRotationPoint(5.0f, 3.0f, 2.0f);
        this.horseRightSaddleMetal = new ModelRenderer(this, 74, 4);
        this.horseRightSaddleMetal.addBox(-0.5f, 6.0f, -1.0f, 1, 2, 2);
        this.horseRightSaddleMetal.setRotationPoint(-5.0f, 3.0f, 2.0f);
        this.horseRightSaddleRope = new ModelRenderer(this, 80, 0);
        this.horseRightSaddleRope.addBox(-0.5f, 0.0f, -0.5f, 1, 6, 1);
        this.horseRightSaddleRope.setRotationPoint(-5.0f, 3.0f, 2.0f);
        this.horseLeftFaceMetal = new ModelRenderer(this, 74, 13);
        this.horseLeftFaceMetal.addBox(1.5f, -8.0f, -4.0f, 1, 2, 2);
        this.horseLeftFaceMetal.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.horseLeftFaceMetal, 0.5235988f, 0.0f, 0.0f);
        this.horseRightFaceMetal = new ModelRenderer(this, 74, 13);
        this.horseRightFaceMetal.addBox(-2.5f, -8.0f, -4.0f, 1, 2, 2);
        this.horseRightFaceMetal.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.horseRightFaceMetal, 0.5235988f, 0.0f, 0.0f);
        this.horseLeftRein = new ModelRenderer(this, 44, 10);
        this.horseLeftRein.addBox(2.6f, -6.0f, -6.0f, 0, 3, 16);
        this.horseLeftRein.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.horseRightRein = new ModelRenderer(this, 44, 5);
        this.horseRightRein.addBox(-2.6f, -6.0f, -6.0f, 0, 3, 16);
        this.horseRightRein.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.mane = new ModelRenderer(this, 58, 0);
        this.mane.addBox(-1.0f, -11.5f, 5.0f, 2, 16, 4);
        this.mane.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.mane, 0.5235988f, 0.0f, 0.0f);
        this.horseFaceRopes = new ModelRenderer(this, 80, 12);
        this.horseFaceRopes.addBox(-2.5f, -10.1f, -7.0f, 5, 5, 12, 0.2f);
        this.horseFaceRopes.setRotationPoint(0.0f, 4.0f, -10.0f);
        this.setBoxRotation(this.horseFaceRopes, 0.5235988f, 0.0f, 0.0f);
    }

    @Override
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        boolean flag4;
        EntityHorse entityhorse = (EntityHorse)entityIn;
        int i = entityhorse.getHorseType();
        float f = entityhorse.getGrassEatingAmount(0.0f);
        boolean flag = entityhorse.isAdultHorse();
        boolean flag1 = flag && entityhorse.isHorseSaddled();
        boolean flag2 = flag && entityhorse.isChested();
        boolean flag3 = i == 1 || i == 2;
        float f1 = entityhorse.getHorseSize();
        boolean bl = flag4 = entityhorse.riddenByEntity != null;
        if (flag1) {
            this.horseFaceRopes.render(scale);
            this.horseSaddleBottom.render(scale);
            this.horseSaddleFront.render(scale);
            this.horseSaddleBack.render(scale);
            this.horseLeftSaddleRope.render(scale);
            this.horseLeftSaddleMetal.render(scale);
            this.horseRightSaddleRope.render(scale);
            this.horseRightSaddleMetal.render(scale);
            this.horseLeftFaceMetal.render(scale);
            this.horseRightFaceMetal.render(scale);
            if (flag4) {
                this.horseLeftRein.render(scale);
                this.horseRightRein.render(scale);
            }
        }
        if (!flag) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(f1, 0.5f + f1 * 0.5f, f1);
            GlStateManager.translate(0.0f, 0.95f * (1.0f - f1), 0.0f);
        }
        this.backLeftLeg.render(scale);
        this.backLeftShin.render(scale);
        this.backLeftHoof.render(scale);
        this.backRightLeg.render(scale);
        this.backRightShin.render(scale);
        this.backRightHoof.render(scale);
        this.frontLeftLeg.render(scale);
        this.frontLeftShin.render(scale);
        this.frontLeftHoof.render(scale);
        this.frontRightLeg.render(scale);
        this.frontRightShin.render(scale);
        this.frontRightHoof.render(scale);
        if (!flag) {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(f1, f1, f1);
            GlStateManager.translate(0.0f, 1.35f * (1.0f - f1), 0.0f);
        }
        this.body.render(scale);
        this.tailBase.render(scale);
        this.tailMiddle.render(scale);
        this.tailTip.render(scale);
        this.neck.render(scale);
        this.mane.render(scale);
        if (!flag) {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float f2 = 0.5f + f1 * f1 * 0.5f;
            GlStateManager.scale(f2, f2, f2);
            if (f <= 0.0f) {
                GlStateManager.translate(0.0f, 1.35f * (1.0f - f1), 0.0f);
            } else {
                GlStateManager.translate(0.0f, 0.9f * (1.0f - f1) * f + 1.35f * (1.0f - f1) * (1.0f - f), 0.15f * (1.0f - f1) * f);
            }
        }
        if (flag3) {
            this.muleLeftEar.render(scale);
            this.muleRightEar.render(scale);
        } else {
            this.horseLeftEar.render(scale);
            this.horseRightEar.render(scale);
        }
        this.head.render(scale);
        if (!flag) {
            GlStateManager.popMatrix();
        }
        if (flag2) {
            this.muleLeftChest.render(scale);
            this.muleRightChest.render(scale);
        }
    }

    private void setBoxRotation(ModelRenderer p_110682_1_, float p_110682_2_, float p_110682_3_, float p_110682_4_) {
        p_110682_1_.rotateAngleX = p_110682_2_;
        p_110682_1_.rotateAngleY = p_110682_3_;
        p_110682_1_.rotateAngleZ = p_110682_4_;
    }

    private float updateHorseRotation(float p_110683_1_, float p_110683_2_, float p_110683_3_) {
        float f;
        for (f = p_110683_2_ - p_110683_1_; f < -180.0f; f += 360.0f) {
        }
        while (f >= 180.0f) {
            f -= 360.0f;
        }
        return p_110683_1_ + p_110683_3_ * f;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
        super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
        float f = this.updateHorseRotation(entitylivingbaseIn.prevRenderYawOffset, entitylivingbaseIn.renderYawOffset, partialTickTime);
        float f1 = this.updateHorseRotation(entitylivingbaseIn.prevRotationYawHead, entitylivingbaseIn.rotationYawHead, partialTickTime);
        float f2 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTickTime;
        float f3 = f1 - f;
        float f4 = f2 / 57.295776f;
        if (f3 > 20.0f) {
            f3 = 20.0f;
        }
        if (f3 < -20.0f) {
            f3 = -20.0f;
        }
        if (p_78086_3_ > 0.2f) {
            f4 += MathHelper.cos(p_78086_2_ * 0.4f) * 0.15f * p_78086_3_;
        }
        EntityHorse entityhorse = (EntityHorse)entitylivingbaseIn;
        float f5 = entityhorse.getGrassEatingAmount(partialTickTime);
        float f6 = entityhorse.getRearingAmount(partialTickTime);
        float f7 = 1.0f - f6;
        float f8 = entityhorse.getMouthOpennessAngle(partialTickTime);
        boolean flag = entityhorse.field_110278_bp != 0;
        boolean flag1 = entityhorse.isHorseSaddled();
        boolean flag2 = entityhorse.riddenByEntity != null;
        float f9 = (float)entitylivingbaseIn.ticksExisted + partialTickTime;
        float f10 = MathHelper.cos(p_78086_2_ * 0.6662f + (float)Math.PI);
        float f11 = f10 * 0.8f * p_78086_3_;
        this.head.rotationPointY = 4.0f;
        this.head.rotationPointZ = -10.0f;
        this.tailBase.rotationPointY = 3.0f;
        this.tailMiddle.rotationPointZ = 14.0f;
        this.muleRightChest.rotationPointY = 3.0f;
        this.muleRightChest.rotationPointZ = 10.0f;
        this.body.rotateAngleX = 0.0f;
        this.head.rotateAngleX = 0.5235988f + f4;
        this.head.rotateAngleY = f3 / 57.295776f;
        this.head.rotateAngleX = f6 * (0.2617994f + f4) + f5 * 2.18166f + (1.0f - Math.max(f6, f5)) * this.head.rotateAngleX;
        this.head.rotateAngleY = f6 * f3 / 57.295776f + (1.0f - Math.max(f6, f5)) * this.head.rotateAngleY;
        this.head.rotationPointY = f6 * -6.0f + f5 * 11.0f + (1.0f - Math.max(f6, f5)) * this.head.rotationPointY;
        this.head.rotationPointZ = f6 * -1.0f + f5 * -10.0f + (1.0f - Math.max(f6, f5)) * this.head.rotationPointZ;
        this.tailBase.rotationPointY = f6 * 9.0f + f7 * this.tailBase.rotationPointY;
        this.tailMiddle.rotationPointZ = f6 * 18.0f + f7 * this.tailMiddle.rotationPointZ;
        this.muleRightChest.rotationPointY = f6 * 5.5f + f7 * this.muleRightChest.rotationPointY;
        this.muleRightChest.rotationPointZ = f6 * 15.0f + f7 * this.muleRightChest.rotationPointZ;
        this.body.rotateAngleX = f6 * -45.0f / 57.295776f + f7 * this.body.rotateAngleX;
        this.horseLeftEar.rotationPointY = this.head.rotationPointY;
        this.horseRightEar.rotationPointY = this.head.rotationPointY;
        this.muleLeftEar.rotationPointY = this.head.rotationPointY;
        this.muleRightEar.rotationPointY = this.head.rotationPointY;
        this.neck.rotationPointY = this.head.rotationPointY;
        this.field_178711_b.rotationPointY = 0.02f;
        this.field_178712_c.rotationPointY = 0.0f;
        this.mane.rotationPointY = this.head.rotationPointY;
        this.horseLeftEar.rotationPointZ = this.head.rotationPointZ;
        this.horseRightEar.rotationPointZ = this.head.rotationPointZ;
        this.muleLeftEar.rotationPointZ = this.head.rotationPointZ;
        this.muleRightEar.rotationPointZ = this.head.rotationPointZ;
        this.neck.rotationPointZ = this.head.rotationPointZ;
        this.field_178711_b.rotationPointZ = 0.02f - f8 * 1.0f;
        this.field_178712_c.rotationPointZ = 0.0f + f8 * 1.0f;
        this.mane.rotationPointZ = this.head.rotationPointZ;
        this.horseLeftEar.rotateAngleX = this.head.rotateAngleX;
        this.horseRightEar.rotateAngleX = this.head.rotateAngleX;
        this.muleLeftEar.rotateAngleX = this.head.rotateAngleX;
        this.muleRightEar.rotateAngleX = this.head.rotateAngleX;
        this.neck.rotateAngleX = this.head.rotateAngleX;
        this.field_178711_b.rotateAngleX = 0.0f - 0.09424778f * f8;
        this.field_178712_c.rotateAngleX = 0.0f + 0.15707964f * f8;
        this.mane.rotateAngleX = this.head.rotateAngleX;
        this.horseLeftEar.rotateAngleY = this.head.rotateAngleY;
        this.horseRightEar.rotateAngleY = this.head.rotateAngleY;
        this.muleLeftEar.rotateAngleY = this.head.rotateAngleY;
        this.muleRightEar.rotateAngleY = this.head.rotateAngleY;
        this.neck.rotateAngleY = this.head.rotateAngleY;
        this.field_178711_b.rotateAngleY = 0.0f;
        this.field_178712_c.rotateAngleY = 0.0f;
        this.mane.rotateAngleY = this.head.rotateAngleY;
        this.muleLeftChest.rotateAngleX = f11 / 5.0f;
        this.muleRightChest.rotateAngleX = -f11 / 5.0f;
        float f12 = 1.5707964f;
        float f13 = 4.712389f;
        float f14 = -1.0471976f;
        float f15 = 0.2617994f * f6;
        float f16 = MathHelper.cos(f9 * 0.6f + (float)Math.PI);
        this.frontLeftLeg.rotationPointY = -2.0f * f6 + 9.0f * f7;
        this.frontLeftLeg.rotationPointZ = -2.0f * f6 + -8.0f * f7;
        this.frontRightLeg.rotationPointY = this.frontLeftLeg.rotationPointY;
        this.frontRightLeg.rotationPointZ = this.frontLeftLeg.rotationPointZ;
        this.backLeftShin.rotationPointY = this.backLeftLeg.rotationPointY + MathHelper.sin(1.5707964f + f15 + f7 * -f10 * 0.5f * p_78086_3_) * 7.0f;
        this.backLeftShin.rotationPointZ = this.backLeftLeg.rotationPointZ + MathHelper.cos(4.712389f + f15 + f7 * -f10 * 0.5f * p_78086_3_) * 7.0f;
        this.backRightShin.rotationPointY = this.backRightLeg.rotationPointY + MathHelper.sin(1.5707964f + f15 + f7 * f10 * 0.5f * p_78086_3_) * 7.0f;
        this.backRightShin.rotationPointZ = this.backRightLeg.rotationPointZ + MathHelper.cos(4.712389f + f15 + f7 * f10 * 0.5f * p_78086_3_) * 7.0f;
        float f17 = (-1.0471976f + f16) * f6 + f11 * f7;
        float f18 = (-1.0471976f + -f16) * f6 + -f11 * f7;
        this.frontLeftShin.rotationPointY = this.frontLeftLeg.rotationPointY + MathHelper.sin(1.5707964f + f17) * 7.0f;
        this.frontLeftShin.rotationPointZ = this.frontLeftLeg.rotationPointZ + MathHelper.cos(4.712389f + f17) * 7.0f;
        this.frontRightShin.rotationPointY = this.frontRightLeg.rotationPointY + MathHelper.sin(1.5707964f + f18) * 7.0f;
        this.frontRightShin.rotationPointZ = this.frontRightLeg.rotationPointZ + MathHelper.cos(4.712389f + f18) * 7.0f;
        this.backLeftLeg.rotateAngleX = f15 + -f10 * 0.5f * p_78086_3_ * f7;
        this.backLeftHoof.rotateAngleX = this.backLeftShin.rotateAngleX = -0.08726646f * f6 + (-f10 * 0.5f * p_78086_3_ - Math.max(0.0f, f10 * 0.5f * p_78086_3_)) * f7;
        this.backRightLeg.rotateAngleX = f15 + f10 * 0.5f * p_78086_3_ * f7;
        this.backRightHoof.rotateAngleX = this.backRightShin.rotateAngleX = -0.08726646f * f6 + (f10 * 0.5f * p_78086_3_ - Math.max(0.0f, -f10 * 0.5f * p_78086_3_)) * f7;
        this.frontLeftLeg.rotateAngleX = f17;
        this.frontLeftHoof.rotateAngleX = this.frontLeftShin.rotateAngleX = (this.frontLeftLeg.rotateAngleX + (float)Math.PI * Math.max(0.0f, 0.2f + f16 * 0.2f)) * f6 + (f11 + Math.max(0.0f, f10 * 0.5f * p_78086_3_)) * f7;
        this.frontRightLeg.rotateAngleX = f18;
        this.frontRightHoof.rotateAngleX = this.frontRightShin.rotateAngleX = (this.frontRightLeg.rotateAngleX + (float)Math.PI * Math.max(0.0f, 0.2f - f16 * 0.2f)) * f6 + (-f11 + Math.max(0.0f, -f10 * 0.5f * p_78086_3_)) * f7;
        this.backLeftHoof.rotationPointY = this.backLeftShin.rotationPointY;
        this.backLeftHoof.rotationPointZ = this.backLeftShin.rotationPointZ;
        this.backRightHoof.rotationPointY = this.backRightShin.rotationPointY;
        this.backRightHoof.rotationPointZ = this.backRightShin.rotationPointZ;
        this.frontLeftHoof.rotationPointY = this.frontLeftShin.rotationPointY;
        this.frontLeftHoof.rotationPointZ = this.frontLeftShin.rotationPointZ;
        this.frontRightHoof.rotationPointY = this.frontRightShin.rotationPointY;
        this.frontRightHoof.rotationPointZ = this.frontRightShin.rotationPointZ;
        if (flag1) {
            this.horseSaddleBottom.rotationPointY = f6 * 0.5f + f7 * 2.0f;
            this.horseSaddleBottom.rotationPointZ = f6 * 11.0f + f7 * 2.0f;
            this.horseSaddleFront.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseSaddleBack.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseLeftSaddleRope.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseRightSaddleRope.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseLeftSaddleMetal.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseRightSaddleMetal.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.muleLeftChest.rotationPointY = this.muleRightChest.rotationPointY;
            this.horseSaddleFront.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseSaddleBack.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseLeftSaddleRope.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseRightSaddleRope.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseLeftSaddleMetal.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseRightSaddleMetal.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.muleLeftChest.rotationPointZ = this.muleRightChest.rotationPointZ;
            this.horseSaddleBottom.rotateAngleX = this.body.rotateAngleX;
            this.horseSaddleFront.rotateAngleX = this.body.rotateAngleX;
            this.horseSaddleBack.rotateAngleX = this.body.rotateAngleX;
            this.horseLeftRein.rotationPointY = this.head.rotationPointY;
            this.horseRightRein.rotationPointY = this.head.rotationPointY;
            this.horseFaceRopes.rotationPointY = this.head.rotationPointY;
            this.horseLeftFaceMetal.rotationPointY = this.head.rotationPointY;
            this.horseRightFaceMetal.rotationPointY = this.head.rotationPointY;
            this.horseLeftRein.rotationPointZ = this.head.rotationPointZ;
            this.horseRightRein.rotationPointZ = this.head.rotationPointZ;
            this.horseFaceRopes.rotationPointZ = this.head.rotationPointZ;
            this.horseLeftFaceMetal.rotationPointZ = this.head.rotationPointZ;
            this.horseRightFaceMetal.rotationPointZ = this.head.rotationPointZ;
            this.horseLeftRein.rotateAngleX = f4;
            this.horseRightRein.rotateAngleX = f4;
            this.horseFaceRopes.rotateAngleX = this.head.rotateAngleX;
            this.horseLeftFaceMetal.rotateAngleX = this.head.rotateAngleX;
            this.horseRightFaceMetal.rotateAngleX = this.head.rotateAngleX;
            this.horseFaceRopes.rotateAngleY = this.head.rotateAngleY;
            this.horseLeftFaceMetal.rotateAngleY = this.head.rotateAngleY;
            this.horseLeftRein.rotateAngleY = this.head.rotateAngleY;
            this.horseRightFaceMetal.rotateAngleY = this.head.rotateAngleY;
            this.horseRightRein.rotateAngleY = this.head.rotateAngleY;
            if (flag2) {
                this.horseLeftSaddleRope.rotateAngleX = -1.0471976f;
                this.horseLeftSaddleMetal.rotateAngleX = -1.0471976f;
                this.horseRightSaddleRope.rotateAngleX = -1.0471976f;
                this.horseRightSaddleMetal.rotateAngleX = -1.0471976f;
                this.horseLeftSaddleRope.rotateAngleZ = 0.0f;
                this.horseLeftSaddleMetal.rotateAngleZ = 0.0f;
                this.horseRightSaddleRope.rotateAngleZ = 0.0f;
                this.horseRightSaddleMetal.rotateAngleZ = 0.0f;
            } else {
                this.horseLeftSaddleRope.rotateAngleX = f11 / 3.0f;
                this.horseLeftSaddleMetal.rotateAngleX = f11 / 3.0f;
                this.horseRightSaddleRope.rotateAngleX = f11 / 3.0f;
                this.horseRightSaddleMetal.rotateAngleX = f11 / 3.0f;
                this.horseLeftSaddleRope.rotateAngleZ = f11 / 5.0f;
                this.horseLeftSaddleMetal.rotateAngleZ = f11 / 5.0f;
                this.horseRightSaddleRope.rotateAngleZ = -f11 / 5.0f;
                this.horseRightSaddleMetal.rotateAngleZ = -f11 / 5.0f;
            }
        }
        if ((f12 = -1.3089f + p_78086_3_ * 1.5f) > 0.0f) {
            f12 = 0.0f;
        }
        if (flag) {
            this.tailBase.rotateAngleY = MathHelper.cos(f9 * 0.7f);
            f12 = 0.0f;
        } else {
            this.tailBase.rotateAngleY = 0.0f;
        }
        this.tailMiddle.rotateAngleY = this.tailBase.rotateAngleY;
        this.tailTip.rotateAngleY = this.tailBase.rotateAngleY;
        this.tailMiddle.rotationPointY = this.tailBase.rotationPointY;
        this.tailTip.rotationPointY = this.tailBase.rotationPointY;
        this.tailMiddle.rotationPointZ = this.tailBase.rotationPointZ;
        this.tailTip.rotationPointZ = this.tailBase.rotationPointZ;
        this.tailBase.rotateAngleX = f12;
        this.tailMiddle.rotateAngleX = f12;
        this.tailTip.rotateAngleX = -0.2618f + f12;
    }
}

