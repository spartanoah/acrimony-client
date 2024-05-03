/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.HashSet;
import java.util.Set;

final class SetUtils {
    SetUtils() {
    }

    static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
        HashSet<T> intersection = new HashSet<T>(s1);
        intersection.retainAll(s2);
        return intersection;
    }

    static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
        HashSet<T> difference = new HashSet<T>(s1);
        difference.removeAll(s2);
        return difference;
    }
}

