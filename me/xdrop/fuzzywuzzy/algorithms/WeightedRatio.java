/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.BasicAlgorithm;
import me.xdrop.fuzzywuzzy.algorithms.PrimitiveUtils;

public class WeightedRatio
extends BasicAlgorithm {
    public static final double UNBASE_SCALE = 0.95;
    public static final double PARTIAL_SCALE = 0.9;
    public static final boolean TRY_PARTIALS = true;

    @Override
    public int apply(String s1, String s2, ToStringFunction<String> stringProcessor) {
        s1 = stringProcessor.apply(s1);
        s2 = stringProcessor.apply(s2);
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) {
            return 0;
        }
        boolean tryPartials = true;
        double unbaseScale = 0.95;
        double partialScale = 0.9;
        int base = FuzzySearch.ratio(s1, s2);
        double lenRatio = (double)Math.max(len1, len2) / (double)Math.min(len1, len2);
        if (lenRatio < 1.5) {
            tryPartials = false;
        }
        if (lenRatio > 8.0) {
            partialScale = 0.6;
        }
        if (tryPartials) {
            double partial = (double)FuzzySearch.partialRatio(s1, s2) * partialScale;
            double partialSor = (double)FuzzySearch.tokenSortPartialRatio(s1, s2) * unbaseScale * partialScale;
            double partialSet = (double)FuzzySearch.tokenSetPartialRatio(s1, s2) * unbaseScale * partialScale;
            return (int)Math.round(PrimitiveUtils.max(base, partial, partialSor, partialSet));
        }
        double tokenSort = (double)FuzzySearch.tokenSortRatio(s1, s2) * unbaseScale;
        double tokenSet = (double)FuzzySearch.tokenSetRatio(s1, s2) * unbaseScale;
        return (int)Math.round(PrimitiveUtils.max(base, tokenSort, tokenSet));
    }
}

