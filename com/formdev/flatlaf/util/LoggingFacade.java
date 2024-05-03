/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.util.LoggingFacadeImpl;

public interface LoggingFacade {
    public static final LoggingFacade INSTANCE = new LoggingFacadeImpl();

    public void logSevere(String var1, Throwable var2);

    public void logConfig(String var1, Throwable var2);
}

