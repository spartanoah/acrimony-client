/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.util;

public class ArrayUtils {
    public static boolean contains(Object[] arr, Object val2) {
        if (arr == null) {
            return false;
        }
        for (int i = 0; i < arr.length; ++i) {
            Object object = arr[i];
            if (object != val2) continue;
            return true;
        }
        return false;
    }
}

