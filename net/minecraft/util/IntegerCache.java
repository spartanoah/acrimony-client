/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

public class IntegerCache {
    private static final Integer[] field_181757_a = new Integer[65535];

    public static Integer func_181756_a(int p_181756_0_) {
        return p_181756_0_ >= 0 && p_181756_0_ < field_181757_a.length ? field_181757_a[p_181756_0_] : new Integer(p_181756_0_);
    }

    static {
        int j = field_181757_a.length;
        for (int i = 0; i < j; ++i) {
            IntegerCache.field_181757_a[i] = i;
        }
    }
}

