/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.DefaultErrorHandler;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Integers;

public abstract class AbstractAppender
extends AbstractFilterable
implements Appender,
LocationAware {
    private final String name;
    private final boolean ignoreExceptions;
    private final Layout<? extends Serializable> layout;
    private ErrorHandler handler = new DefaultErrorHandler(this);

    public static int parseInt(String s, int defaultValue) {
        try {
            return Integers.parseInt(s, defaultValue);
        } catch (NumberFormatException e) {
            LOGGER.error("Could not parse \"{}\" as an integer,  using default value {}: {}", (Object)s, (Object)defaultValue, (Object)e);
            return defaultValue;
        }
    }

    @Override
    public boolean requiresLocation() {
        return this.layout instanceof LocationAware && ((LocationAware)((Object)this.layout)).requiresLocation();
    }

    @Deprecated
    protected AbstractAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        this(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @Deprecated
    protected AbstractAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        this(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    protected AbstractAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(filter, properties);
        this.name = Objects.requireNonNull(name, "name");
        this.layout = layout;
        this.ignoreExceptions = ignoreExceptions;
    }

    public void error(String msg) {
        this.handler.error(msg);
    }

    public void error(String msg, LogEvent event, Throwable t) {
        this.handler.error(msg, event, t);
    }

    public void error(String msg, Throwable t) {
        this.handler.error(msg, t);
    }

    @Override
    public ErrorHandler getHandler() {
        return this.handler;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return this.layout;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean ignoreExceptions() {
        return this.ignoreExceptions;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
        if (handler == null) {
            LOGGER.error("The handler cannot be set to null");
            return;
        }
        if (this.isStarted()) {
            LOGGER.error("The handler cannot be changed once the appender is started");
            return;
        }
        this.handler = handler;
    }

    protected Serializable toSerializable(LogEvent event) {
        return this.layout != null ? this.layout.toSerializable(event) : null;
    }

    public String toString() {
        return this.name;
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractFilterable.Builder<B> {
        @PluginBuilderAttribute
        private boolean ignoreExceptions = true;
        @PluginElement(value="Layout")
        private Layout<? extends Serializable> layout;
        @PluginBuilderAttribute
        @Required(message="No appender name provided")
        private String name;
        @PluginConfiguration
        private Configuration configuration;

        public Configuration getConfiguration() {
            return this.configuration;
        }

        public Layout<? extends Serializable> getLayout() {
            return this.layout;
        }

        public String getName() {
            return this.name;
        }

        public Layout<? extends Serializable> getOrCreateLayout() {
            if (this.layout == null) {
                return PatternLayout.createDefaultLayout(this.configuration);
            }
            return this.layout;
        }

        public Layout<? extends Serializable> getOrCreateLayout(Charset charset) {
            if (this.layout == null) {
                return PatternLayout.newBuilder().withCharset(charset).withConfiguration(this.configuration).build();
            }
            return this.layout;
        }

        public boolean isIgnoreExceptions() {
            return this.ignoreExceptions;
        }

        public B setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return (B)((Builder)this.asBuilder());
        }

        public B setIgnoreExceptions(boolean ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
            return (B)((Builder)this.asBuilder());
        }

        public B setLayout(Layout<? extends Serializable> layout) {
            this.layout = layout;
            return (B)((Builder)this.asBuilder());
        }

        public B setName(String name) {
            this.name = name;
            return (B)((Builder)this.asBuilder());
        }

        @Deprecated
        public B withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return (B)((Builder)this.asBuilder());
        }

        @Deprecated
        public B withIgnoreExceptions(boolean ignoreExceptions) {
            return this.setIgnoreExceptions(ignoreExceptions);
        }

        @Deprecated
        public B withLayout(Layout<? extends Serializable> layout) {
            return this.setLayout(layout);
        }

        @Deprecated
        public B withName(String name) {
            return this.setName(name);
        }

        public String getErrorPrefix() {
            Class<?> appenderClass = this.getClass().getEnclosingClass();
            String name = this.getName();
            StringBuilder sb = new StringBuilder(appenderClass != null ? appenderClass.getSimpleName() : "Appender");
            if (name != null) {
                sb.append(" '").append(name).append("'");
            }
            return sb.toString();
        }
    }
}

