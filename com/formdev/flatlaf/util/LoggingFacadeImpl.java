/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.lang.System$Logger
 *  java.lang.System$Logger$Level
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.LoggingFacade;

class LoggingFacadeImpl
implements LoggingFacade {
    private static final System.Logger LOG = System.getLogger((String)FlatLaf.class.getName());

    LoggingFacadeImpl() {
    }

    @Override
    public void logSevere(String message, Throwable t) {
        LOG.log(System.Logger.Level.ERROR, message, t);
    }

    @Override
    public void logConfig(String message, Throwable t) {
        LOG.log(System.Logger.Level.DEBUG, message, t);
    }
}

