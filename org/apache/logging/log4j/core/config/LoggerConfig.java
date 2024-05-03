/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderControlArraySet;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultReliabilityStrategy;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.impl.ReusableLogEventFactory;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="logger", category="Core", printObject=true)
public class LoggerConfig
extends AbstractFilterable
implements LocationAware {
    public static final String ROOT = "root";
    private static LogEventFactory LOG_EVENT_FACTORY = null;
    private List<AppenderRef> appenderRefs = new ArrayList<AppenderRef>();
    private final AppenderControlArraySet appenders = new AppenderControlArraySet();
    private final String name;
    private LogEventFactory logEventFactory = LOG_EVENT_FACTORY;
    private Level level;
    private boolean additive = true;
    private boolean includeLocation = true;
    private LoggerConfig parent;
    private Map<Property, Boolean> propertiesMap;
    private final List<Property> properties;
    private final boolean propertiesRequireLookup;
    private final Configuration config;
    private final ReliabilityStrategy reliabilityStrategy;

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder().asBuilder();
    }

    public LoggerConfig() {
        this.level = Level.ERROR;
        this.name = "";
        this.properties = null;
        this.propertiesRequireLookup = false;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }

    public LoggerConfig(String name, Level level, boolean additive) {
        this.name = name;
        this.level = level;
        this.additive = additive;
        this.properties = null;
        this.propertiesRequireLookup = false;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }

    protected LoggerConfig(String name, List<AppenderRef> appenders, Filter filter, Level level, boolean additive, Property[] properties, Configuration config, boolean includeLocation) {
        super(filter);
        this.name = name;
        this.appenderRefs = appenders;
        this.level = level;
        this.additive = additive;
        this.includeLocation = includeLocation;
        this.config = config;
        this.properties = properties != null && properties.length > 0 ? Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(properties, properties.length))) : null;
        this.propertiesRequireLookup = LoggerConfig.containsPropertyRequiringLookup(properties);
        this.reliabilityStrategy = config.getReliabilityStrategy(this);
    }

    private static boolean containsPropertyRequiringLookup(Property[] properties) {
        if (properties == null) {
            return false;
        }
        for (int i = 0; i < properties.length; ++i) {
            if (!properties[i].isValueNeedsLookup()) continue;
            return true;
        }
        return false;
    }

    @Override
    public Filter getFilter() {
        return super.getFilter();
    }

    public String getName() {
        return this.name;
    }

    public void setParent(LoggerConfig parent) {
        this.parent = parent;
    }

    public LoggerConfig getParent() {
        return this.parent;
    }

    public void addAppender(Appender appender, Level level, Filter filter) {
        this.appenders.add(new AppenderControl(appender, level, filter));
    }

    public void removeAppender(String name) {
        AppenderControl removed = null;
        while ((removed = this.appenders.remove(name)) != null) {
            this.cleanupFilter(removed);
        }
    }

    public Map<String, Appender> getAppenders() {
        return this.appenders.asMap();
    }

    protected void clearAppenders() {
        do {
            AppenderControl[] original;
            for (AppenderControl ctl : original = this.appenders.clear()) {
                this.cleanupFilter(ctl);
            }
        } while (!this.appenders.isEmpty());
    }

    private void cleanupFilter(AppenderControl ctl) {
        Filter filter = ctl.getFilter();
        if (filter != null) {
            ctl.removeFilter(filter);
            filter.stop();
        }
    }

    public List<AppenderRef> getAppenderRefs() {
        return this.appenderRefs;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level == null ? (this.parent == null ? Level.ERROR : this.parent.getLevel()) : this.level;
    }

    public LogEventFactory getLogEventFactory() {
        return this.logEventFactory;
    }

    public void setLogEventFactory(LogEventFactory logEventFactory) {
        this.logEventFactory = logEventFactory;
    }

    public boolean isAdditive() {
        return this.additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    @Deprecated
    public Map<Property, Boolean> getProperties() {
        if (this.properties == null) {
            return null;
        }
        if (this.propertiesMap == null) {
            HashMap<Property, Boolean> result = new HashMap<Property, Boolean>(this.properties.size() * 2);
            for (int i = 0; i < this.properties.size(); ++i) {
                result.put(this.properties.get(i), this.properties.get(i).isValueNeedsLookup());
            }
            this.propertiesMap = Collections.unmodifiableMap(result);
        }
        return this.propertiesMap;
    }

    public List<Property> getPropertyList() {
        return this.properties;
    }

    public boolean isPropertiesRequireLookup() {
        return this.propertiesRequireLookup;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @PerformanceSensitive(value={"allocation"})
    public void log(String loggerName, String fqcn, Marker marker, Level level, Message data, Throwable t) {
        List<Property> props = this.getProperties(loggerName, fqcn, marker, level, data, t);
        LogEvent logEvent = this.logEventFactory.createEvent(loggerName, marker, fqcn, this.location(fqcn), level, data, props, t);
        try {
            this.log(logEvent, LoggerConfigPredicate.ALL);
        } finally {
            ReusableLogEventFactory.release(logEvent);
        }
    }

    private StackTraceElement location(String fqcn) {
        return this.requiresLocation() ? StackLocatorUtil.calcLocation(fqcn) : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @PerformanceSensitive(value={"allocation"})
    public void log(String loggerName, String fqcn, StackTraceElement location, Marker marker, Level level, Message data, Throwable t) {
        List<Property> props = this.getProperties(loggerName, fqcn, marker, level, data, t);
        LogEvent logEvent = this.logEventFactory.createEvent(loggerName, marker, fqcn, location, level, data, props, t);
        try {
            this.log(logEvent, LoggerConfigPredicate.ALL);
        } finally {
            ReusableLogEventFactory.release(logEvent);
        }
    }

    private List<Property> getProperties(String loggerName, String fqcn, Marker marker, Level level, Message data, Throwable t) {
        List<Property> snapshot = this.properties;
        if (snapshot == null || !this.propertiesRequireLookup) {
            return snapshot;
        }
        return this.getPropertiesWithLookups(loggerName, fqcn, marker, level, data, t, snapshot);
    }

    private List<Property> getPropertiesWithLookups(String loggerName, String fqcn, Marker marker, Level level, Message data, Throwable t, List<Property> props) {
        ArrayList<Property> results = new ArrayList<Property>(props.size());
        Log4jLogEvent event = Log4jLogEvent.newBuilder().setMessage(data).setMarker(marker).setLevel(level).setLoggerName(loggerName).setLoggerFqcn(fqcn).setThrown(t).build();
        for (int i = 0; i < props.size(); ++i) {
            Property prop = props.get(i);
            String value = prop.evaluate(this.config.getStrSubstitutor());
            results.add(Property.createProperty(prop.getName(), prop.getRawValue(), value));
        }
        return results;
    }

    public void log(LogEvent event) {
        this.log(event, LoggerConfigPredicate.ALL);
    }

    protected void log(LogEvent event, LoggerConfigPredicate predicate) {
        if (!this.isFiltered(event)) {
            this.processLogEvent(event, predicate);
        }
    }

    public ReliabilityStrategy getReliabilityStrategy() {
        return this.reliabilityStrategy;
    }

    private void processLogEvent(LogEvent event, LoggerConfigPredicate predicate) {
        event.setIncludeLocation(this.isIncludeLocation());
        if (predicate.allow(this)) {
            this.callAppenders(event);
        }
        this.logParent(event, predicate);
    }

    @Override
    public boolean requiresLocation() {
        if (!this.includeLocation) {
            return false;
        }
        AppenderControl[] controls = this.appenders.get();
        LoggerConfig loggerConfig = this;
        while (loggerConfig != null) {
            for (AppenderControl control : controls) {
                Appender appender = control.getAppender();
                if (!(appender instanceof LocationAware) || !((LocationAware)((Object)appender)).requiresLocation()) continue;
                return true;
            }
            if (!loggerConfig.additive) break;
            loggerConfig = loggerConfig.parent;
            if (loggerConfig == null) continue;
            controls = loggerConfig.appenders.get();
        }
        return false;
    }

    private void logParent(LogEvent event, LoggerConfigPredicate predicate) {
        if (this.additive && this.parent != null) {
            this.parent.log(event, predicate);
        }
    }

    @PerformanceSensitive(value={"allocation"})
    protected void callAppenders(LogEvent event) {
        AppenderControl[] controls = this.appenders.get();
        for (int i = 0; i < controls.length; ++i) {
            controls[i].callAppender(event);
        }
    }

    public String toString() {
        return Strings.isEmpty(this.name) ? ROOT : this.name;
    }

    @Deprecated
    public static LoggerConfig createLogger(String additivity, Level level, @PluginAttribute(value="name") String loggerName, String includeLocation, AppenderRef[] refs, Property[] properties, @PluginConfiguration Configuration config, Filter filter) {
        if (loggerName == null) {
            LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        List<AppenderRef> appenderRefs = Arrays.asList(refs);
        String name = loggerName.equals(ROOT) ? "" : loggerName;
        boolean additive = Booleans.parseBoolean(additivity, true);
        return new LoggerConfig(name, appenderRefs, filter, level, additive, properties, config, LoggerConfig.includeLocation(includeLocation, config));
    }

    @Deprecated
    public static LoggerConfig createLogger(@PluginAttribute(value="additivity", defaultBoolean=true) boolean additivity, @PluginAttribute(value="level") Level level, @Required(message="Loggers cannot be configured without a name") @PluginAttribute(value="name") String loggerName, @PluginAttribute(value="includeLocation") String includeLocation, @PluginElement(value="AppenderRef") AppenderRef[] refs, @PluginElement(value="Properties") Property[] properties, @PluginConfiguration Configuration config, @PluginElement(value="Filter") Filter filter) {
        String name = loggerName.equals(ROOT) ? "" : loggerName;
        return new LoggerConfig(name, Arrays.asList(refs), filter, level, additivity, properties, config, LoggerConfig.includeLocation(includeLocation, config));
    }

    protected static boolean includeLocation(String includeLocationConfigValue) {
        return LoggerConfig.includeLocation(includeLocationConfigValue, null);
    }

    protected static boolean includeLocation(String includeLocationConfigValue, Configuration configuration) {
        if (includeLocationConfigValue == null) {
            LoggerContext context = null;
            if (configuration != null) {
                context = configuration.getLoggerContext();
            }
            if (context != null) {
                return !(context instanceof AsyncLoggerContext);
            }
            return !AsyncLoggerContextSelector.isSelected();
        }
        return Boolean.parseBoolean(includeLocationConfigValue);
    }

    protected final boolean hasAppenders() {
        return !this.appenders.isEmpty();
    }

    protected static LevelAndRefs getLevelAndRefs(Level level, AppenderRef[] refs, String levelAndRefs, Configuration config) {
        LevelAndRefs result = new LevelAndRefs();
        if (levelAndRefs != null) {
            if (config instanceof PropertiesConfiguration) {
                if (level != null) {
                    LOGGER.warn("Level is ignored when levelAndRefs syntax is used.");
                }
                if (refs != null && refs.length > 0) {
                    LOGGER.warn("Appender references are ignored when levelAndRefs syntax is used");
                }
                String[] parts = Strings.splitList(levelAndRefs);
                result.level = Level.getLevel(parts[0]);
                if (parts.length > 1) {
                    ArrayList<AppenderRef> refList = new ArrayList<AppenderRef>();
                    Arrays.stream(parts).skip(1L).forEach(ref -> refList.add(AppenderRef.createAppenderRef(ref, null, null)));
                    result.refs = refList;
                }
            } else {
                LOGGER.warn("levelAndRefs are only allowed in a properties configuration. The value is ignored.");
                result.level = level;
                result.refs = Arrays.asList(refs);
            }
        } else {
            result.level = level;
            result.refs = Arrays.asList(refs);
        }
        return result;
    }

    static {
        String factory = PropertiesUtil.getProperties().getStringProperty("Log4jLogEventFactory");
        if (factory != null) {
            try {
                Class<?> clazz = Loader.loadClass(factory);
                if (clazz != null && LogEventFactory.class.isAssignableFrom(clazz)) {
                    LOG_EVENT_FACTORY = (LogEventFactory)clazz.newInstance();
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to create LogEventFactory {}", (Object)factory, (Object)ex);
            }
        }
        if (LOG_EVENT_FACTORY == null) {
            LOG_EVENT_FACTORY = Constants.ENABLE_THREADLOCALS ? new ReusableLogEventFactory() : new DefaultLogEventFactory();
        }
    }

    protected static enum LoggerConfigPredicate {
        ALL{

            @Override
            boolean allow(LoggerConfig config) {
                return true;
            }
        }
        ,
        ASYNCHRONOUS_ONLY{

            @Override
            boolean allow(LoggerConfig config) {
                return config instanceof AsyncLoggerConfig;
            }
        }
        ,
        SYNCHRONOUS_ONLY{

            @Override
            boolean allow(LoggerConfig config) {
                return !ASYNCHRONOUS_ONLY.allow(config);
            }
        };


        abstract boolean allow(LoggerConfig var1);
    }

    protected static class LevelAndRefs {
        public Level level;
        public List<AppenderRef> refs;

        protected LevelAndRefs() {
        }
    }

    @Plugin(name="root", category="Core", printObject=true)
    public static class RootLogger
    extends LoggerConfig {
        @PluginBuilderFactory
        public static <B extends Builder<B>> B newRootBuilder() {
            return new Builder().asBuilder();
        }

        @Deprecated
        public static LoggerConfig createLogger(@PluginAttribute(value="additivity") String additivity, @PluginAttribute(value="level") Level level, @PluginAttribute(value="includeLocation") String includeLocation, @PluginElement(value="AppenderRef") AppenderRef[] refs, @PluginElement(value="Properties") Property[] properties, @PluginConfiguration Configuration config, @PluginElement(value="Filter") Filter filter) {
            List<AppenderRef> appenderRefs = Arrays.asList(refs);
            Level actualLevel = level == null ? Level.ERROR : level;
            boolean additive = Booleans.parseBoolean(additivity, true);
            return new LoggerConfig("", appenderRefs, filter, actualLevel, additive, properties, config, RootLogger.includeLocation(includeLocation, config));
        }

        public static class Builder<B extends Builder<B>>
        implements org.apache.logging.log4j.core.util.Builder<LoggerConfig> {
            @PluginBuilderAttribute
            private boolean additivity;
            @PluginBuilderAttribute
            private Level level;
            @PluginBuilderAttribute
            private String levelAndRefs;
            @PluginBuilderAttribute
            private String includeLocation;
            @PluginElement(value="AppenderRef")
            private AppenderRef[] refs;
            @PluginElement(value="Properties")
            private Property[] properties;
            @PluginConfiguration
            private Configuration config;
            @PluginElement(value="Filter")
            private Filter filter;

            public boolean isAdditivity() {
                return this.additivity;
            }

            public B withAdditivity(boolean additivity) {
                this.additivity = additivity;
                return this.asBuilder();
            }

            public Level getLevel() {
                return this.level;
            }

            public B withLevel(Level level) {
                this.level = level;
                return this.asBuilder();
            }

            public String getLevelAndRefs() {
                return this.levelAndRefs;
            }

            public B withLevelAndRefs(String levelAndRefs) {
                this.levelAndRefs = levelAndRefs;
                return this.asBuilder();
            }

            public String getIncludeLocation() {
                return this.includeLocation;
            }

            public B withIncludeLocation(String includeLocation) {
                this.includeLocation = includeLocation;
                return this.asBuilder();
            }

            public AppenderRef[] getRefs() {
                return this.refs;
            }

            public B withRefs(AppenderRef[] refs) {
                this.refs = refs;
                return this.asBuilder();
            }

            public Property[] getProperties() {
                return this.properties;
            }

            public B withProperties(Property[] properties) {
                this.properties = properties;
                return this.asBuilder();
            }

            public Configuration getConfig() {
                return this.config;
            }

            public B withConfig(Configuration config) {
                this.config = config;
                return this.asBuilder();
            }

            public Filter getFilter() {
                return this.filter;
            }

            public B withtFilter(Filter filter) {
                this.filter = filter;
                return this.asBuilder();
            }

            @Override
            public LoggerConfig build() {
                LevelAndRefs container = LoggerConfig.getLevelAndRefs(this.level, this.refs, this.levelAndRefs, this.config);
                return new LoggerConfig("", container.refs, this.filter, container.level, this.additivity, this.properties, this.config, LoggerConfig.includeLocation(this.includeLocation, this.config));
            }

            public B asBuilder() {
                return (B)this;
            }
        }
    }

    public static class Builder<B extends Builder<B>>
    implements org.apache.logging.log4j.core.util.Builder<LoggerConfig> {
        @PluginBuilderAttribute
        private Boolean additivity;
        @PluginBuilderAttribute
        private Level level;
        @PluginBuilderAttribute
        private String levelAndRefs;
        @PluginBuilderAttribute(value="name")
        @Required(message="Loggers cannot be configured without a name")
        private String loggerName;
        @PluginBuilderAttribute
        private String includeLocation;
        @PluginElement(value="AppenderRef")
        private AppenderRef[] refs;
        @PluginElement(value="Properties")
        private Property[] properties;
        @PluginConfiguration
        private Configuration config;
        @PluginElement(value="Filter")
        private Filter filter;

        public boolean isAdditivity() {
            return this.additivity == null || this.additivity != false;
        }

        public B withAdditivity(boolean additivity) {
            this.additivity = additivity;
            return this.asBuilder();
        }

        public Level getLevel() {
            return this.level;
        }

        public B withLevel(Level level) {
            this.level = level;
            return this.asBuilder();
        }

        public String getLevelAndRefs() {
            return this.levelAndRefs;
        }

        public B withLevelAndRefs(String levelAndRefs) {
            this.levelAndRefs = levelAndRefs;
            return this.asBuilder();
        }

        public String getLoggerName() {
            return this.loggerName;
        }

        public B withLoggerName(String loggerName) {
            this.loggerName = loggerName;
            return this.asBuilder();
        }

        public String getIncludeLocation() {
            return this.includeLocation;
        }

        public B withIncludeLocation(String includeLocation) {
            this.includeLocation = includeLocation;
            return this.asBuilder();
        }

        public AppenderRef[] getRefs() {
            return this.refs;
        }

        public B withRefs(AppenderRef[] refs) {
            this.refs = refs;
            return this.asBuilder();
        }

        public Property[] getProperties() {
            return this.properties;
        }

        public B withProperties(Property[] properties) {
            this.properties = properties;
            return this.asBuilder();
        }

        public Configuration getConfig() {
            return this.config;
        }

        public B withConfig(Configuration config) {
            this.config = config;
            return this.asBuilder();
        }

        public Filter getFilter() {
            return this.filter;
        }

        public B withtFilter(Filter filter) {
            this.filter = filter;
            return this.asBuilder();
        }

        @Override
        public LoggerConfig build() {
            String name = this.loggerName.equals(LoggerConfig.ROOT) ? "" : this.loggerName;
            LevelAndRefs container = LoggerConfig.getLevelAndRefs(this.level, this.refs, this.levelAndRefs, this.config);
            boolean useLocation = LoggerConfig.includeLocation(this.includeLocation, this.config);
            return new LoggerConfig(name, container.refs, this.filter, container.level, this.isAdditivity(), this.properties, this.config, useLocation);
        }

        public B asBuilder() {
            return (B)this;
        }
    }
}

