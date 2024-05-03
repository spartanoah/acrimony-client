/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.text.Replaceable;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeMatcher;

public abstract class UnicodeFilter
implements UnicodeMatcher {
    public abstract boolean contains(int var1);

    public int matches(Replaceable text, int[] offset, int limit, boolean incremental) {
        int c;
        if (offset[0] < limit && this.contains(c = text.char32At(offset[0]))) {
            offset[0] = offset[0] + UTF16.getCharCount(c);
            return 2;
        }
        if (offset[0] > limit && this.contains(text.char32At(offset[0]))) {
            offset[0] = offset[0] - 1;
            if (offset[0] >= 0) {
                offset[0] = offset[0] - (UTF16.getCharCount(text.char32At(offset[0])) - 1);
            }
            return 2;
        }
        if (incremental && offset[0] == limit) {
            return 1;
        }
        return 0;
    }

    protected UnicodeFilter() {
    }
}

