/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.ToStringFunction;

public interface Ratio
extends Applicable {
    @Override
    public int apply(String var1, String var2);

    public int apply(String var1, String var2, ToStringFunction<String> var3);
}

