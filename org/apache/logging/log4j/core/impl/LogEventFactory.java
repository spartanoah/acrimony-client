/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.impl;

import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.LocationAwareLogEventFactory;
import org.apache.logging.log4j.message.Message;

public interface LogEventFactory
extends LocationAwareLogEventFactory {
    public LogEvent createEvent(String var1, Marker var2, String var3, Level var4, Message var5, List<Property> var6, Throwable var7);

    @Override
    default public LogEvent createEvent(String loggerName, Marker marker, String fqcn, StackTraceElement location, Level level, Message data, List<Property> properties, Throwable t) {
        return this.createEvent(loggerName, marker, fqcn, level, data, properties, t);
    }
}

