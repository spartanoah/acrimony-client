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
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.message.Message;

public class DefaultLogEventFactory
implements LogEventFactory,
LocationAwareLogEventFactory {
    private static final DefaultLogEventFactory instance = new DefaultLogEventFactory();

    public static DefaultLogEventFactory getInstance() {
        return instance;
    }

    @Override
    public LogEvent createEvent(String loggerName, Marker marker, String fqcn, Level level, Message data, List<Property> properties, Throwable t) {
        return new Log4jLogEvent(loggerName, marker, fqcn, level, data, properties, t);
    }

    @Override
    public LogEvent createEvent(String loggerName, Marker marker, String fqcn, StackTraceElement location, Level level, Message data, List<Property> properties, Throwable t) {
        return new Log4jLogEvent(loggerName, marker, fqcn, location, level, data, properties, t);
    }
}

