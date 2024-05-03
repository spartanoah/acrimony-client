/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

public interface ConfigFeature {
    public boolean enabledByDefault();

    public int getMask();

    public boolean enabledIn(int var1);
}

