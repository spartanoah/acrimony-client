/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy;

import java.util.Collection;
import java.util.List;
import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.Extractor;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.TokenSet;
import me.xdrop.fuzzywuzzy.algorithms.TokenSort;
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import me.xdrop.fuzzywuzzy.ratios.PartialRatio;
import me.xdrop.fuzzywuzzy.ratios.SimpleRatio;

public class FuzzySearch {
    public static int ratio(String s1, String s2) {
        return new SimpleRatio().apply(s1, s2);
    }

    public static int ratio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new SimpleRatio().apply(s1, s2, stringFunction);
    }

    public static int partialRatio(String s1, String s2) {
        return new PartialRatio().apply(s1, s2);
    }

    public static int partialRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new PartialRatio().apply(s1, s2, stringFunction);
    }

    public static int tokenSortPartialRatio(String s1, String s2) {
        return new TokenSort().apply(s1, s2, new PartialRatio());
    }

    public static int tokenSortPartialRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new TokenSort().apply(s1, s2, new PartialRatio(), stringFunction);
    }

    public static int tokenSortRatio(String s1, String s2) {
        return new TokenSort().apply(s1, s2, new SimpleRatio());
    }

    public static int tokenSortRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new TokenSort().apply(s1, s2, new SimpleRatio(), stringFunction);
    }

    public static int tokenSetRatio(String s1, String s2) {
        return new TokenSet().apply(s1, s2, new SimpleRatio());
    }

    public static int tokenSetRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new TokenSet().apply(s1, s2, new SimpleRatio(), stringFunction);
    }

    public static int tokenSetPartialRatio(String s1, String s2) {
        return new TokenSet().apply(s1, s2, new PartialRatio());
    }

    public static int tokenSetPartialRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new TokenSet().apply(s1, s2, new PartialRatio(), stringFunction);
    }

    public static int weightedRatio(String s1, String s2) {
        return new WeightedRatio().apply(s1, s2);
    }

    public static int weightedRatio(String s1, String s2, ToStringFunction<String> stringFunction) {
        return new WeightedRatio().apply(s1, s2, stringFunction);
    }

    public static List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func, int limit, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, func, limit);
    }

    public static List<ExtractedResult> extractTop(String query, Collection<String> choices, int limit, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, new WeightedRatio(), limit);
    }

    public static List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func, int limit) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, func, limit);
    }

    public static List<ExtractedResult> extractTop(String query, Collection<String> choices, int limit) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, new WeightedRatio(), limit);
    }

    public static List<ExtractedResult> extractSorted(String query, Collection<String> choices, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, func);
    }

    public static List<ExtractedResult> extractSorted(String query, Collection<String> choices, Applicable func, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, func);
    }

    public static List<ExtractedResult> extractSorted(String query, Collection<String> choices) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, new WeightedRatio());
    }

    public static List<ExtractedResult> extractSorted(String query, Collection<String> choices, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, new WeightedRatio());
    }

    public static List<ExtractedResult> extractAll(String query, Collection<String> choices, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractWithoutOrder(query, choices, func);
    }

    public static List<ExtractedResult> extractAll(String query, Collection<String> choices, Applicable func, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractWithoutOrder(query, choices, func);
    }

    public static List<ExtractedResult> extractAll(String query, Collection<String> choices) {
        Extractor extractor = new Extractor();
        return extractor.extractWithoutOrder(query, choices, new WeightedRatio());
    }

    public static List<ExtractedResult> extractAll(String query, Collection<String> choices, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractWithoutOrder(query, choices, new WeightedRatio());
    }

    public static ExtractedResult extractOne(String query, Collection<String> choices, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractOne(query, choices, func);
    }

    public static ExtractedResult extractOne(String query, Collection<String> choices) {
        Extractor extractor = new Extractor();
        return extractor.extractOne(query, choices, new WeightedRatio());
    }

    public static <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func, int limit, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, toStringFunction, func, limit);
    }

    public static <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, int limit, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, toStringFunction, new WeightedRatio(), limit);
    }

    public static <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func, int limit) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, toStringFunction, func, limit);
    }

    public static <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, int limit) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, toStringFunction, new WeightedRatio(), limit);
    }

    public static <T> List<BoundExtractedResult<T>> extractSorted(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, toStringFunction, func);
    }

    public static <T> List<BoundExtractedResult<T>> extractSorted(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, toStringFunction, func);
    }

    public static <T> List<BoundExtractedResult<T>> extractSorted(String query, Collection<T> choices, ToStringFunction<T> toStringFunction) {
        Extractor extractor = new Extractor();
        return extractor.extractTop(query, choices, toStringFunction, new WeightedRatio());
    }

    public static <T> List<BoundExtractedResult<T>> extractSorted(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractTop(query, choices, toStringFunction, new WeightedRatio());
    }

    public static <T> List<BoundExtractedResult<T>> extractAll(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractWithoutOrder(query, choices, toStringFunction, func);
    }

    public static <T> List<BoundExtractedResult<T>> extractAll(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractWithoutOrder(query, choices, toStringFunction, func);
    }

    public static <T> List<BoundExtractedResult<T>> extractAll(String query, Collection<T> choices, ToStringFunction<T> toStringFunction) {
        Extractor extractor = new Extractor();
        return extractor.extractWithoutOrder(query, choices, toStringFunction, new WeightedRatio());
    }

    public static <T> List<BoundExtractedResult<T>> extractAll(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, int cutoff) {
        Extractor extractor = new Extractor(cutoff);
        return extractor.extractWithoutOrder(query, choices, toStringFunction, new WeightedRatio());
    }

    public static <T> BoundExtractedResult<T> extractOne(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        Extractor extractor = new Extractor();
        return extractor.extractOne(query, choices, toStringFunction, func);
    }

    public static <T> BoundExtractedResult<T> extractOne(String query, Collection<T> choices, ToStringFunction<T> toStringFunction) {
        Extractor extractor = new Extractor();
        return extractor.extractOne(query, choices, toStringFunction, new WeightedRatio());
    }
}

