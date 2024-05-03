/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple.internal;

public final class Objects {
    private Objects() {
        throw new UnsupportedOperationException();
    }

    public static void ensureNotNull(Object target) {
        if (target == null) {
            throw new NullPointerException();
        }
    }
}

