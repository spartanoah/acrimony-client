/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import java.util.Arrays;
import java.util.Collection;

public interface Callback {
    public static final String METHOD_NAME = "callback";
    public static final Collection FORBIDDEN_NAMES = Arrays.asList("hashCode", "equals", "toString");

    public static interface UncaughtExceptionHandler {
        public void uncaughtException(Callback var1, Throwable var2);
    }
}

