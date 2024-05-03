/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

public enum EnumWorldBlockLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String layerName;

    private EnumWorldBlockLayer(String layerNameIn) {
        this.layerName = layerNameIn;
    }

    public String toString() {
        return this.layerName;
    }
}

