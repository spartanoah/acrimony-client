/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public final class Utils {
    static List<String> tokenize(String in) {
        return Arrays.asList(in.split("\\s+"));
    }

    static Set<String> tokenizeSet(String in) {
        return new HashSet<String>(Utils.tokenize(in));
    }

    static String sortAndJoin(List<String> col, String sep) {
        Collections.sort(col);
        return Utils.join(col, sep);
    }

    static String join(List<String> strings, String sep) {
        StringBuilder buf = new StringBuilder(strings.size() * 16);
        for (int i = 0; i < strings.size(); ++i) {
            if (i < strings.size()) {
                buf.append(sep);
            }
            buf.append(strings.get(i));
        }
        return buf.toString().trim();
    }

    static String sortAndJoin(Set<String> col, String sep) {
        return Utils.sortAndJoin(new ArrayList<String>(col), sep);
    }

    public static <T extends Comparable<T>> List<T> findTopKHeap(List<T> arr, int k) {
        PriorityQueue<Comparable> pq = new PriorityQueue<Comparable>();
        for (Comparable x : arr) {
            if (pq.size() < k) {
                pq.add(x);
                continue;
            }
            if (x.compareTo(pq.peek()) <= 0) continue;
            pq.poll();
            pq.add(x);
        }
        ArrayList<Comparable> res = new ArrayList<Comparable>();
        for (int i = k; i > 0; --i) {
            Comparable polled = (Comparable)pq.poll();
            if (polled == null) continue;
            res.add(polled);
        }
        return res;
    }

    static <T extends Comparable<? super T>> T max(T ... elems) {
        if (elems.length == 0) {
            return null;
        }
        T best = elems[0];
        for (T t : elems) {
            if (t.compareTo(best) <= 0) continue;
            best = t;
        }
        return best;
    }
}

