/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.LoggerContextAccessor;

public class DefaultLoggerContextAccessor
implements LoggerContextAccessor {
    public static DefaultLoggerContextAccessor INSTANCE = new DefaultLoggerContextAccessor();

    @Override
    public LoggerContext getLoggerContext() {
        return LoggerContext.getContext();
    }
}

