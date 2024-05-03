/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.Arrays;
import java.util.List;
import me.xdrop.fuzzywuzzy.Ratio;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.RatioAlgorithm;
import me.xdrop.fuzzywuzzy.algorithms.Utils;

public class TokenSort
extends RatioAlgorithm {
    @Override
    public int apply(String s1, String s2, Ratio ratio, ToStringFunction<String> stringFunction) {
        String sorted1 = TokenSort.processAndSort(s1, stringFunction);
        String sorted2 = TokenSort.processAndSort(s2, stringFunction);
        return ratio.apply(sorted1, sorted2);
    }

    private static String processAndSort(String in, ToStringFunction<String> stringProcessor) {
        in = stringProcessor.apply(in);
        String[] wordsArray = in.split("\\s+");
        List<String> words = Arrays.asList(wordsArray);
        String joined = Utils.sortAndJoin(words, " ");
        return joined.trim();
    }
}

