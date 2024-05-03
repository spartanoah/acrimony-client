/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class AbstractServer {
    private final LoggerContext context = (LoggerContext)LogManager.getContext(false);

    protected AbstractServer() {
    }

    protected void log(LogEvent event) {
        Logger logger = this.context.getLogger(event.getLoggerName());
        if (logger.config.filter(event.getLevel(), event.getMarker(), event.getMessage(), event.getThrown())) {
            logger.config.logEvent(event);
        }
    }
}

