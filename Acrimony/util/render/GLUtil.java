/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GLUtil {
    public static int[] enabledCaps = new int[32];

    public static void enableDepth() {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }

    public static void disableDepth() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }

    public static void enableCaps(int ... caps) {
        for (int cap : caps) {
            GL11.glEnable(cap);
        }
        enabledCaps = caps;
    }

    public static void disableCaps() {
        for (int cap : enabledCaps) {
            GL11.glDisable(cap);
        }
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void setup2DRendering(boolean blend) {
        if (blend) {
            GLUtil.startBlend();
        }
        GlStateManager.disableTexture2D();
    }

    public static void setup2DRendering() {
        GLUtil.setup2DRendering(true);
    }

    public static void end2DRendering() {
        GlStateManager.enableTexture2D();
        GLUtil.endBlend();
    }

    public static void startRotate(float x, float y, float rotate) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        GlStateManager.rotate(rotate, 0.0f, 0.0f, -1.0f);
        GlStateManager.translate(-x, -y, 0.0f);
    }

    public static void endRotate() {
        GlStateManager.popMatrix();
    }
}

