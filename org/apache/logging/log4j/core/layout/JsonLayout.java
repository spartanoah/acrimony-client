/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractJacksonLayout;
import org.apache.logging.log4j.core.layout.JacksonFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;

@Plugin(name="JsonLayout", category="Core", elementType="layout", printObject=true)
public final class JsonLayout
extends AbstractJacksonLayout {
    private static final String DEFAULT_FOOTER = "]";
    private static final String DEFAULT_HEADER = "[";
    static final String CONTENT_TYPE = "application/json";

    @Deprecated
    protected JsonLayout(Configuration config, boolean locationInfo, boolean properties, boolean encodeThreadContextAsList, boolean complete, boolean compact, boolean eventEol, String endOfLine, String headerPattern, String footerPattern, Charset charset, boolean includeStacktrace) {
        super(config, new JacksonFactory.JSON(encodeThreadContextAsList, includeStacktrace, false, false).newWriter(locationInfo, properties, compact), charset, compact, complete, eventEol, endOfLine, PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(headerPattern).setDefaultPattern(DEFAULT_HEADER).build(), PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(footerPattern).setDefaultPattern(DEFAULT_FOOTER).build(), false, null);
    }

    private JsonLayout(Configuration config, boolean locationInfo, boolean properties, boolean encodeThreadContextAsList, boolean complete, boolean compact, boolean eventEol, String endOfLine, String headerPattern, String footerPattern, Charset charset, boolean includeStacktrace, boolean stacktraceAsString, boolean includeNullDelimiter, boolean includeTimeMillis, KeyValuePair[] additionalFields, boolean objectMessageAsJsonObject) {
        super(config, new JacksonFactory.JSON(encodeThreadContextAsList, includeStacktrace, stacktraceAsString, objectMessageAsJsonObject).newWriter(locationInfo, properties, compact, includeTimeMillis), charset, compact, complete, eventEol, endOfLine, PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(headerPattern).setDefaultPattern(DEFAULT_HEADER).build(), PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(footerPattern).setDefaultPattern(DEFAULT_FOOTER).build(), includeNullDelimiter, additionalFields);
    }

    @Override
    public byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String str = this.serializeToString(this.getHeaderSerializer());
        if (str != null) {
            buf.append(str);
        }
        buf.append(this.eol);
        return this.getBytes(buf.toString());
    }

    @Override
    public byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(this.eol);
        String str = this.serializeToString(this.getFooterSerializer());
        if (str != null) {
            buf.append(str);
        }
        buf.append(this.eol);
        return this.getBytes(buf.toString());
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("version", "2.0");
        return result;
    }

    @Override
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    @Deprecated
    public static JsonLayout createLayout(Configuration config, boolean locationInfo, boolean properties, boolean propertiesAsList, boolean complete, boolean compact, boolean eventEol, String headerPattern, String footerPattern, Charset charset, boolean includeStacktrace) {
        boolean encodeThreadContextAsList = properties && propertiesAsList;
        return new JsonLayout(config, locationInfo, properties, encodeThreadContextAsList, complete, compact, eventEol, null, headerPattern, footerPattern, charset, includeStacktrace, false, false, false, null, false);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static JsonLayout createDefaultLayout() {
        return new JsonLayout(new DefaultConfiguration(), false, false, false, false, false, false, null, DEFAULT_HEADER, DEFAULT_FOOTER, StandardCharsets.UTF_8, true, false, false, false, null, false);
    }

    @Override
    public void toSerializable(LogEvent event, Writer writer) throws IOException {
        if (this.complete && this.eventCount > 0L) {
            writer.append(", ");
        }
        super.toSerializable(event, writer);
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractJacksonLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<JsonLayout> {
        @PluginBuilderAttribute
        private boolean propertiesAsList;
        @PluginBuilderAttribute
        private boolean objectMessageAsJsonObject;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;

        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        @Override
        public JsonLayout build() {
            boolean encodeThreadContextAsList = this.isProperties() && this.propertiesAsList;
            String headerPattern = this.toStringOrNull(this.getHeader());
            String footerPattern = this.toStringOrNull(this.getFooter());
            return new JsonLayout(this.getConfiguration(), this.isLocationInfo(), this.isProperties(), encodeThreadContextAsList, this.isComplete(), this.isCompact(), this.getEventEol(), this.getEndOfLine(), headerPattern, footerPattern, this.getCharset(), this.isIncludeStacktrace(), this.isStacktraceAsString(), this.isIncludeNullDelimiter(), this.isIncludeTimeMillis(), this.getAdditionalFields(), this.getObjectMessageAsJsonObject());
        }

        public boolean isPropertiesAsList() {
            return this.propertiesAsList;
        }

        public B setPropertiesAsList(boolean propertiesAsList) {
            this.propertiesAsList = propertiesAsList;
            return (B)((Builder)this.asBuilder());
        }

        public boolean getObjectMessageAsJsonObject() {
            return this.objectMessageAsJsonObject;
        }

        public B setObjectMessageAsJsonObject(boolean objectMessageAsJsonObject) {
            this.objectMessageAsJsonObject = objectMessageAsJsonObject;
            return (B)((Builder)this.asBuilder());
        }

        @Override
        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        @Override
        public B setAdditionalFields(KeyValuePair[] additionalFields) {
            this.additionalFields = additionalFields;
            return (B)((Builder)this.asBuilder());
        }
    }
}

