/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.themes;

import com.formdev.flatlaf.FlatDarkLaf;

public class FlatMacDarkLaf
extends FlatDarkLaf {
    public static final String NAME = "FlatLaf macOS Dark";

    public static boolean setup() {
        return FlatMacDarkLaf.setup(new FlatMacDarkLaf());
    }

    public static void installLafInfo() {
        FlatMacDarkLaf.installLafInfo(NAME, FlatMacDarkLaf.class);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "FlatLaf macOS Dark Look and Feel";
    }

    @Override
    public boolean isDark() {
        return true;
    }
}

