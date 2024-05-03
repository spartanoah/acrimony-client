/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.util;

public class NumUtils {
    public static float limit(float val2, float min, float max) {
        return val2 < min ? min : (val2 > max ? max : val2);
    }

    public static int mod(int x, int y) {
        int i = x % y;
        if (i < 0) {
            i += y;
        }
        return i;
    }
}

