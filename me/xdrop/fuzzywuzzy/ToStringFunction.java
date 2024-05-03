/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy;

public interface ToStringFunction<T> {
    public static final ToStringFunction<String> NO_PROCESS = new ToStringFunction<String>(){

        @Override
        public String apply(String item) {
            return item;
        }
    };

    public String apply(T var1);
}

