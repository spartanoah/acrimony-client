/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.async;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDelegate;
import org.apache.logging.log4j.core.async.AsyncQueueFullMessageUtil;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.spi.AbstractLogger;

@Plugin(name="asyncLogger", category="Core", printObject=true)
public class AsyncLoggerConfig
extends LoggerConfig {
    private static final ThreadLocal<Boolean> ASYNC_LOGGER_ENTERED = new ThreadLocal<Boolean>(){

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    private final AsyncLoggerConfigDelegate delegate;

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newAsyncBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    protected AsyncLoggerConfig(String name, List<AppenderRef> appenders, Filter filter, Level level, boolean additive, Property[] properties, Configuration config, boolean includeLocation) {
        super(name, appenders, filter, level, additive, properties, config, includeLocation);
        this.delegate = config.getAsyncLoggerConfigDelegate();
        this.delegate.setLogEventFactory(this.getLogEventFactory());
    }

    AsyncLoggerConfigDelegate getAsyncLoggerConfigDelegate() {
        return this.delegate;
    }

    @Override
    protected void log(LogEvent event, LoggerConfig.LoggerConfigPredicate predicate) {
        if (predicate == LoggerConfig.LoggerConfigPredicate.ALL && ASYNC_LOGGER_ENTERED.get() == Boolean.FALSE && this.hasAppenders()) {
            ASYNC_LOGGER_ENTERED.set(Boolean.TRUE);
            try {
                super.log(event, LoggerConfig.LoggerConfigPredicate.SYNCHRONOUS_ONLY);
                this.logToAsyncDelegate(event);
            } finally {
                ASYNC_LOGGER_ENTERED.set(Boolean.FALSE);
            }
        } else {
            super.log(event, predicate);
        }
    }

    @Override
    protected void callAppenders(LogEvent event) {
        super.callAppenders(event);
    }

    private void logToAsyncDelegate(LogEvent event) {
        if (!this.isFiltered(event)) {
            this.populateLazilyInitializedFields(event);
            if (!this.delegate.tryEnqueue(event, this)) {
                this.handleQueueFull(event);
            }
        }
    }

    private void handleQueueFull(LogEvent event) {
        if (AbstractLogger.getRecursionDepth() > 1) {
            AsyncQueueFullMessageUtil.logWarningToStatusLogger();
            this.logToAsyncLoggerConfigsOnCurrentThread(event);
        } else {
            EventRoute eventRoute = this.delegate.getEventRoute(event.getLevel());
            eventRoute.logMessage(this, event);
        }
    }

    private void populateLazilyInitializedFields(LogEvent event) {
        event.getSource();
        event.getThreadName();
    }

    void logInBackgroundThread(LogEvent event) {
        this.delegate.enqueueEvent(event, this);
    }

    void logToAsyncLoggerConfigsOnCurrentThread(LogEvent event) {
        this.log(event, LoggerConfig.LoggerConfigPredicate.ASYNCHRONOUS_ONLY);
    }

    private String displayName() {
        return "".equals(this.getName()) ? "root" : this.getName();
    }

    @Override
    public void start() {
        LOGGER.trace("AsyncLoggerConfig[{}] starting...", (Object)this.displayName());
        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(timeout, timeUnit, false);
        LOGGER.trace("AsyncLoggerConfig[{}] stopping...", (Object)this.displayName());
        this.setStopped();
        return true;
    }

    public RingBufferAdmin createRingBufferAdmin(String contextName) {
        return this.delegate.createRingBufferAdmin(contextName, this.getName());
    }

    @Deprecated
    public static LoggerConfig createLogger(String additivity, String levelName, String loggerName, String includeLocation, AppenderRef[] refs, Property[] properties, Configuration config, Filter filter) {
        Level level;
        if (loggerName == null) {
            LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        List<AppenderRef> appenderRefs = Arrays.asList(refs);
        try {
            level = Level.toLevel(levelName, Level.ERROR);
        } catch (Exception ex) {
            LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", (Object)levelName);
            level = Level.ERROR;
        }
        String name = loggerName.equals("root") ? "" : loggerName;
        boolean additive = Booleans.parseBoolean(additivity, true);
        return new AsyncLoggerConfig(name, appenderRefs, filter, level, additive, properties, config, AsyncLoggerConfig.includeLocation(includeLocation));
    }

    @Deprecated
    public static LoggerConfig createLogger(@PluginAttribute(value="additivity", defaultBoolean=true) boolean additivity, @PluginAttribute(value="level") Level level, @Required(message="Loggers cannot be configured without a name") @PluginAttribute(value="name") String loggerName, @PluginAttribute(value="includeLocation") String includeLocation, @PluginElement(value="AppenderRef") AppenderRef[] refs, @PluginElement(value="Properties") Property[] properties, @PluginConfiguration Configuration config, @PluginElement(value="Filter") Filter filter) {
        String name = loggerName.equals("root") ? "" : loggerName;
        return new AsyncLoggerConfig(name, Arrays.asList(refs), filter, level, additivity, properties, config, AsyncLoggerConfig.includeLocation(includeLocation));
    }

    protected static boolean includeLocation(String includeLocationConfigValue) {
        return Boolean.parseBoolean(includeLocationConfigValue);
    }

    @Plugin(name="asyncRoot", category="Core", printObject=true)
    public static class RootLogger
    extends LoggerConfig {
        @PluginBuilderFactory
        public static <B extends Builder<B>> B newAsyncRootBuilder() {
            return (B)((Builder)new Builder().asBuilder());
        }

        @Deprecated
        public static LoggerConfig createLogger(String additivity, String levelName, String includeLocation, AppenderRef[] refs, Property[] properties, Configuration config, Filter filter) {
            List<AppenderRef> appenderRefs = Arrays.asList(refs);
            Level level = null;
            try {
                level = Level.toLevel(levelName, Level.ERROR);
            } catch (Exception ex) {
                LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", (Object)levelName);
                level = Level.ERROR;
            }
            boolean additive = Booleans.parseBoolean(additivity, true);
            return new AsyncLoggerConfig("", appenderRefs, filter, level, additive, properties, config, AsyncLoggerConfig.includeLocation(includeLocation));
        }

        @Deprecated
        public static LoggerConfig createLogger(@PluginAttribute(value="additivity") String additivity, @PluginAttribute(value="level") Level level, @PluginAttribute(value="includeLocation") String includeLocation, @PluginElement(value="AppenderRef") AppenderRef[] refs, @PluginElement(value="Properties") Property[] properties, @PluginConfiguration Configuration config, @PluginElement(value="Filter") Filter filter) {
            List<AppenderRef> appenderRefs = Arrays.asList(refs);
            Level actualLevel = level == null ? Level.ERROR : level;
            boolean additive = Booleans.parseBoolean(additivity, true);
            return new AsyncLoggerConfig("", appenderRefs, filter, actualLevel, additive, properties, config, AsyncLoggerConfig.includeLocation(includeLocation));
        }

        public static class Builder<B extends Builder<B>>
        extends LoggerConfig.RootLogger.Builder<B> {
            @Override
            public LoggerConfig build() {
                LoggerConfig.LevelAndRefs container = RootLogger.getLevelAndRefs(this.getLevel(), this.getRefs(), this.getLevelAndRefs(), this.getConfig());
                return new AsyncLoggerConfig("", container.refs, this.getFilter(), container.level, this.isAdditivity(), this.getProperties(), this.getConfig(), RootLogger.includeLocation(this.getIncludeLocation()));
            }
        }
    }

    public static class Builder<B extends Builder<B>>
    extends LoggerConfig.Builder<B> {
        @Override
        public LoggerConfig build() {
            String name = this.getLoggerName().equals("root") ? "" : this.getLoggerName();
            LoggerConfig.LevelAndRefs container = AsyncLoggerConfig.getLevelAndRefs(this.getLevel(), this.getRefs(), this.getLevelAndRefs(), this.getConfig());
            return new AsyncLoggerConfig(name, container.refs, this.getFilter(), container.level, this.isAdditivity(), this.getProperties(), this.getConfig(), AsyncLoggerConfig.includeLocation(this.getIncludeLocation()));
        }
    }
}

