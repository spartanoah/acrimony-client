/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.waveycapes.renderlayers;

import Acrimony.util.waveycapes.CapeMovement;
import Acrimony.util.waveycapes.config.Config;
import Acrimony.util.waveycapes.renderlayers.CustomCapeRenderLayer;
import Acrimony.util.waveycapes.sim.StickSimulation;
import Acrimony.util.waveycapes.util.Matrix4f;
import Acrimony.util.waveycapes.util.Mth;
import Acrimony.util.waveycapes.util.PoseStack;
import Acrimony.util.waveycapes.util.Vector3f;
import Acrimony.util.waveycapes.util.Vector4f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;

public class SmoothCapeRenderer {
    public void renderSmoothCape(CustomCapeRenderLayer layer, AbstractClientPlayer abstractClientPlayer, float delta) {
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181705_e);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        Matrix4f oldPositionMatrix = null;
        for (int part = 0; part < 16; ++part) {
            this.modifyPoseStack(layer, poseStack, abstractClientPlayer, delta, part);
            if (oldPositionMatrix == null) {
                oldPositionMatrix = poseStack.last().pose();
            }
            if (part == 0) {
                SmoothCapeRenderer.addTopVerfunc_181673_a(worldrenderer, poseStack.last().pose(), oldPositionMatrix, 0.3f, 0.0f, 0.0f, -0.3f, 0.0f, -0.06f, part);
            }
            if (part == 15) {
                SmoothCapeRenderer.addBottomVerfunc_181673_a(worldrenderer, poseStack.last().pose(), poseStack.last().pose(), 0.3f, (float)(part + 1) * 0.06f, 0.0f, -0.3f, (float)(part + 1) * 0.06f, -0.06f, part);
            }
            SmoothCapeRenderer.addLeftVerfunc_181673_a(worldrenderer, poseStack.last().pose(), oldPositionMatrix, -0.3f, (float)(part + 1) * 0.06f, 0.0f, -0.3f, (float)part * 0.06f, -0.06f, part);
            SmoothCapeRenderer.addRightVerfunc_181673_a(worldrenderer, poseStack.last().pose(), oldPositionMatrix, 0.3f, (float)(part + 1) * 0.06f, 0.0f, 0.3f, (float)part * 0.06f, -0.06f, part);
            SmoothCapeRenderer.addBackVerfunc_181673_a(worldrenderer, poseStack.last().pose(), oldPositionMatrix, 0.3f, (float)(part + 1) * 0.06f, -0.06f, -0.3f, (float)part * 0.06f, -0.06f, part);
            SmoothCapeRenderer.addFrontVerfunc_181673_a(worldrenderer, oldPositionMatrix, poseStack.last().pose(), 0.3f, (float)(part + 1) * 0.06f, 0.0f, -0.3f, (float)part * 0.06f, 0.0f, part);
            oldPositionMatrix = poseStack.last().pose();
            poseStack.popPose();
        }
        Tessellator.getInstance().draw();
    }

    void modifyPoseStack(CustomCapeRenderLayer layer, PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        if (Config.capeMovement == CapeMovement.BASIC_SIMULATION) {
            this.modifyPoseStackSimulation(layer, poseStack, abstractClientPlayer, h, part);
            return;
        }
        this.modifyPoseStackVanilla(layer, poseStack, abstractClientPlayer, h, part);
    }

    private void modifyPoseStackSimulation(CustomCapeRenderLayer layer, PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float delta, int part) {
        StickSimulation simulation = abstractClientPlayer.stickSimulation;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.125);
        float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if (z > 0.0f) {
            z = 0.0f;
        }
        float y = simulation.points.get(0).getLerpY(delta) - (float)part - simulation.points.get(part).getLerpY(delta);
        float sidewaysRotationOffset = 0.0f;
        float partRotation = (float)(-Math.atan2(y, z));
        if ((partRotation = Math.max(partRotation, 0.0f)) != 0.0f) {
            partRotation = (float)(Math.PI - (double)partRotation);
        }
        partRotation = (float)((double)partRotation * 57.2958);
        partRotation *= 2.0f;
        float height = 0.0f;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0f;
            poseStack.translate(0.0, 0.15f, 0.0);
        }
        float naturalWindSwing = layer.getNatrualWindSwing(part);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0f + height + naturalWindSwing));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - sidewaysRotationOffset / 2.0f));
        poseStack.translate(0.0, y / 16.0f, z / 16.0f);
        poseStack.translate(0.0, 0.03, -0.03);
        poseStack.translate(0.0, (float)part * 1.0f / 16.0f, part * 0 / 16);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-partRotation));
        poseStack.translate(0.0, (float)(-part) * 1.0f / 16.0f, -part * 0 / 16);
        poseStack.translate(0.0, -0.03, 0.03);
    }

    private void modifyPoseStackVanilla(CustomCapeRenderLayer layer, PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.125);
        double d = Mth.lerp((double)h, abstractClientPlayer.prevChasingPosX, abstractClientPlayer.chasingPosX) - Mth.lerp((double)h, abstractClientPlayer.prevPosX, abstractClientPlayer.posX);
        double e = Mth.lerp((double)h, abstractClientPlayer.prevChasingPosY, abstractClientPlayer.chasingPosY) - Mth.lerp((double)h, abstractClientPlayer.prevPosY, abstractClientPlayer.posY);
        double m = Mth.lerp((double)h, abstractClientPlayer.prevChasingPosZ, abstractClientPlayer.chasingPosZ) - Mth.lerp((double)h, abstractClientPlayer.prevPosZ, abstractClientPlayer.posZ);
        float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        double o = Math.sin(n * ((float)Math.PI / 180));
        double p = -Math.cos(n * ((float)Math.PI / 180));
        float height = (float)e * 10.0f;
        height = MathHelper.clamp_float(height, -6.0f, 32.0f);
        float swing = (float)(d * o + m * p) * SmoothCapeRenderer.easeOutSine(0.0625f * (float)part) * 100.0f;
        swing = MathHelper.clamp_float(swing, 0.0f, 150.0f * SmoothCapeRenderer.easeOutSine(0.0625f * (float)part));
        float sidewaysRotationOffset = (float)(d * p - m * o) * 100.0f;
        sidewaysRotationOffset = MathHelper.clamp_float(sidewaysRotationOffset, -20.0f, 20.0f);
        float t = Mth.lerp(h, abstractClientPlayer.prevCameraYaw, abstractClientPlayer.cameraYaw);
        height = (float)((double)height + Math.sin(Mth.lerp(h, abstractClientPlayer.prevDistanceWalkedModified, abstractClientPlayer.distanceWalkedModified) * 6.0f) * 32.0 * (double)t);
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0f;
            poseStack.translate(0.0, 0.15f, 0.0);
        }
        float naturalWindSwing = layer.getNatrualWindSwing(part);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0f + swing / 2.0f + height + naturalWindSwing));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - sidewaysRotationOffset / 2.0f));
    }

    private static void addBackVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
            Matrix4f k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }
        float minU = 0.015625f;
        float maxU = 0.171875f;
        float minV = 0.03125f;
        float maxV = 0.53125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x1, y2, z1).func_181673_a(maxU, minV += vPerPart * (float)part).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z1).func_181673_a(minU, minV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z2).func_181673_a(minU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x1, y1, z2).func_181673_a(maxU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
    }

    private static void addFrontVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
            Matrix4f k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }
        float minU = 0.1875f;
        float maxU = 0.34375f;
        float minV = 0.03125f;
        float maxV = 0.53125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x1, y1, z1).func_181673_a(maxU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y1, z1).func_181673_a(minU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y2, z2).func_181673_a(minU, minV += vPerPart * (float)part).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x1, y2, z2).func_181673_a(maxU, minV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
    }

    private static void addLeftVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float minU = 0.0f;
        float maxU = 0.015625f;
        float minV = 0.03125f;
        float maxV = 0.53125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z1).func_181673_a(maxU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z2).func_181673_a(minU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z2).func_181673_a(minU, minV += vPerPart * (float)part).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z1).func_181673_a(maxU, minV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
    }

    private static void addRightVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float minU = 0.171875f;
        float maxU = 0.1875f;
        float minV = 0.03125f;
        float maxV = 0.53125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z2).func_181673_a(minU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z1).func_181673_a(maxU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z1).func_181673_a(maxU, minV += vPerPart * (float)part).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z2).func_181673_a(minU, minV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
    }

    private static void addBottomVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float minU = 0.171875f;
        float maxU = 0.328125f;
        float minV = 0.0f;
        float maxV = 0.03125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x1, y2, z2).func_181673_a(maxU, minV += vPerPart * (float)part).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z2).func_181673_a(minU, minV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z1).func_181673_a(minU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x1, y1, z1).func_181673_a(maxU, maxV).func_181663_c(1.0f, 0.0f, 0.0f).func_181675_d();
    }

    private static WorldRenderer verfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix4f, float f, float g, float h) {
        Vector4f vector4f = new Vector4f(f, g, h, 1.0f);
        vector4f.transform(matrix4f);
        worldrenderer.func_181662_b(vector4f.x(), vector4f.y(), vector4f.z());
        return worldrenderer;
    }

    private static void addTopVerfunc_181673_a(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float minU = 0.015625f;
        float maxU = 0.171875f;
        float minV = 0.0f;
        float maxV = 0.03125f;
        float deltaV = maxV - minV;
        float vPerPart = deltaV / 16.0f;
        maxV = minV + vPerPart * (float)(part + 1);
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x1, y2, z1).func_181673_a(maxU, maxV).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, oldMatrix, x2, y2, z1).func_181673_a(minU, maxV).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x2, y1, z2).func_181673_a(minU, minV += vPerPart * (float)part).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
        SmoothCapeRenderer.verfunc_181673_a(worldrenderer, matrix, x1, y1, z2).func_181673_a(maxU, minV).func_181663_c(0.0f, 1.0f, 0.0f).func_181675_d();
    }

    private static float easeOutSine(float x) {
        return (float)Math.sin((double)x * Math.PI / 2.0);
    }
}

