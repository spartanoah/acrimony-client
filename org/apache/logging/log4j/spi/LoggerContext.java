/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerRegistry;

public interface LoggerContext {
    public static final LoggerContext[] EMPTY_ARRAY = new LoggerContext[0];

    public Object getExternalContext();

    default public ExtendedLogger getLogger(Class<?> cls) {
        String canonicalName = cls.getCanonicalName();
        return this.getLogger(canonicalName != null ? canonicalName : cls.getName());
    }

    default public ExtendedLogger getLogger(Class<?> cls, MessageFactory messageFactory) {
        String canonicalName = cls.getCanonicalName();
        return this.getLogger(canonicalName != null ? canonicalName : cls.getName(), messageFactory);
    }

    public ExtendedLogger getLogger(String var1);

    public ExtendedLogger getLogger(String var1, MessageFactory var2);

    default public LoggerRegistry<? extends Logger> getLoggerRegistry() {
        return null;
    }

    default public Object getObject(String key) {
        return null;
    }

    public boolean hasLogger(String var1);

    public boolean hasLogger(String var1, Class<? extends MessageFactory> var2);

    public boolean hasLogger(String var1, MessageFactory var2);

    default public Object putObject(String key, Object value) {
        return null;
    }

    default public Object putObjectIfAbsent(String key, Object value) {
        return null;
    }

    default public Object removeObject(String key) {
        return null;
    }

    default public boolean removeObject(String key, Object value) {
        return false;
    }
}

