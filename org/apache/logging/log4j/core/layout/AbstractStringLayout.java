/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.layout.StringBuilderEncoder;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.StringEncoder;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StringBuilders;

public abstract class AbstractStringLayout
extends AbstractLayout<String>
implements StringLayout,
LocationAware {
    protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    protected static final int MAX_STRING_BUILDER_SIZE = Math.max(1024, AbstractStringLayout.size("log4j.layoutStringBuilder.maxSize", 2048));
    private static final ThreadLocal<StringBuilder> threadLocal = new ThreadLocal();
    private Encoder<StringBuilder> textEncoder;
    private final Charset charset;
    private final Serializer footerSerializer;
    private final Serializer headerSerializer;

    @Override
    public boolean requiresLocation() {
        return false;
    }

    protected static StringBuilder getStringBuilder() {
        if (AbstractLogger.getRecursionDepth() > 1) {
            return new StringBuilder(1024);
        }
        StringBuilder result = threadLocal.get();
        if (result == null) {
            result = new StringBuilder(1024);
            threadLocal.set(result);
        }
        AbstractStringLayout.trimToMaxSize(result);
        result.setLength(0);
        return result;
    }

    private static int size(String property, int defaultValue) {
        return PropertiesUtil.getProperties().getIntegerProperty(property, defaultValue);
    }

    protected static void trimToMaxSize(StringBuilder stringBuilder) {
        StringBuilders.trimToMaxSize(stringBuilder, MAX_STRING_BUILDER_SIZE);
    }

    protected AbstractStringLayout(Charset charset) {
        this(charset, (byte[])null, (byte[])null);
    }

    protected AbstractStringLayout(Charset aCharset, byte[] header, byte[] footer) {
        super(null, header, footer);
        this.headerSerializer = null;
        this.footerSerializer = null;
        this.charset = aCharset == null ? StandardCharsets.UTF_8 : aCharset;
        this.textEncoder = Constants.ENABLE_DIRECT_ENCODERS ? new StringBuilderEncoder(this.charset) : null;
    }

    protected AbstractStringLayout(Configuration config, Charset aCharset, Serializer headerSerializer, Serializer footerSerializer) {
        super(config, null, null);
        this.headerSerializer = headerSerializer;
        this.footerSerializer = footerSerializer;
        this.charset = aCharset == null ? StandardCharsets.UTF_8 : aCharset;
        this.textEncoder = Constants.ENABLE_DIRECT_ENCODERS ? new StringBuilderEncoder(this.charset) : null;
    }

    protected byte[] getBytes(String s) {
        return s.getBytes(this.charset);
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public byte[] getFooter() {
        return this.serializeToBytes(this.footerSerializer, super.getFooter());
    }

    public Serializer getFooterSerializer() {
        return this.footerSerializer;
    }

    @Override
    public byte[] getHeader() {
        return this.serializeToBytes(this.headerSerializer, super.getHeader());
    }

    public Serializer getHeaderSerializer() {
        return this.headerSerializer;
    }

    private DefaultLogEventFactory getLogEventFactory() {
        return DefaultLogEventFactory.getInstance();
    }

    protected Encoder<StringBuilder> getStringBuilderEncoder() {
        if (this.textEncoder == null) {
            this.textEncoder = new StringBuilderEncoder(this.getCharset());
        }
        return this.textEncoder;
    }

    protected byte[] serializeToBytes(Serializer serializer, byte[] defaultValue) {
        String serializable = this.serializeToString(serializer);
        if (serializable == null) {
            return defaultValue;
        }
        return StringEncoder.toBytes(serializable, this.getCharset());
    }

    protected String serializeToString(Serializer serializer) {
        Level level;
        String loggerName;
        if (serializer == null) {
            return null;
        }
        if (this.configuration != null) {
            LoggerConfig rootLogger = this.configuration.getRootLogger();
            loggerName = rootLogger.getName();
            level = rootLogger.getLevel();
        } else {
            loggerName = "";
            level = AbstractConfiguration.getDefaultLevel();
        }
        LogEvent logEvent = this.getLogEventFactory().createEvent(loggerName, null, "", level, null, null, null);
        return serializer.toSerializable(logEvent);
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        return this.getBytes((String)this.toSerializable(event));
    }

    public static interface Serializer2 {
        public StringBuilder toSerializable(LogEvent var1, StringBuilder var2);
    }

    public static interface Serializer
    extends Serializer2 {
        public String toSerializable(LogEvent var1);

        @Override
        default public StringBuilder toSerializable(LogEvent event, StringBuilder builder) {
            builder.append(this.toSerializable(event));
            return builder;
        }
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractLayout.Builder<B> {
        @PluginBuilderAttribute(value="charset")
        private Charset charset;
        @PluginElement(value="footerSerializer")
        private Serializer footerSerializer;
        @PluginElement(value="headerSerializer")
        private Serializer headerSerializer;

        public Charset getCharset() {
            return this.charset;
        }

        public Serializer getFooterSerializer() {
            return this.footerSerializer;
        }

        public Serializer getHeaderSerializer() {
            return this.headerSerializer;
        }

        public B setCharset(Charset charset) {
            this.charset = charset;
            return (B)((Builder)this.asBuilder());
        }

        public B setFooterSerializer(Serializer footerSerializer) {
            this.footerSerializer = footerSerializer;
            return (B)((Builder)this.asBuilder());
        }

        public B setHeaderSerializer(Serializer headerSerializer) {
            this.headerSerializer = headerSerializer;
            return (B)((Builder)this.asBuilder());
        }
    }
}

