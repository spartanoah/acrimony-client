/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import me.xdrop.fuzzywuzzy.Ratio;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.BasicAlgorithm;
import me.xdrop.fuzzywuzzy.ratios.SimpleRatio;

public abstract class RatioAlgorithm
extends BasicAlgorithm {
    private Ratio ratio;

    public RatioAlgorithm() {
        this.ratio = new SimpleRatio();
    }

    public RatioAlgorithm(ToStringFunction<String> stringFunction) {
        super(stringFunction);
    }

    public RatioAlgorithm(Ratio ratio) {
        this.ratio = ratio;
    }

    public RatioAlgorithm(ToStringFunction<String> stringFunction, Ratio ratio) {
        super(stringFunction);
        this.ratio = ratio;
    }

    public abstract int apply(String var1, String var2, Ratio var3, ToStringFunction<String> var4);

    public RatioAlgorithm with(Ratio ratio) {
        this.setRatio(ratio);
        return this;
    }

    public int apply(String s1, String s2, Ratio ratio) {
        return this.apply(s1, s2, ratio, this.getStringFunction());
    }

    @Override
    public int apply(String s1, String s2, ToStringFunction<String> stringFunction) {
        return this.apply(s1, s2, this.getRatio(), stringFunction);
    }

    public void setRatio(Ratio ratio) {
        this.ratio = ratio;
    }

    public Ratio getRatio() {
        return this.ratio;
    }
}

