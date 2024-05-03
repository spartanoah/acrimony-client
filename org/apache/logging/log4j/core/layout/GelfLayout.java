/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.layout.internal.ExcludeChecker;
import org.apache.logging.log4j.core.layout.internal.IncludeChecker;
import org.apache.logging.log4j.core.layout.internal.ListChecker;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.net.Severity;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.TriConsumer;

@Plugin(name="GelfLayout", category="Core", elementType="layout", printObject=true)
public final class GelfLayout
extends AbstractStringLayout {
    private static final char C = ',';
    private static final int COMPRESSION_THRESHOLD = 1024;
    private static final char Q = '\"';
    private static final String QC = "\",";
    private static final String QU = "\"_";
    private final KeyValuePair[] additionalFields;
    private final int compressionThreshold;
    private final CompressionType compressionType;
    private final String host;
    private final boolean includeStacktrace;
    private final boolean includeThreadContext;
    private final boolean includeMapMessage;
    private final boolean includeNullDelimiter;
    private final boolean includeNewLineDelimiter;
    private final boolean omitEmptyFields;
    private final PatternLayout layout;
    private final FieldWriter mdcWriter;
    private final FieldWriter mapWriter;
    private static final ThreadLocal<StringBuilder> messageStringBuilder = new ThreadLocal();
    private static final ThreadLocal<StringBuilder> timestampStringBuilder = new ThreadLocal();

    @Deprecated
    public GelfLayout(String host, KeyValuePair[] additionalFields, CompressionType compressionType, int compressionThreshold, boolean includeStacktrace) {
        this(null, host, additionalFields, compressionType, compressionThreshold, includeStacktrace, true, true, false, false, false, null, null, null, "", "");
    }

    private GelfLayout(Configuration config, String host, KeyValuePair[] additionalFields, CompressionType compressionType, int compressionThreshold, boolean includeStacktrace, boolean includeThreadContext, boolean includeMapMessage, boolean includeNullDelimiter, boolean includeNewLineDelimiter, boolean omitEmptyFields, ListChecker mdcChecker, ListChecker mapChecker, PatternLayout patternLayout, String mdcPrefix, String mapPrefix) {
        super(config, StandardCharsets.UTF_8, null, null);
        this.host = host != null ? host : NetUtils.getLocalHostname();
        KeyValuePair[] keyValuePairArray = this.additionalFields = additionalFields != null ? additionalFields : KeyValuePair.EMPTY_ARRAY;
        if (config == null) {
            for (KeyValuePair additionalField : this.additionalFields) {
                if (!GelfLayout.valueNeedsLookup(additionalField.getValue())) continue;
                throw new IllegalArgumentException("configuration needs to be set when there are additional fields with variables");
            }
        }
        this.compressionType = compressionType;
        this.compressionThreshold = compressionThreshold;
        this.includeStacktrace = includeStacktrace;
        this.includeThreadContext = includeThreadContext;
        this.includeMapMessage = includeMapMessage;
        this.includeNullDelimiter = includeNullDelimiter;
        this.includeNewLineDelimiter = includeNewLineDelimiter;
        this.omitEmptyFields = omitEmptyFields;
        if (includeNullDelimiter && compressionType != CompressionType.OFF) {
            throw new IllegalArgumentException("null delimiter cannot be used with compression");
        }
        this.mdcWriter = new FieldWriter(mdcChecker, mdcPrefix);
        this.mapWriter = new FieldWriter(mapChecker, mapPrefix);
        this.layout = patternLayout;
    }

    public String toString() {
        String mapVars;
        StringBuilder sb = new StringBuilder();
        sb.append("host=").append(this.host);
        sb.append(", compressionType=").append(this.compressionType.toString());
        sb.append(", compressionThreshold=").append(this.compressionThreshold);
        sb.append(", includeStackTrace=").append(this.includeStacktrace);
        sb.append(", includeThreadContext=").append(this.includeThreadContext);
        sb.append(", includeNullDelimiter=").append(this.includeNullDelimiter);
        sb.append(", includeNewLineDelimiter=").append(this.includeNewLineDelimiter);
        String threadVars = this.mdcWriter.getChecker().toString();
        if (threadVars.length() > 0) {
            sb.append(", ").append(threadVars);
        }
        if ((mapVars = this.mapWriter.getChecker().toString()).length() > 0) {
            sb.append(", ").append(mapVars);
        }
        if (this.layout != null) {
            sb.append(", PatternLayout{").append(this.layout.toString()).append("}");
        }
        return sb.toString();
    }

    @Deprecated
    public static GelfLayout createLayout(@PluginAttribute(value="host") String host, @PluginElement(value="AdditionalField") KeyValuePair[] additionalFields, @PluginAttribute(value="compressionType", defaultString="GZIP") CompressionType compressionType, @PluginAttribute(value="compressionThreshold", defaultInt=1024) int compressionThreshold, @PluginAttribute(value="includeStacktrace", defaultBoolean=true) boolean includeStacktrace) {
        return new GelfLayout(null, host, additionalFields, compressionType, compressionThreshold, includeStacktrace, true, true, false, false, false, null, null, null, "", "");
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    @Override
    public Map<String, String> getContentFormat() {
        return Collections.emptyMap();
    }

    @Override
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        StringBuilder text = this.toText(event, GelfLayout.getStringBuilder(), false);
        byte[] bytes = this.getBytes(text.toString());
        return this.compressionType != CompressionType.OFF && bytes.length > this.compressionThreshold ? this.compress(bytes) : bytes;
    }

    @Override
    public void encode(LogEvent event, ByteBufferDestination destination) {
        if (this.compressionType != CompressionType.OFF) {
            super.encode(event, destination);
            return;
        }
        StringBuilder text = this.toText(event, GelfLayout.getStringBuilder(), true);
        Encoder<StringBuilder> helper = this.getStringBuilderEncoder();
        helper.encode(text, destination);
    }

    @Override
    public boolean requiresLocation() {
        return Objects.nonNull(this.layout) && this.layout.requiresLocation();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private byte[] compress(byte[] bytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(this.compressionThreshold / 8);
            try (DeflaterOutputStream stream = this.compressionType.createDeflaterOutputStream(baos);){
                if (stream == null) {
                    byte[] byArray = bytes;
                    return byArray;
                }
                stream.write(bytes);
                stream.finish();
                return baos.toByteArray();
            }
        } catch (IOException e) {
            StatusLogger.getLogger().error(e);
            return bytes;
        }
    }

    @Override
    public String toSerializable(LogEvent event) {
        StringBuilder text = this.toText(event, GelfLayout.getStringBuilder(), false);
        return text.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private StringBuilder toText(LogEvent event, StringBuilder builder, boolean gcFree) {
        builder.append('{');
        builder.append("\"version\":\"1.1\",");
        builder.append("\"host\":\"");
        JsonUtils.quoteAsString(GelfLayout.toNullSafeString(this.host), builder);
        builder.append(QC);
        builder.append("\"timestamp\":").append(GelfLayout.formatTimestamp(event.getTimeMillis())).append(',');
        builder.append("\"level\":").append(this.formatLevel(event.getLevel())).append(',');
        if (event.getThreadName() != null) {
            builder.append("\"_thread\":\"");
            JsonUtils.quoteAsString(event.getThreadName(), builder);
            builder.append(QC);
        }
        if (event.getLoggerName() != null) {
            builder.append("\"_logger\":\"");
            JsonUtils.quoteAsString(event.getLoggerName(), builder);
            builder.append(QC);
        }
        if (this.additionalFields.length > 0) {
            StrSubstitutor strSubstitutor = this.getConfiguration().getStrSubstitutor();
            for (KeyValuePair additionalField : this.additionalFields) {
                String value2;
                String string = value2 = GelfLayout.valueNeedsLookup(additionalField.getValue()) ? strSubstitutor.replace(event, additionalField.getValue()) : additionalField.getValue();
                if (!Strings.isNotEmpty(value2) && this.omitEmptyFields) continue;
                builder.append(QU);
                JsonUtils.quoteAsString(additionalField.getKey(), builder);
                builder.append("\":\"");
                JsonUtils.quoteAsString(GelfLayout.toNullSafeString(value2), builder);
                builder.append(QC);
            }
        }
        if (this.includeThreadContext) {
            event.getContextData().forEach(this.mdcWriter, builder);
        }
        if (this.includeMapMessage && event.getMessage() instanceof MapMessage) {
            ((MapMessage)event.getMessage()).forEach((key, value) -> this.mapWriter.accept((String)key, value, builder));
        }
        if (event.getThrown() != null || this.layout != null) {
            builder.append("\"full_message\":\"");
            if (this.layout != null) {
                StringBuilder messageBuffer = GelfLayout.getMessageStringBuilder();
                this.layout.serialize(event, messageBuffer);
                JsonUtils.quoteAsString(messageBuffer, builder);
            } else if (this.includeStacktrace) {
                JsonUtils.quoteAsString(GelfLayout.formatThrowable(event.getThrown()), builder);
            } else {
                JsonUtils.quoteAsString(event.getThrown().toString(), builder);
            }
            builder.append(QC);
        }
        builder.append("\"short_message\":\"");
        Message message = event.getMessage();
        if (message instanceof CharSequence) {
            JsonUtils.quoteAsString((CharSequence)((Object)message), builder);
        } else if (gcFree && message instanceof StringBuilderFormattable) {
            StringBuilder messageBuffer = GelfLayout.getMessageStringBuilder();
            try {
                ((StringBuilderFormattable)((Object)message)).formatTo(messageBuffer);
                JsonUtils.quoteAsString(messageBuffer, builder);
            } finally {
                GelfLayout.trimToMaxSize(messageBuffer);
            }
        } else {
            JsonUtils.quoteAsString(GelfLayout.toNullSafeString(message.getFormattedMessage()), builder);
        }
        builder.append('\"');
        builder.append('}');
        if (this.includeNullDelimiter) {
            builder.append('\u0000');
        }
        if (this.includeNewLineDelimiter) {
            builder.append('\n');
        }
        return builder;
    }

    private static boolean valueNeedsLookup(String value) {
        return value != null && value.contains("${");
    }

    private static StringBuilder getMessageStringBuilder() {
        StringBuilder result = messageStringBuilder.get();
        if (result == null) {
            result = new StringBuilder(1024);
            messageStringBuilder.set(result);
        }
        result.setLength(0);
        return result;
    }

    private static CharSequence toNullSafeString(CharSequence s) {
        return s == null ? "" : s;
    }

    static CharSequence formatTimestamp(long timeMillis) {
        if (timeMillis < 1000L) {
            return "0";
        }
        StringBuilder builder = GelfLayout.getTimestampStringBuilder();
        builder.append(timeMillis);
        builder.insert(builder.length() - 3, '.');
        return builder;
    }

    private static StringBuilder getTimestampStringBuilder() {
        StringBuilder result = timestampStringBuilder.get();
        if (result == null) {
            result = new StringBuilder(20);
            timestampStringBuilder.set(result);
        }
        result.setLength(0);
        return result;
    }

    private int formatLevel(Level level) {
        return Severity.getSeverity(level).getCode();
    }

    static CharSequence formatThrowable(Throwable throwable) {
        StringWriter sw = new StringWriter(2048);
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.getBuffer();
    }

    private class FieldWriter
    implements TriConsumer<String, Object, StringBuilder> {
        private final ListChecker checker;
        private final String prefix;

        FieldWriter(ListChecker checker, String prefix) {
            this.checker = checker;
            this.prefix = prefix;
        }

        @Override
        public void accept(String key, Object value, StringBuilder stringBuilder) {
            String stringValue = String.valueOf(value);
            if (this.checker.check(key) && (Strings.isNotEmpty(stringValue) || !GelfLayout.this.omitEmptyFields)) {
                stringBuilder.append(GelfLayout.QU);
                JsonUtils.quoteAsString(Strings.concat(this.prefix, key), stringBuilder);
                stringBuilder.append("\":\"");
                JsonUtils.quoteAsString(GelfLayout.toNullSafeString(stringValue), stringBuilder);
                stringBuilder.append(GelfLayout.QC);
            }
        }

        public ListChecker getChecker() {
            return this.checker;
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractStringLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<GelfLayout> {
        @PluginBuilderAttribute
        private String host;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;
        @PluginBuilderAttribute
        private CompressionType compressionType = CompressionType.GZIP;
        @PluginBuilderAttribute
        private int compressionThreshold = 1024;
        @PluginBuilderAttribute
        private boolean includeStacktrace = true;
        @PluginBuilderAttribute
        private boolean includeThreadContext = true;
        @PluginBuilderAttribute
        private boolean includeNullDelimiter;
        @PluginBuilderAttribute
        private boolean includeNewLineDelimiter;
        @PluginBuilderAttribute
        private String threadContextIncludes;
        @PluginBuilderAttribute
        private String threadContextExcludes;
        @PluginBuilderAttribute
        private String mapMessageIncludes;
        @PluginBuilderAttribute
        private String mapMessageExcludes;
        @PluginBuilderAttribute
        private boolean includeMapMessage = true;
        @PluginBuilderAttribute
        private boolean omitEmptyFields;
        @PluginBuilderAttribute
        private String messagePattern;
        @PluginBuilderAttribute
        private String threadContextPrefix = "";
        @PluginBuilderAttribute
        private String mapPrefix = "";
        @PluginElement(value="PatternSelector")
        private PatternSelector patternSelector;

        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        @Override
        public GelfLayout build() {
            ListChecker mdcChecker = this.createChecker(this.threadContextExcludes, this.threadContextIncludes);
            ListChecker mapChecker = this.createChecker(this.mapMessageExcludes, this.mapMessageIncludes);
            PatternLayout patternLayout = null;
            if (this.messagePattern != null && this.patternSelector != null) {
                AbstractLayout.LOGGER.error("A message pattern and PatternSelector cannot both be specified on GelfLayout, ignoring message pattern");
                this.messagePattern = null;
            }
            if (this.messagePattern != null) {
                patternLayout = PatternLayout.newBuilder().withPattern(this.messagePattern).withAlwaysWriteExceptions(this.includeStacktrace).withConfiguration(this.getConfiguration()).build();
            }
            if (this.patternSelector != null) {
                patternLayout = PatternLayout.newBuilder().withPatternSelector(this.patternSelector).withAlwaysWriteExceptions(this.includeStacktrace).withConfiguration(this.getConfiguration()).build();
            }
            return new GelfLayout(this.getConfiguration(), this.host, this.additionalFields, this.compressionType, this.compressionThreshold, this.includeStacktrace, this.includeThreadContext, this.includeMapMessage, this.includeNullDelimiter, this.includeNewLineDelimiter, this.omitEmptyFields, mdcChecker, mapChecker, patternLayout, this.threadContextPrefix, this.mapPrefix);
        }

        private ListChecker createChecker(String excludes, String includes) {
            String[] array;
            ListChecker checker = null;
            if (excludes != null && (array = excludes.split(Patterns.COMMA_SEPARATOR)).length > 0) {
                ArrayList<String> excludeList = new ArrayList<String>(array.length);
                for (String str : array) {
                    excludeList.add(str.trim());
                }
                checker = new ExcludeChecker(excludeList);
            }
            if (includes != null && (array = includes.split(Patterns.COMMA_SEPARATOR)).length > 0) {
                ArrayList<String> includeList = new ArrayList<String>(array.length);
                for (String str : array) {
                    includeList.add(str.trim());
                }
                checker = new IncludeChecker(includeList);
            }
            if (checker == null) {
                checker = ListChecker.NOOP_CHECKER;
            }
            return checker;
        }

        public String getHost() {
            return this.host;
        }

        public CompressionType getCompressionType() {
            return this.compressionType;
        }

        public int getCompressionThreshold() {
            return this.compressionThreshold;
        }

        public boolean isIncludeStacktrace() {
            return this.includeStacktrace;
        }

        public boolean isIncludeThreadContext() {
            return this.includeThreadContext;
        }

        public boolean isIncludeNullDelimiter() {
            return this.includeNullDelimiter;
        }

        public boolean isIncludeNewLineDelimiter() {
            return this.includeNewLineDelimiter;
        }

        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        public B setHost(String host) {
            this.host = host;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompressionThreshold(int compressionThreshold) {
            this.compressionThreshold = compressionThreshold;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeStacktrace(boolean includeStacktrace) {
            this.includeStacktrace = includeStacktrace;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeThreadContext(boolean includeThreadContext) {
            this.includeThreadContext = includeThreadContext;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeNullDelimiter(boolean includeNullDelimiter) {
            this.includeNullDelimiter = includeNullDelimiter;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeNewLineDelimiter(boolean includeNewLineDelimiter) {
            this.includeNewLineDelimiter = includeNewLineDelimiter;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdditionalFields(KeyValuePair[] additionalFields) {
            this.additionalFields = additionalFields;
            return (B)((Builder)this.asBuilder());
        }

        public B setMessagePattern(String pattern) {
            this.messagePattern = pattern;
            return (B)((Builder)this.asBuilder());
        }

        public B setPatternSelector(PatternSelector patternSelector) {
            this.patternSelector = patternSelector;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcIncludes(String mdcIncludes) {
            this.threadContextIncludes = mdcIncludes;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcExcludes(String mdcExcludes) {
            this.threadContextExcludes = mdcExcludes;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeMapMessage(boolean includeMapMessage) {
            this.includeMapMessage = includeMapMessage;
            return (B)((Builder)this.asBuilder());
        }

        public B setMapMessageIncludes(String mapMessageIncludes) {
            this.mapMessageIncludes = mapMessageIncludes;
            return (B)((Builder)this.asBuilder());
        }

        public B setMapMessageExcludes(String mapMessageExcludes) {
            this.mapMessageExcludes = mapMessageExcludes;
            return (B)((Builder)this.asBuilder());
        }

        public B setThreadContextPrefix(String prefix) {
            if (prefix != null) {
                this.threadContextPrefix = prefix;
            }
            return (B)((Builder)this.asBuilder());
        }

        public B setMapPrefix(String prefix) {
            if (prefix != null) {
                this.mapPrefix = prefix;
            }
            return (B)((Builder)this.asBuilder());
        }
    }

    public static enum CompressionType {
        GZIP{

            @Override
            public DeflaterOutputStream createDeflaterOutputStream(OutputStream os) throws IOException {
                return new GZIPOutputStream(os);
            }
        }
        ,
        ZLIB{

            @Override
            public DeflaterOutputStream createDeflaterOutputStream(OutputStream os) throws IOException {
                return new DeflaterOutputStream(os);
            }
        }
        ,
        OFF{

            @Override
            public DeflaterOutputStream createDeflaterOutputStream(OutputStream os) throws IOException {
                return null;
            }
        };


        public abstract DeflaterOutputStream createDeflaterOutputStream(OutputStream var1) throws IOException;
    }
}

