/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class NVBlendEquationAdvanced {
    public static final int GL_BLEND_ADVANCED_COHERENT_NV = 37509;
    public static final int GL_BLEND_PREMULTIPLIED_SRC_NV = 37504;
    public static final int GL_BLEND_OVERLAP_NV = 37505;
    public static final int GL_UNCORRELATED_NV = 37506;
    public static final int GL_DISJOINT_NV = 37507;
    public static final int GL_CONJOINT_NV = 37508;
    public static final int GL_SRC_NV = 37510;
    public static final int GL_DST_NV = 37511;
    public static final int GL_SRC_OVER_NV = 37512;
    public static final int GL_DST_OVER_NV = 37513;
    public static final int GL_SRC_IN_NV = 37514;
    public static final int GL_DST_IN_NV = 37515;
    public static final int GL_SRC_OUT_NV = 37516;
    public static final int GL_DST_OUT_NV = 37517;
    public static final int GL_SRC_ATOP_NV = 37518;
    public static final int GL_DST_ATOP_NV = 37519;
    public static final int GL_MULTIPLY_NV = 37524;
    public static final int GL_SCREEN_NV = 37525;
    public static final int GL_OVERLAY_NV = 37526;
    public static final int GL_DARKEN_NV = 37527;
    public static final int GL_LIGHTEN_NV = 37528;
    public static final int GL_COLORDODGE_NV = 37529;
    public static final int GL_COLORBURN_NV = 37530;
    public static final int GL_HARDLIGHT_NV = 37531;
    public static final int GL_SOFTLIGHT_NV = 37532;
    public static final int GL_DIFFERENCE_NV = 37534;
    public static final int GL_EXCLUSION_NV = 37536;
    public static final int GL_INVERT_RGB_NV = 37539;
    public static final int GL_LINEARDODGE_NV = 37540;
    public static final int GL_LINEARBURN_NV = 37541;
    public static final int GL_VIVIDLIGHT_NV = 37542;
    public static final int GL_LINEARLIGHT_NV = 37543;
    public static final int GL_PINLIGHT_NV = 37544;
    public static final int GL_HARDMIX_NV = 37545;
    public static final int GL_HSL_HUE_NV = 37549;
    public static final int GL_HSL_SATURATION_NV = 37550;
    public static final int GL_HSL_COLOR_NV = 37551;
    public static final int GL_HSL_LUMINOSITY_NV = 37552;
    public static final int GL_PLUS_NV = 37521;
    public static final int GL_PLUS_CLAMPED_NV = 37553;
    public static final int GL_PLUS_CLAMPED_ALPHA_NV = 37554;
    public static final int GL_PLUS_DARKER_NV = 37522;
    public static final int GL_MINUS_NV = 37535;
    public static final int GL_MINUS_CLAMPED_NV = 37555;
    public static final int GL_CONTRAST_NV = 37537;
    public static final int GL_INVERT_OVG_NV = 37556;

    private NVBlendEquationAdvanced() {
    }

    public static void glBlendParameteriNV(int pname, int value) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendParameteriNV;
        BufferChecks.checkFunctionAddress(function_pointer);
        NVBlendEquationAdvanced.nglBlendParameteriNV(pname, value, function_pointer);
    }

    static native void nglBlendParameteriNV(int var0, int var1, long var2);

    public static void glBlendBarrierNV() {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendBarrierNV;
        BufferChecks.checkFunctionAddress(function_pointer);
        NVBlendEquationAdvanced.nglBlendBarrierNV(function_pointer);
    }

    static native void nglBlendBarrierNV(long var0);
}

