/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.fastutil.objects;

public final class ObjectIterables {
    private ObjectIterables() {
    }

    public static <K> long size(Iterable<K> iterable) {
        long c = 0L;
        for (K dummy : iterable) {
            ++c;
        }
        return c;
    }
}

