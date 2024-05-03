/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

public interface FormatFeature {
    public boolean enabledByDefault();

    public int getMask();

    public boolean enabledIn(int var1);
}

