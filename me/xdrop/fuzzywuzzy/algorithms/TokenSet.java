/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import me.xdrop.fuzzywuzzy.Ratio;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.RatioAlgorithm;
import me.xdrop.fuzzywuzzy.algorithms.SetUtils;
import me.xdrop.fuzzywuzzy.algorithms.Utils;

public class TokenSet
extends RatioAlgorithm {
    @Override
    public int apply(String s1, String s2, Ratio ratio, ToStringFunction<String> stringFunction) {
        s1 = stringFunction.apply(s1);
        s2 = stringFunction.apply(s2);
        Set<String> tokens1 = Utils.tokenizeSet(s1);
        Set<String> tokens2 = Utils.tokenizeSet(s2);
        Set<String> intersection = SetUtils.intersection(tokens1, tokens2);
        Set<String> diff1to2 = SetUtils.difference(tokens1, tokens2);
        Set<String> diff2to1 = SetUtils.difference(tokens2, tokens1);
        String sortedInter = Utils.sortAndJoin(intersection, " ").trim();
        String sorted1to2 = (sortedInter + " " + Utils.sortAndJoin(diff1to2, " ")).trim();
        String sorted2to1 = (sortedInter + " " + Utils.sortAndJoin(diff2to1, " ")).trim();
        ArrayList<Integer> results = new ArrayList<Integer>();
        results.add(ratio.apply(sortedInter, sorted1to2));
        results.add(ratio.apply(sortedInter, sorted2to1));
        results.add(ratio.apply(sorted1to2, sorted2to1));
        return (Integer)Collections.max(results);
    }
}

