/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.properties;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterableComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggableComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptFileComponentBuilder;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public class PropertiesConfigurationBuilder
extends ConfigurationBuilderFactory
implements Builder<PropertiesConfiguration> {
    private static final String ADVERTISER_KEY = "advertiser";
    private static final String STATUS_KEY = "status";
    private static final String SHUTDOWN_HOOK = "shutdownHook";
    private static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";
    private static final String VERBOSE = "verbose";
    private static final String DEST = "dest";
    private static final String PACKAGES = "packages";
    private static final String CONFIG_NAME = "name";
    private static final String MONITOR_INTERVAL = "monitorInterval";
    private static final String CONFIG_TYPE = "type";
    private final ConfigurationBuilder<PropertiesConfiguration> builder = PropertiesConfigurationBuilder.newConfigurationBuilder(PropertiesConfiguration.class);
    private LoggerContext loggerContext;
    private Properties rootProperties;

    public PropertiesConfigurationBuilder setRootProperties(Properties rootProperties) {
        this.rootProperties = rootProperties;
        return this;
    }

    public PropertiesConfigurationBuilder setConfigurationSource(ConfigurationSource source) {
        this.builder.setConfigurationSource(source);
        return this;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public PropertiesConfiguration build() {
        String loggerProp;
        String appenderProp;
        String name;
        String string;
        Object type;
        for (String string2 : this.rootProperties.stringPropertyNames()) {
            if (string2.contains(".")) continue;
            this.builder.addRootProperty(string2, this.rootProperties.getProperty(string2));
        }
        this.builder.setStatusLevel(Level.toLevel(this.rootProperties.getProperty(STATUS_KEY), Level.ERROR)).setShutdownHook(this.rootProperties.getProperty(SHUTDOWN_HOOK)).setShutdownTimeout(Long.parseLong(this.rootProperties.getProperty(SHUTDOWN_TIMEOUT, "0")), TimeUnit.MILLISECONDS).setVerbosity(this.rootProperties.getProperty(VERBOSE)).setDestination(this.rootProperties.getProperty(DEST)).setPackages(this.rootProperties.getProperty(PACKAGES)).setConfigurationName(this.rootProperties.getProperty(CONFIG_NAME)).setMonitorInterval(this.rootProperties.getProperty(MONITOR_INTERVAL, "0")).setAdvertiser(this.rootProperties.getProperty(ADVERTISER_KEY));
        Properties propertyPlaceholders = PropertiesUtil.extractSubset(this.rootProperties, "property");
        for (String string3 : propertyPlaceholders.stringPropertyNames()) {
            this.builder.addProperty(string3, propertyPlaceholders.getProperty(string3));
        }
        Map<String, Properties> map = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "script"));
        for (Map.Entry<String, Properties> entry : map.entrySet()) {
            Properties scriptProps = entry.getValue();
            type = (String[])scriptProps.remove(CONFIG_TYPE);
            if (type == null) {
                throw new ConfigurationException("No type provided for script - must be Script or ScriptFile");
            }
            if (((String)type).equalsIgnoreCase("script")) {
                this.builder.add(this.createScript(scriptProps));
                continue;
            }
            this.builder.add(this.createScriptFile(scriptProps));
        }
        Properties properties = PropertiesUtil.extractSubset(this.rootProperties, "customLevel");
        if (properties.size() > 0) {
            for (String key : properties.stringPropertyNames()) {
                this.builder.add(this.builder.newCustomLevel(key, Integers.parseInt(properties.getProperty(key))));
            }
        }
        if ((string = this.rootProperties.getProperty("filters")) != null) {
            void var8_24;
            String[] filterNames = string.split(",");
            type = filterNames;
            int n = ((String[])type).length;
            boolean bl = false;
            while (var8_24 < n) {
                Object object = type[var8_24];
                name = ((String)object).trim();
                this.builder.add(this.createFilter(name, PropertiesUtil.extractSubset(this.rootProperties, "filter." + name)));
                ++var8_24;
            }
        } else {
            Map<String, Properties> filters = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "filter"));
            for (Map.Entry entry : filters.entrySet()) {
                this.builder.add(this.createFilter(((String)entry.getKey()).trim(), (Properties)entry.getValue()));
            }
        }
        if ((appenderProp = this.rootProperties.getProperty("appenders")) != null) {
            void var9_33;
            String[] appenderNames;
            String[] stringArray = appenderNames = appenderProp.split(",");
            int n = stringArray.length;
            boolean bl = false;
            while (var9_33 < n) {
                String appenderName = stringArray[var9_33];
                String name2 = appenderName.trim();
                this.builder.add(this.createAppender(appenderName.trim(), PropertiesUtil.extractSubset(this.rootProperties, "appender." + name2)));
                ++var9_33;
            }
        } else {
            Map<String, Properties> appenders = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "appender"));
            for (Map.Entry<String, Properties> entry : appenders.entrySet()) {
                this.builder.add(this.createAppender(entry.getKey().trim(), entry.getValue()));
            }
        }
        if ((loggerProp = this.rootProperties.getProperty("loggers")) != null) {
            String[] stringArray;
            for (String loggerName : stringArray = loggerProp.split(",")) {
                String name3 = loggerName.trim();
                if (name3.equals("root")) continue;
                this.builder.add(this.createLogger(name3, PropertiesUtil.extractSubset(this.rootProperties, "logger." + name3)));
            }
        } else {
            Map<String, Properties> map2 = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "logger"), true);
            for (Map.Entry<String, Properties> entry : map2.entrySet()) {
                name = entry.getKey().trim();
                if (name.equals("root")) continue;
                this.builder.add(this.createLogger(name, entry.getValue()));
            }
        }
        String string4 = this.rootProperties.getProperty("rootLogger");
        Properties properties2 = PropertiesUtil.extractSubset(this.rootProperties, "rootLogger");
        if (string4 != null) {
            properties2.setProperty("", string4);
            this.rootProperties.remove("rootLogger");
        }
        if (properties2.size() > 0) {
            this.builder.add(this.createRootLogger(properties2));
        }
        this.builder.setLoggerContext(this.loggerContext);
        return this.builder.build(false);
    }

    private ScriptComponentBuilder createScript(Properties properties) {
        String name = (String)properties.remove(CONFIG_NAME);
        String language = (String)properties.remove("language");
        String text = (String)properties.remove("text");
        ScriptComponentBuilder scriptBuilder = this.builder.newScript(name, language, text);
        return PropertiesConfigurationBuilder.processRemainingProperties(scriptBuilder, properties);
    }

    private ScriptFileComponentBuilder createScriptFile(Properties properties) {
        String name = (String)properties.remove(CONFIG_NAME);
        String path = (String)properties.remove("path");
        ScriptFileComponentBuilder scriptFileBuilder = this.builder.newScriptFile(name, path);
        return PropertiesConfigurationBuilder.processRemainingProperties(scriptFileBuilder, properties);
    }

    private AppenderComponentBuilder createAppender(String key, Properties properties) {
        String name = (String)properties.remove(CONFIG_NAME);
        if (Strings.isEmpty(name)) {
            throw new ConfigurationException("No name attribute provided for Appender " + key);
        }
        String type = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Appender " + key);
        }
        AppenderComponentBuilder appenderBuilder = this.builder.newAppender(name, type);
        this.addFiltersToComponent(appenderBuilder, properties);
        Properties layoutProps = PropertiesUtil.extractSubset(properties, "layout");
        if (layoutProps.size() > 0) {
            appenderBuilder.add(this.createLayout(name, layoutProps));
        }
        return PropertiesConfigurationBuilder.processRemainingProperties(appenderBuilder, properties);
    }

    private FilterComponentBuilder createFilter(String key, Properties properties) {
        String type = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Filter " + key);
        }
        String onMatch = (String)properties.remove("onMatch");
        String onMismatch = (String)properties.remove("onMismatch");
        FilterComponentBuilder filterBuilder = this.builder.newFilter(type, onMatch, onMismatch);
        return PropertiesConfigurationBuilder.processRemainingProperties(filterBuilder, properties);
    }

    private AppenderRefComponentBuilder createAppenderRef(String key, Properties properties) {
        String ref = (String)properties.remove("ref");
        if (Strings.isEmpty(ref)) {
            throw new ConfigurationException("No ref attribute provided for AppenderRef " + key);
        }
        AppenderRefComponentBuilder appenderRefBuilder = this.builder.newAppenderRef(ref);
        String level = Strings.trimToNull((String)properties.remove("level"));
        if (!Strings.isEmpty(level)) {
            appenderRefBuilder.addAttribute("level", level);
        }
        return this.addFiltersToComponent(appenderRefBuilder, properties);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private LoggerComponentBuilder createLogger(String key, Properties properties) {
        LoggerComponentBuilder loggerBuilder;
        String levelAndRefs = properties.getProperty("");
        String name = (String)properties.remove(CONFIG_NAME);
        String location = (String)properties.remove("includeLocation");
        if (Strings.isEmpty(name)) {
            throw new ConfigurationException("No name attribute provided for Logger " + key);
        }
        String level = Strings.trimToNull((String)properties.remove("level"));
        String type = (String)properties.remove(CONFIG_TYPE);
        if (type != null) {
            if (!type.equalsIgnoreCase("asyncLogger")) throw new ConfigurationException("Unknown Logger type " + type + " for Logger " + name);
            if (location != null) {
                boolean includeLocation = Boolean.parseBoolean(location);
                loggerBuilder = this.builder.newAsyncLogger(name, level, includeLocation);
            } else {
                loggerBuilder = this.builder.newAsyncLogger(name, level);
            }
        } else if (location != null) {
            boolean includeLocation = Boolean.parseBoolean(location);
            loggerBuilder = this.builder.newLogger(name, level, includeLocation);
        } else {
            loggerBuilder = this.builder.newLogger(name, level);
        }
        this.addLoggersToComponent(loggerBuilder, properties);
        this.addFiltersToComponent(loggerBuilder, properties);
        String additivity = (String)properties.remove("additivity");
        if (!Strings.isEmpty(additivity)) {
            loggerBuilder.addAttribute("additivity", additivity);
        }
        if (levelAndRefs == null) return loggerBuilder;
        loggerBuilder.addAttribute("levelAndRefs", levelAndRefs);
        return loggerBuilder;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private RootLoggerComponentBuilder createRootLogger(Properties properties) {
        RootLoggerComponentBuilder loggerBuilder;
        String levelAndRefs = properties.getProperty("");
        String level = Strings.trimToNull((String)properties.remove("level"));
        String type = (String)properties.remove(CONFIG_TYPE);
        String location = (String)properties.remove("includeLocation");
        if (type != null) {
            if (!type.equalsIgnoreCase("asyncRoot")) throw new ConfigurationException("Unknown Logger type for root logger" + type);
            if (location != null) {
                boolean includeLocation = Boolean.parseBoolean(location);
                loggerBuilder = this.builder.newAsyncRootLogger(level, includeLocation);
            } else {
                loggerBuilder = this.builder.newAsyncRootLogger(level);
            }
        } else if (location != null) {
            boolean includeLocation = Boolean.parseBoolean(location);
            loggerBuilder = this.builder.newRootLogger(level, includeLocation);
        } else {
            loggerBuilder = this.builder.newRootLogger(level);
        }
        this.addLoggersToComponent(loggerBuilder, properties);
        if (levelAndRefs == null) return this.addFiltersToComponent(loggerBuilder, properties);
        loggerBuilder.addAttribute("levelAndRefs", levelAndRefs);
        return this.addFiltersToComponent(loggerBuilder, properties);
    }

    private LayoutComponentBuilder createLayout(String appenderName, Properties properties) {
        String type = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Layout on Appender " + appenderName);
        }
        LayoutComponentBuilder layoutBuilder = this.builder.newLayout(type);
        return PropertiesConfigurationBuilder.processRemainingProperties(layoutBuilder, properties);
    }

    private static <B extends ComponentBuilder<B>> ComponentBuilder<B> createComponent(ComponentBuilder<?> parent, String key, Properties properties) {
        String name = (String)properties.remove(CONFIG_NAME);
        String type = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for component " + key);
        }
        ComponentBuilder componentBuilder = parent.getBuilder().newComponent(name, type);
        return PropertiesConfigurationBuilder.processRemainingProperties(componentBuilder, properties);
    }

    private static <B extends ComponentBuilder<?>> B processRemainingProperties(B builder, Properties properties) {
        while (properties.size() > 0) {
            String propertyName = properties.stringPropertyNames().iterator().next();
            int index = propertyName.indexOf(46);
            if (index > 0) {
                String prefix = propertyName.substring(0, index);
                Properties componentProperties = PropertiesUtil.extractSubset(properties, prefix);
                builder.addComponent(PropertiesConfigurationBuilder.createComponent(builder, prefix, componentProperties));
                continue;
            }
            builder.addAttribute(propertyName, properties.getProperty(propertyName));
            properties.remove(propertyName);
        }
        return builder;
    }

    private <B extends FilterableComponentBuilder<? extends ComponentBuilder<?>>> B addFiltersToComponent(B componentBuilder, Properties properties) {
        Map<String, Properties> filters = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(properties, "filter"));
        for (Map.Entry<String, Properties> entry : filters.entrySet()) {
            componentBuilder.add(this.createFilter(entry.getKey().trim(), entry.getValue()));
        }
        return componentBuilder;
    }

    private <B extends LoggableComponentBuilder<? extends ComponentBuilder<?>>> B addLoggersToComponent(B loggerBuilder, Properties properties) {
        Map<String, Properties> appenderRefs = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(properties, "appenderRef"));
        for (Map.Entry<String, Properties> entry : appenderRefs.entrySet()) {
            loggerBuilder.add(this.createAppenderRef(entry.getKey().trim(), entry.getValue()));
        }
        return loggerBuilder;
    }

    public PropertiesConfigurationBuilder setLoggerContext(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        return this;
    }

    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }
}

