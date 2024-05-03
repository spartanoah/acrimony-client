/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Window;

public class FlatNativeWindowsLibrary {
    private static long osBuildNumber = Long.MIN_VALUE;
    public static final int DWMWCP_DEFAULT = 0;
    public static final int DWMWCP_DONOTROUND = 1;
    public static final int DWMWCP_ROUND = 2;
    public static final int DWMWCP_ROUNDSMALL = 3;
    public static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    public static final int DWMWA_BORDER_COLOR = 34;
    public static final int DWMWA_CAPTION_COLOR = 35;
    public static final int DWMWA_TEXT_COLOR = 36;
    public static final int DWMWA_COLOR_DEFAULT = -1;
    public static final int DWMWA_COLOR_NONE = -2;
    public static final Color COLOR_NONE = new Color(0, true);

    public static boolean isLoaded() {
        return SystemInfo.isWindows && FlatNativeLibrary.isLoaded();
    }

    public static long getOSBuildNumber() {
        if (osBuildNumber == Long.MIN_VALUE) {
            osBuildNumber = FlatNativeWindowsLibrary.getOSBuildNumberImpl();
        }
        return osBuildNumber;
    }

    private static native long getOSBuildNumberImpl();

    public static native long getHWND(Window var0);

    public static native boolean setWindowCornerPreference(long var0, int var2);

    public static native boolean dwmSetWindowAttributeBOOL(long var0, int var2, boolean var3);

    public static native boolean dwmSetWindowAttributeDWORD(long var0, int var2, int var3);

    public static boolean dwmSetWindowAttributeCOLORREF(long hwnd, int attribute, Color color) {
        int rgb = color == COLOR_NONE ? -2 : (color != null ? color.getRed() | color.getGreen() << 8 | color.getBlue() << 16 : -1);
        return FlatNativeWindowsLibrary.dwmSetWindowAttributeDWORD(hwnd, attribute, rgb);
    }
}

