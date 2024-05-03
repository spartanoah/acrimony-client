/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.algorithms.DefaultStringFunction;

public abstract class BasicAlgorithm
implements Applicable {
    private ToStringFunction<String> stringFunction;

    public BasicAlgorithm() {
        this.stringFunction = new DefaultStringFunction();
    }

    public BasicAlgorithm(ToStringFunction<String> stringFunction) {
        this.stringFunction = stringFunction;
    }

    public abstract int apply(String var1, String var2, ToStringFunction<String> var3);

    @Override
    public int apply(String s1, String s2) {
        return this.apply(s1, s2, this.stringFunction);
    }

    public BasicAlgorithm with(ToStringFunction<String> stringFunction) {
        this.setStringFunction(stringFunction);
        return this;
    }

    public BasicAlgorithm noProcessor() {
        this.stringFunction = ToStringFunction.NO_PROCESS;
        return this;
    }

    void setStringFunction(ToStringFunction<String> stringFunction) {
        this.stringFunction = stringFunction;
    }

    public ToStringFunction<String> getStringFunction() {
        return this.stringFunction;
    }
}

