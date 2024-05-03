/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.api.minecraft;

public class IdDataCombine {
    public static int idFromCombined(int combined) {
        return combined >> 4;
    }

    public static int dataFromCombined(int combined) {
        return combined & 0xF;
    }

    public static int toCombined(int id, int data) {
        return id << 4 | data & 0xF;
    }
}

