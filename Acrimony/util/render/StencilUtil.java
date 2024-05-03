/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class StencilUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }

    public static void checkSetupFBO(Framebuffer framebuffer) {
        if (framebuffer != null && framebuffer.depthBuffer > -1) {
            StencilUtil.setupFBO(framebuffer);
            framebuffer.depthBuffer = -1;
        }
    }

    public static void setupFBO(Framebuffer framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer);
        int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, StencilUtil.mc.displayWidth, StencilUtil.mc.displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID);
    }

    public static void initStencilToWrite() {
        mc.getFramebuffer().bindFramebuffer(false);
        StencilUtil.checkSetupFBO(mc.getFramebuffer());
        GL11.glClear(1024);
        GL11.glEnable(2960);
        GL11.glStencilFunc(519, 1, 1);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glColorMask(false, false, false, false);
    }

    public static void readStencilBuffer(int ref) {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(514, ref, 1);
        GL11.glStencilOp(7680, 7680, 7680);
    }

    public static void uninitStencilBuffer() {
        GL11.glDisable(2960);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return StencilUtil.createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (StencilUtil.needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(StencilUtil.mc.displayWidth, StencilUtil.mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != StencilUtil.mc.displayWidth || framebuffer.framebufferHeight != StencilUtil.mc.displayHeight;
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(3553, texture);
    }
}

