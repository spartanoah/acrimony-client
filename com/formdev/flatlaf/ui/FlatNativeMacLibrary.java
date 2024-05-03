/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import java.awt.Window;

public class FlatNativeMacLibrary {
    public static boolean isLoaded() {
        return FlatNativeLibrary.isLoaded();
    }

    public static native boolean setWindowRoundedBorder(Window var0, float var1, float var2, int var3);
}

