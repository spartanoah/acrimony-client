/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

abstract class AbstractJacksonLayout
extends AbstractStringLayout {
    protected static final String DEFAULT_EOL = "\r\n";
    protected static final String COMPACT_EOL = "";
    protected final String eol;
    protected final ObjectWriter objectWriter;
    protected final boolean compact;
    protected final boolean complete;
    protected final boolean includeNullDelimiter;
    protected final ResolvableKeyValuePair[] additionalFields;

    @Deprecated
    protected AbstractJacksonLayout(Configuration config, ObjectWriter objectWriter, Charset charset, boolean compact, boolean complete, boolean eventEol, AbstractStringLayout.Serializer headerSerializer, AbstractStringLayout.Serializer footerSerializer) {
        this(config, objectWriter, charset, compact, complete, eventEol, headerSerializer, footerSerializer, false);
    }

    @Deprecated
    protected AbstractJacksonLayout(Configuration config, ObjectWriter objectWriter, Charset charset, boolean compact, boolean complete, boolean eventEol, AbstractStringLayout.Serializer headerSerializer, AbstractStringLayout.Serializer footerSerializer, boolean includeNullDelimiter) {
        this(config, objectWriter, charset, compact, complete, eventEol, null, headerSerializer, footerSerializer, includeNullDelimiter, null);
    }

    protected AbstractJacksonLayout(Configuration config, ObjectWriter objectWriter, Charset charset, boolean compact, boolean complete, boolean eventEol, String endOfLine, AbstractStringLayout.Serializer headerSerializer, AbstractStringLayout.Serializer footerSerializer, boolean includeNullDelimiter, KeyValuePair[] additionalFields) {
        super(config, charset, headerSerializer, footerSerializer);
        this.objectWriter = objectWriter;
        this.compact = compact;
        this.complete = complete;
        this.eol = endOfLine != null ? endOfLine : (compact && !eventEol ? COMPACT_EOL : DEFAULT_EOL);
        this.includeNullDelimiter = includeNullDelimiter;
        this.additionalFields = AbstractJacksonLayout.prepareAdditionalFields(config, additionalFields);
    }

    protected static boolean valueNeedsLookup(String value) {
        return value != null && value.contains("${");
    }

    private static ResolvableKeyValuePair[] prepareAdditionalFields(Configuration config, KeyValuePair[] additionalFields) {
        if (additionalFields == null || additionalFields.length == 0) {
            return ResolvableKeyValuePair.EMPTY_ARRAY;
        }
        ResolvableKeyValuePair[] resolvableFields = new ResolvableKeyValuePair[additionalFields.length];
        for (int i = 0; i < additionalFields.length; ++i) {
            ResolvableKeyValuePair resolvable = resolvableFields[i] = new ResolvableKeyValuePair(additionalFields[i]);
            if (config != null || !resolvable.valueNeedsLookup) continue;
            throw new IllegalArgumentException("configuration needs to be set when there are additional fields with variables");
        }
        return resolvableFields;
    }

    @Override
    public String toSerializable(LogEvent event) {
        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            this.toSerializable(event, writer);
            return writer.toString();
        } catch (IOException e) {
            LOGGER.error(e);
            return COMPACT_EOL;
        }
    }

    private static LogEvent convertMutableToLog4jEvent(LogEvent event) {
        return event instanceof Log4jLogEvent ? event : Log4jLogEvent.createMemento(event);
    }

    protected Object wrapLogEvent(LogEvent event) {
        if (this.additionalFields.length > 0) {
            Map<String, String> additionalFieldsMap = this.resolveAdditionalFields(event);
            return new LogEventWithAdditionalFields(event, additionalFieldsMap);
        }
        if (event instanceof Message) {
            return new ReadOnlyLogEventWrapper(event);
        }
        return event;
    }

    private Map<String, String> resolveAdditionalFields(LogEvent logEvent) {
        LinkedHashMap<String, String> additionalFieldsMap = new LinkedHashMap<String, String>(this.additionalFields.length);
        StrSubstitutor strSubstitutor = this.configuration.getStrSubstitutor();
        for (ResolvableKeyValuePair pair : this.additionalFields) {
            if (pair.valueNeedsLookup) {
                additionalFieldsMap.put(pair.key, strSubstitutor.replace(logEvent, pair.value));
                continue;
            }
            additionalFieldsMap.put(pair.key, pair.value);
        }
        return additionalFieldsMap;
    }

    public void toSerializable(LogEvent event, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
        this.objectWriter.writeValue(writer, this.wrapLogEvent(AbstractJacksonLayout.convertMutableToLog4jEvent(event)));
        writer.write(this.eol);
        if (this.includeNullDelimiter) {
            writer.write(0);
        }
        this.markEvent();
    }

    private static class ReadOnlyLogEventWrapper
    implements LogEvent {
        @JsonIgnore
        private final LogEvent event;

        public ReadOnlyLogEventWrapper(LogEvent event) {
            this.event = event;
        }

        @Override
        public LogEvent toImmutable() {
            return this.event.toImmutable();
        }

        @Override
        public Map<String, String> getContextMap() {
            return this.event.getContextMap();
        }

        @Override
        public ReadOnlyStringMap getContextData() {
            return this.event.getContextData();
        }

        @Override
        public ThreadContext.ContextStack getContextStack() {
            return this.event.getContextStack();
        }

        @Override
        public String getLoggerFqcn() {
            return this.event.getLoggerFqcn();
        }

        @Override
        public Level getLevel() {
            return this.event.getLevel();
        }

        @Override
        public String getLoggerName() {
            return this.event.getLoggerName();
        }

        @Override
        public Marker getMarker() {
            return this.event.getMarker();
        }

        @Override
        public Message getMessage() {
            return this.event.getMessage();
        }

        @Override
        public long getTimeMillis() {
            return this.event.getTimeMillis();
        }

        @Override
        public Instant getInstant() {
            return this.event.getInstant();
        }

        @Override
        public StackTraceElement getSource() {
            return this.event.getSource();
        }

        @Override
        public String getThreadName() {
            return this.event.getThreadName();
        }

        @Override
        public long getThreadId() {
            return this.event.getThreadId();
        }

        @Override
        public int getThreadPriority() {
            return this.event.getThreadPriority();
        }

        @Override
        public Throwable getThrown() {
            return this.event.getThrown();
        }

        @Override
        public ThrowableProxy getThrownProxy() {
            return this.event.getThrownProxy();
        }

        @Override
        public boolean isEndOfBatch() {
            return this.event.isEndOfBatch();
        }

        @Override
        public boolean isIncludeLocation() {
            return this.event.isIncludeLocation();
        }

        @Override
        public void setEndOfBatch(boolean endOfBatch) {
        }

        @Override
        public void setIncludeLocation(boolean locationRequired) {
        }

        @Override
        public long getNanoTime() {
            return this.event.getNanoTime();
        }
    }

    protected static class ResolvableKeyValuePair {
        static final ResolvableKeyValuePair[] EMPTY_ARRAY = new ResolvableKeyValuePair[0];
        final String key;
        final String value;
        final boolean valueNeedsLookup;

        ResolvableKeyValuePair(KeyValuePair pair) {
            this.key = pair.getKey();
            this.value = pair.getValue();
            this.valueNeedsLookup = AbstractJacksonLayout.valueNeedsLookup(this.value);
        }
    }

    @JsonRootName(value="Event")
    @JacksonXmlRootElement(namespace="http://logging.apache.org/log4j/2.0/events", localName="Event")
    public static class LogEventWithAdditionalFields {
        private final Object logEvent;
        private final Map<String, String> additionalFields;

        public LogEventWithAdditionalFields(Object logEvent, Map<String, String> additionalFields) {
            this.logEvent = logEvent;
            this.additionalFields = additionalFields;
        }

        @JsonUnwrapped
        public Object getLogEvent() {
            return this.logEvent;
        }

        @JsonAnyGetter
        public Map<String, String> getAdditionalFields() {
            return this.additionalFields;
        }
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractStringLayout.Builder<B> {
        @PluginBuilderAttribute
        private boolean eventEol;
        @PluginBuilderAttribute
        private String endOfLine;
        @PluginBuilderAttribute
        private boolean compact;
        @PluginBuilderAttribute
        private boolean complete;
        @PluginBuilderAttribute
        private boolean locationInfo;
        @PluginBuilderAttribute
        private boolean properties;
        @PluginBuilderAttribute
        private boolean includeStacktrace = true;
        @PluginBuilderAttribute
        private boolean stacktraceAsString = false;
        @PluginBuilderAttribute
        private boolean includeNullDelimiter = false;
        @PluginBuilderAttribute
        private boolean includeTimeMillis = false;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;

        protected String toStringOrNull(byte[] header) {
            return header == null ? null : new String(header, Charset.defaultCharset());
        }

        public boolean getEventEol() {
            return this.eventEol;
        }

        public String getEndOfLine() {
            return this.endOfLine;
        }

        public boolean isCompact() {
            return this.compact;
        }

        public boolean isComplete() {
            return this.complete;
        }

        public boolean isLocationInfo() {
            return this.locationInfo;
        }

        public boolean isProperties() {
            return this.properties;
        }

        public boolean isIncludeStacktrace() {
            return this.includeStacktrace;
        }

        public boolean isStacktraceAsString() {
            return this.stacktraceAsString;
        }

        public boolean isIncludeNullDelimiter() {
            return this.includeNullDelimiter;
        }

        public boolean isIncludeTimeMillis() {
            return this.includeTimeMillis;
        }

        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        public B setEventEol(boolean eventEol) {
            this.eventEol = eventEol;
            return (B)((Builder)this.asBuilder());
        }

        public B setEndOfLine(String endOfLine) {
            this.endOfLine = endOfLine;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompact(boolean compact) {
            this.compact = compact;
            return (B)((Builder)this.asBuilder());
        }

        public B setComplete(boolean complete) {
            this.complete = complete;
            return (B)((Builder)this.asBuilder());
        }

        public B setLocationInfo(boolean locationInfo) {
            this.locationInfo = locationInfo;
            return (B)((Builder)this.asBuilder());
        }

        public B setProperties(boolean properties) {
            this.properties = properties;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeStacktrace(boolean includeStacktrace) {
            this.includeStacktrace = includeStacktrace;
            return (B)((Builder)this.asBuilder());
        }

        public B setStacktraceAsString(boolean stacktraceAsString) {
            this.stacktraceAsString = stacktraceAsString;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeNullDelimiter(boolean includeNullDelimiter) {
            this.includeNullDelimiter = includeNullDelimiter;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeTimeMillis(boolean includeTimeMillis) {
            this.includeTimeMillis = includeTimeMillis;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdditionalFields(KeyValuePair[] additionalFields) {
            this.additionalFields = additionalFields;
            return (B)((Builder)this.asBuilder());
        }
    }
}

