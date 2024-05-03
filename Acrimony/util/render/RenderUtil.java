/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

import Acrimony.util.render.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderUtil {
    public static Frustum frustrum = new Frustum();

    public static void setColor(int color) {
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        GL11.glColor4f(r, g, b, a);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return RenderUtil.isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void prepareBoxRender(float lineWidth, double red, double green, double blue, double alpha) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(lineWidth);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void drawFilledCircleNoGL(int x, int y, double r, int c, int quality) {
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0.0f);
        GLUtil.setup2DRendering();
        RenderUtil.color(c);
        GL11.glBegin(6);
        for (int i = 0; i <= 360 / quality; ++i) {
            double x2 = Math.sin((double)(i * quality) * Math.PI / 180.0) * r;
            double y2 = Math.cos((double)(i * quality) * Math.PI / 180.0) * r;
            GL11.glVertex2d((double)x + x2, (double)y + y2);
        }
        GL11.glEnd();
        GLUtil.end2DRendering();
    }

    public static void renderBlockBox(RenderManager rm, float partialTicks, double x, double y, double z) {
        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        RenderGlobal.func_181561_a(new AxisAlignedBB(bb.minX - x + (x - rm.renderPosX), bb.minY - y + (y - rm.renderPosY), bb.minZ - z + (z - rm.renderPosZ), bb.maxX - x + (x - rm.renderPosX), bb.maxY - y + (y - rm.renderPosY), bb.maxZ - z + (z - rm.renderPosZ)));
    }

    public static void renderEntityBox(RenderManager rm, float partialTicks, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        double posX = RenderUtil.interpolate(entity.posX, entity.lastTickPosX, partialTicks);
        double posY = RenderUtil.interpolate(entity.posY, entity.lastTickPosY, partialTicks);
        double posZ = RenderUtil.interpolate(entity.posZ, entity.lastTickPosZ, partialTicks);
        RenderGlobal.func_181561_a(new AxisAlignedBB(bb.minX - 0.05 - entity.posX + (posX - rm.renderPosX), bb.minY - 0.05 - entity.posY + (posY - rm.renderPosY), bb.minZ - 0.05 - entity.posZ + (posZ - rm.renderPosZ), bb.maxX + 0.05 - entity.posX + (posX - rm.renderPosX), bb.maxY + 0.1 - entity.posY + (posY - rm.renderPosY), bb.maxZ + 0.05 - entity.posZ + (posZ - rm.renderPosZ)));
    }

    public static void renderCustomPlayerBox(RenderManager rm, float partialTicks, double x, double y, double z) {
        RenderUtil.renderCustomPlayerBox(rm, partialTicks, x, y, z, x, y, z);
    }

    public static void renderCustomPlayerBox(RenderManager rm, float partialTicks, double x, double y, double z, double lastX, double lastY, double lastZ) {
        AxisAlignedBB bb = new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3);
        double posX = RenderUtil.interpolate(x, lastX, partialTicks);
        double posY = RenderUtil.interpolate(y, lastY, partialTicks);
        double posZ = RenderUtil.interpolate(z, lastZ, partialTicks);
        RenderGlobal.func_181561_a(new AxisAlignedBB(bb.minX - 0.05 - x + (posX - rm.renderPosX), bb.minY - 0.05 - y + (posY - rm.renderPosY), bb.minZ - 0.05 - z + (posZ - rm.renderPosZ), bb.maxX + 0.05 - x + (posX - rm.renderPosX), bb.maxY + 0.1 - y + (posY - rm.renderPosY), bb.maxZ + 0.05 - z + (posZ - rm.renderPosZ)));
    }

    public static void stopBoxRender() {
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static double interpolate(double old, double now, float partialTicks) {
        return old + (now - old) * (double)partialTicks;
    }

    public static float interpolate(float old, float now, float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static void color(int color, float alpha) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void color(int color) {
        RenderUtil.color(color, (float)(color >> 24 & 0xFF) / 255.0f);
    }
}

