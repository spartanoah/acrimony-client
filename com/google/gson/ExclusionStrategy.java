/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.gson;

import com.google.gson.FieldAttributes;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface ExclusionStrategy {
    public boolean shouldSkipField(FieldAttributes var1);

    public boolean shouldSkipClass(Class<?> var1);
}

