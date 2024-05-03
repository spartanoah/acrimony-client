/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.plugins;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Loggers;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="loggers", category="Core")
public final class LoggersPlugin {
    private LoggersPlugin() {
    }

    @PluginFactory
    public static Loggers createLoggers(@PluginElement(value="Loggers") LoggerConfig[] loggers) {
        ConcurrentHashMap<String, LoggerConfig> loggerMap = new ConcurrentHashMap<String, LoggerConfig>();
        LoggerConfig root = null;
        for (LoggerConfig logger : loggers) {
            if (logger == null) continue;
            if (logger.getName().isEmpty()) {
                root = logger;
            }
            loggerMap.put(logger.getName(), logger);
        }
        return new Loggers(loggerMap, root);
    }
}

