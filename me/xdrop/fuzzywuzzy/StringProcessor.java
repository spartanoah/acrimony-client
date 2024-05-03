/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy;

import me.xdrop.fuzzywuzzy.ToStringFunction;

@Deprecated
public abstract class StringProcessor
implements ToStringFunction<String> {
    @Deprecated
    public abstract String process(String var1);

    @Override
    public String apply(String item) {
        return this.process(item);
    }
}

