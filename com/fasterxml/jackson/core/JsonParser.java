/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.async.NonBlockingInputFeeder;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.RequestPayload;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

public abstract class JsonParser
implements Closeable,
Versioned {
    private static final int MIN_BYTE_I = -128;
    private static final int MAX_BYTE_I = 255;
    private static final int MIN_SHORT_I = Short.MIN_VALUE;
    private static final int MAX_SHORT_I = Short.MAX_VALUE;
    protected int _features;
    protected transient RequestPayload _requestPayload;

    protected JsonParser() {
    }

    protected JsonParser(int features) {
        this._features = features;
    }

    public abstract ObjectCodec getCodec();

    public abstract void setCodec(ObjectCodec var1);

    public Object getInputSource() {
        return null;
    }

    public Object getCurrentValue() {
        JsonStreamContext ctxt = this.getParsingContext();
        return ctxt == null ? null : ctxt.getCurrentValue();
    }

    public void setCurrentValue(Object v) {
        JsonStreamContext ctxt = this.getParsingContext();
        if (ctxt != null) {
            ctxt.setCurrentValue(v);
        }
    }

    public void setRequestPayloadOnError(RequestPayload payload) {
        this._requestPayload = payload;
    }

    public void setRequestPayloadOnError(byte[] payload, String charset) {
        this._requestPayload = payload == null ? null : new RequestPayload(payload, charset);
    }

    public void setRequestPayloadOnError(String payload) {
        this._requestPayload = payload == null ? null : new RequestPayload(payload);
    }

    public void setSchema(FormatSchema schema) {
        throw new UnsupportedOperationException("Parser of type " + this.getClass().getName() + " does not support schema of type '" + schema.getSchemaType() + "'");
    }

    public FormatSchema getSchema() {
        return null;
    }

    public boolean canUseSchema(FormatSchema schema) {
        return false;
    }

    public boolean requiresCustomCodec() {
        return false;
    }

    public boolean canParseAsync() {
        return false;
    }

    public NonBlockingInputFeeder getNonBlockingInputFeeder() {
        return null;
    }

    @Override
    public abstract Version version();

    @Override
    public abstract void close() throws IOException;

    public abstract boolean isClosed();

    public abstract JsonStreamContext getParsingContext();

    public abstract JsonLocation getTokenLocation();

    public abstract JsonLocation getCurrentLocation();

    public int releaseBuffered(OutputStream out) throws IOException {
        return -1;
    }

    public int releaseBuffered(Writer w) throws IOException {
        return -1;
    }

    public JsonParser enable(Feature f) {
        this._features |= f.getMask();
        return this;
    }

    public JsonParser disable(Feature f) {
        this._features &= ~f.getMask();
        return this;
    }

    public JsonParser configure(Feature f, boolean state) {
        if (state) {
            this.enable(f);
        } else {
            this.disable(f);
        }
        return this;
    }

    public boolean isEnabled(Feature f) {
        return f.enabledIn(this._features);
    }

    public boolean isEnabled(StreamReadFeature f) {
        return f.mappedFeature().enabledIn(this._features);
    }

    public int getFeatureMask() {
        return this._features;
    }

    @Deprecated
    public JsonParser setFeatureMask(int mask) {
        this._features = mask;
        return this;
    }

    public JsonParser overrideStdFeatures(int values, int mask) {
        int newState = this._features & ~mask | values & mask;
        return this.setFeatureMask(newState);
    }

    public int getFormatFeatures() {
        return 0;
    }

    public JsonParser overrideFormatFeatures(int values, int mask) {
        return this;
    }

    public abstract JsonToken nextToken() throws IOException;

    public abstract JsonToken nextValue() throws IOException;

    public boolean nextFieldName(SerializableString str) throws IOException {
        return this.nextToken() == JsonToken.FIELD_NAME && str.getValue().equals(this.getCurrentName());
    }

    public String nextFieldName() throws IOException {
        return this.nextToken() == JsonToken.FIELD_NAME ? this.getCurrentName() : null;
    }

    public String nextTextValue() throws IOException {
        return this.nextToken() == JsonToken.VALUE_STRING ? this.getText() : null;
    }

    public int nextIntValue(int defaultValue) throws IOException {
        return this.nextToken() == JsonToken.VALUE_NUMBER_INT ? this.getIntValue() : defaultValue;
    }

    public long nextLongValue(long defaultValue) throws IOException {
        return this.nextToken() == JsonToken.VALUE_NUMBER_INT ? this.getLongValue() : defaultValue;
    }

    public Boolean nextBooleanValue() throws IOException {
        JsonToken t = this.nextToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    public abstract JsonParser skipChildren() throws IOException;

    public void finishToken() throws IOException {
    }

    public JsonToken currentToken() {
        return this.getCurrentToken();
    }

    public int currentTokenId() {
        return this.getCurrentTokenId();
    }

    public abstract JsonToken getCurrentToken();

    public abstract int getCurrentTokenId();

    public abstract boolean hasCurrentToken();

    public abstract boolean hasTokenId(int var1);

    public abstract boolean hasToken(JsonToken var1);

    public boolean isExpectedStartArrayToken() {
        return this.currentToken() == JsonToken.START_ARRAY;
    }

    public boolean isExpectedStartObjectToken() {
        return this.currentToken() == JsonToken.START_OBJECT;
    }

    public boolean isNaN() throws IOException {
        return false;
    }

    public abstract void clearCurrentToken();

    public abstract JsonToken getLastClearedToken();

    public abstract void overrideCurrentName(String var1);

    public abstract String getCurrentName() throws IOException;

    public String currentName() throws IOException {
        return this.getCurrentName();
    }

    public abstract String getText() throws IOException;

    public int getText(Writer writer) throws IOException, UnsupportedOperationException {
        String str = this.getText();
        if (str == null) {
            return 0;
        }
        writer.write(str);
        return str.length();
    }

    public abstract char[] getTextCharacters() throws IOException;

    public abstract int getTextLength() throws IOException;

    public abstract int getTextOffset() throws IOException;

    public abstract boolean hasTextCharacters();

    public abstract Number getNumberValue() throws IOException;

    public abstract NumberType getNumberType() throws IOException;

    public byte getByteValue() throws IOException {
        int value = this.getIntValue();
        if (value < -128 || value > 255) {
            throw new InputCoercionException(this, String.format("Numeric value (%s) out of range of Java byte", this.getText()), JsonToken.VALUE_NUMBER_INT, Byte.TYPE);
        }
        return (byte)value;
    }

    public short getShortValue() throws IOException {
        int value = this.getIntValue();
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new InputCoercionException(this, String.format("Numeric value (%s) out of range of Java short", this.getText()), JsonToken.VALUE_NUMBER_INT, Short.TYPE);
        }
        return (short)value;
    }

    public abstract int getIntValue() throws IOException;

    public abstract long getLongValue() throws IOException;

    public abstract BigInteger getBigIntegerValue() throws IOException;

    public abstract float getFloatValue() throws IOException;

    public abstract double getDoubleValue() throws IOException;

    public abstract BigDecimal getDecimalValue() throws IOException;

    public boolean getBooleanValue() throws IOException {
        JsonToken t = this.currentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return false;
        }
        throw new JsonParseException(this, String.format("Current token (%s) not of boolean type", new Object[]{t})).withRequestPayload(this._requestPayload);
    }

    public Object getEmbeddedObject() throws IOException {
        return null;
    }

    public abstract byte[] getBinaryValue(Base64Variant var1) throws IOException;

    public byte[] getBinaryValue() throws IOException {
        return this.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    public int readBinaryValue(OutputStream out) throws IOException {
        return this.readBinaryValue(Base64Variants.getDefaultVariant(), out);
    }

    public int readBinaryValue(Base64Variant bv, OutputStream out) throws IOException {
        this._reportUnsupportedOperation();
        return 0;
    }

    public int getValueAsInt() throws IOException {
        return this.getValueAsInt(0);
    }

    public int getValueAsInt(int def) throws IOException {
        return def;
    }

    public long getValueAsLong() throws IOException {
        return this.getValueAsLong(0L);
    }

    public long getValueAsLong(long def) throws IOException {
        return def;
    }

    public double getValueAsDouble() throws IOException {
        return this.getValueAsDouble(0.0);
    }

    public double getValueAsDouble(double def) throws IOException {
        return def;
    }

    public boolean getValueAsBoolean() throws IOException {
        return this.getValueAsBoolean(false);
    }

    public boolean getValueAsBoolean(boolean def) throws IOException {
        return def;
    }

    public String getValueAsString() throws IOException {
        return this.getValueAsString(null);
    }

    public abstract String getValueAsString(String var1) throws IOException;

    public boolean canReadObjectId() {
        return false;
    }

    public boolean canReadTypeId() {
        return false;
    }

    public Object getObjectId() throws IOException {
        return null;
    }

    public Object getTypeId() throws IOException {
        return null;
    }

    public <T> T readValueAs(Class<T> valueType) throws IOException {
        return this._codec().readValue(this, valueType);
    }

    public <T> T readValueAs(TypeReference<?> valueTypeRef) throws IOException {
        return (T)this._codec().readValue(this, valueTypeRef);
    }

    public <T> Iterator<T> readValuesAs(Class<T> valueType) throws IOException {
        return this._codec().readValues(this, valueType);
    }

    public <T> Iterator<T> readValuesAs(TypeReference<T> valueTypeRef) throws IOException {
        return this._codec().readValues(this, valueTypeRef);
    }

    public <T extends TreeNode> T readValueAsTree() throws IOException {
        return this._codec().readTree(this);
    }

    protected ObjectCodec _codec() {
        ObjectCodec c = this.getCodec();
        if (c == null) {
            throw new IllegalStateException("No ObjectCodec defined for parser, needed for deserialization");
        }
        return c;
    }

    protected JsonParseException _constructError(String msg) {
        return new JsonParseException(this, msg).withRequestPayload(this._requestPayload);
    }

    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by parser of type " + this.getClass().getName());
    }

    public static enum Feature {
        AUTO_CLOSE_SOURCE(true),
        ALLOW_COMMENTS(false),
        ALLOW_YAML_COMMENTS(false),
        ALLOW_UNQUOTED_FIELD_NAMES(false),
        ALLOW_SINGLE_QUOTES(false),
        ALLOW_UNQUOTED_CONTROL_CHARS(false),
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),
        ALLOW_NUMERIC_LEADING_ZEROS(false),
        ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS(false),
        ALLOW_NON_NUMERIC_NUMBERS(false),
        ALLOW_MISSING_VALUES(false),
        ALLOW_TRAILING_COMMA(false),
        STRICT_DUPLICATE_DETECTION(false),
        IGNORE_UNDEFINED(false),
        INCLUDE_SOURCE_IN_LOCATION(true);

        private final boolean _defaultState;
        private final int _mask = 1 << this.ordinal();

        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : Feature.values()) {
                if (!f.enabledByDefault()) continue;
                flags |= f.getMask();
            }
            return flags;
        }

        private Feature(boolean defaultState) {
            this._defaultState = defaultState;
        }

        public boolean enabledByDefault() {
            return this._defaultState;
        }

        public boolean enabledIn(int flags) {
            return (flags & this._mask) != 0;
        }

        public int getMask() {
            return this._mask;
        }
    }

    public static enum NumberType {
        INT,
        LONG,
        BIG_INTEGER,
        FLOAT,
        DOUBLE,
        BIG_DECIMAL;

    }
}

