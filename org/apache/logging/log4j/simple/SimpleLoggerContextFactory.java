/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.simple;

import java.net.URI;
import org.apache.logging.log4j.simple.SimpleLoggerContext;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class SimpleLoggerContextFactory
implements LoggerContextFactory {
    public static final SimpleLoggerContextFactory INSTANCE = new SimpleLoggerContextFactory();

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext) {
        return SimpleLoggerContext.INSTANCE;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext, URI configLocation, String name) {
        return SimpleLoggerContext.INSTANCE;
    }

    @Override
    public void removeContext(LoggerContext removeContext) {
    }

    @Override
    public boolean isClassLoaderDependent() {
        return false;
    }
}

