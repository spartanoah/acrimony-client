/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.themes;

import com.formdev.flatlaf.FlatLightLaf;

public class FlatMacLightLaf
extends FlatLightLaf {
    public static final String NAME = "FlatLaf macOS Light";

    public static boolean setup() {
        return FlatMacLightLaf.setup(new FlatMacLightLaf());
    }

    public static void installLafInfo() {
        FlatMacLightLaf.installLafInfo(NAME, FlatMacLightLaf.class);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "FlatLaf macOS Light Look and Feel";
    }

    @Override
    public boolean isDark() {
        return false;
    }
}

