/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.ratios;

import java.util.ArrayList;
import java.util.Collections;
import me.xdrop.diffutils.DiffUtils;
import me.xdrop.diffutils.structs.MatchingBlock;
import me.xdrop.fuzzywuzzy.Ratio;
import me.xdrop.fuzzywuzzy.ToStringFunction;

public class PartialRatio
implements Ratio {
    @Override
    public int apply(String s1, String s2) {
        String longer;
        String shorter;
        if (s1.length() <= s2.length()) {
            shorter = s1;
            longer = s2;
        } else {
            shorter = s2;
            longer = s1;
        }
        MatchingBlock[] matchingBlocks = DiffUtils.getMatchingBlocks(shorter, longer);
        ArrayList<Double> scores = new ArrayList<Double>();
        for (MatchingBlock mb : matchingBlocks) {
            String long_substr;
            double ratio;
            int dist = mb.dpos - mb.spos;
            int long_start = dist > 0 ? dist : 0;
            int long_end = long_start + shorter.length();
            if (long_end > longer.length()) {
                long_end = longer.length();
            }
            if ((ratio = DiffUtils.getRatio(shorter, long_substr = longer.substring(long_start, long_end))) > 0.995) {
                return 100;
            }
            scores.add(ratio);
        }
        return (int)Math.round(100.0 * (Double)Collections.max(scores));
    }

    @Override
    public int apply(String s1, String s2, ToStringFunction<String> sp) {
        return this.apply(sp.apply(s1), sp.apply(s2));
    }
}

