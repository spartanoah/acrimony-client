/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

final class PrimitiveUtils {
    PrimitiveUtils() {
    }

    static double max(double ... elems) {
        if (elems.length == 0) {
            return 0.0;
        }
        double best = elems[0];
        for (double t : elems) {
            if (!(t > best)) continue;
            best = t;
        }
        return best;
    }

    static int max(int ... elems) {
        if (elems.length == 0) {
            return 0;
        }
        int best = elems[0];
        for (int t : elems) {
            if (t <= best) continue;
            best = t;
        }
        return best;
    }
}

