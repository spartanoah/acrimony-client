/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.ratios;

import me.xdrop.diffutils.DiffUtils;
import me.xdrop.fuzzywuzzy.Ratio;
import me.xdrop.fuzzywuzzy.ToStringFunction;

public class SimpleRatio
implements Ratio {
    @Override
    public int apply(String s1, String s2) {
        return (int)Math.round(100.0 * DiffUtils.getRatio(s1, s2));
    }

    @Override
    public int apply(String s1, String s2, ToStringFunction<String> sp) {
        return this.apply(sp.apply(s1), sp.apply(s2));
    }
}

