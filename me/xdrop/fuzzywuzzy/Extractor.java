/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.Utils;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

public class Extractor {
    private int cutoff;

    public Extractor() {
        this.cutoff = 0;
    }

    public Extractor(int cutoff) {
        this.cutoff = cutoff;
    }

    public Extractor with(int cutoff) {
        this.setCutoff(cutoff);
        return this;
    }

    public List<ExtractedResult> extractWithoutOrder(String query, Collection<String> choices, Applicable func) {
        ArrayList<ExtractedResult> yields = new ArrayList<ExtractedResult>();
        int index = 0;
        for (String s : choices) {
            int score = func.apply(query, s);
            if (score >= this.cutoff) {
                yields.add(new ExtractedResult(s, score, index));
            }
            ++index;
        }
        return yields;
    }

    public <T> List<BoundExtractedResult<T>> extractWithoutOrder(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        ArrayList<BoundExtractedResult<T>> yields = new ArrayList<BoundExtractedResult<T>>();
        int index = 0;
        for (T t : choices) {
            String s = toStringFunction.apply(t);
            int score = func.apply(query, s);
            if (score >= this.cutoff) {
                yields.add(new BoundExtractedResult<T>(t, s, score, index));
            }
            ++index;
        }
        return yields;
    }

    public ExtractedResult extractOne(String query, Collection<String> choices, Applicable func) {
        List<ExtractedResult> extracted = this.extractWithoutOrder(query, choices, func);
        return Collections.max(extracted);
    }

    public <T> BoundExtractedResult<T> extractOne(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        List<BoundExtractedResult<T>> extracted = this.extractWithoutOrder(query, choices, toStringFunction, func);
        return Collections.max(extracted);
    }

    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func) {
        List<ExtractedResult> best = this.extractWithoutOrder(query, choices, func);
        Collections.sort(best, Collections.reverseOrder());
        return best;
    }

    public <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func) {
        List<BoundExtractedResult<T>> best = this.extractWithoutOrder(query, choices, toStringFunction, func);
        Collections.sort(best, Collections.reverseOrder());
        return best;
    }

    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func, int limit) {
        List<ExtractedResult> best = this.extractWithoutOrder(query, choices, func);
        List<ExtractedResult> results = Utils.findTopKHeap(best, limit);
        Collections.reverse(results);
        return results;
    }

    public <T> List<BoundExtractedResult<T>> extractTop(String query, Collection<T> choices, ToStringFunction<T> toStringFunction, Applicable func, int limit) {
        List<BoundExtractedResult<T>> best = this.extractWithoutOrder(query, choices, toStringFunction, func);
        List<BoundExtractedResult<T>> results = Utils.findTopKHeap(best, limit);
        Collections.reverse(results);
        return results;
    }

    public int getCutoff() {
        return this.cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }
}

