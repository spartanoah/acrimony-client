/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.snbt.impl.v1_14;

import com.viaversion.viaversion.libs.mcstructs.snbt.impl.v1_12.SNbtSerializer_v1_12;

public class SNbtSerializer_v1_14
extends SNbtSerializer_v1_12 {
    @Override
    protected String escape(String s) {
        char[] chars;
        StringBuilder out = new StringBuilder(" ");
        int openQuotation = 0;
        for (int n : chars = s.toCharArray()) {
            if (n == 92) {
                out.append("\\");
            } else if (n == 34 || n == 39) {
                if (openQuotation == 0) {
                    openQuotation = n == 34 ? 39 : 34;
                }
                if (openQuotation == n) {
                    out.append("\\");
                }
            }
            out.append((char)n);
        }
        if (openQuotation == 0) {
            openQuotation = 34;
        }
        out.setCharAt(0, (char)openQuotation);
        out.append((char)openQuotation);
        return out.toString();
    }
}

