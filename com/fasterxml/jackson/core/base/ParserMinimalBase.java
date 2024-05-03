/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ParserMinimalBase
extends JsonParser {
    protected static final int INT_TAB = 9;
    protected static final int INT_LF = 10;
    protected static final int INT_CR = 13;
    protected static final int INT_SPACE = 32;
    protected static final int INT_LBRACKET = 91;
    protected static final int INT_RBRACKET = 93;
    protected static final int INT_LCURLY = 123;
    protected static final int INT_RCURLY = 125;
    protected static final int INT_QUOTE = 34;
    protected static final int INT_APOS = 39;
    protected static final int INT_BACKSLASH = 92;
    protected static final int INT_SLASH = 47;
    protected static final int INT_ASTERISK = 42;
    protected static final int INT_COLON = 58;
    protected static final int INT_COMMA = 44;
    protected static final int INT_HASH = 35;
    protected static final int INT_0 = 48;
    protected static final int INT_9 = 57;
    protected static final int INT_MINUS = 45;
    protected static final int INT_PLUS = 43;
    protected static final int INT_PERIOD = 46;
    protected static final int INT_e = 101;
    protected static final int INT_E = 69;
    protected static final char CHAR_NULL = '\u0000';
    protected static final byte[] NO_BYTES = new byte[0];
    protected static final int[] NO_INTS = new int[0];
    protected static final int NR_UNKNOWN = 0;
    protected static final int NR_INT = 1;
    protected static final int NR_LONG = 2;
    protected static final int NR_BIGINT = 4;
    protected static final int NR_DOUBLE = 8;
    protected static final int NR_BIGDECIMAL = 16;
    protected static final int NR_FLOAT = 32;
    protected static final BigInteger BI_MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    protected static final BigInteger BI_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    protected static final BigInteger BI_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    protected static final BigInteger BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    protected static final BigDecimal BD_MIN_LONG = new BigDecimal(BI_MIN_LONG);
    protected static final BigDecimal BD_MAX_LONG = new BigDecimal(BI_MAX_LONG);
    protected static final BigDecimal BD_MIN_INT = new BigDecimal(BI_MIN_INT);
    protected static final BigDecimal BD_MAX_INT = new BigDecimal(BI_MAX_INT);
    protected static final long MIN_INT_L = Integer.MIN_VALUE;
    protected static final long MAX_INT_L = Integer.MAX_VALUE;
    protected static final double MIN_LONG_D = -9.223372036854776E18;
    protected static final double MAX_LONG_D = 9.223372036854776E18;
    protected static final double MIN_INT_D = -2.147483648E9;
    protected static final double MAX_INT_D = 2.147483647E9;
    protected static final int MAX_ERROR_TOKEN_LENGTH = 256;
    protected JsonToken _currToken;
    protected JsonToken _lastClearedToken;

    protected ParserMinimalBase() {
    }

    protected ParserMinimalBase(int features) {
        super(features);
    }

    @Override
    public abstract JsonToken nextToken() throws IOException;

    @Override
    public JsonToken currentToken() {
        return this._currToken;
    }

    @Override
    public int currentTokenId() {
        JsonToken t = this._currToken;
        return t == null ? 0 : t.id();
    }

    @Override
    public JsonToken getCurrentToken() {
        return this._currToken;
    }

    @Override
    public int getCurrentTokenId() {
        JsonToken t = this._currToken;
        return t == null ? 0 : t.id();
    }

    @Override
    public boolean hasCurrentToken() {
        return this._currToken != null;
    }

    @Override
    public boolean hasTokenId(int id) {
        JsonToken t = this._currToken;
        if (t == null) {
            return 0 == id;
        }
        return t.id() == id;
    }

    @Override
    public boolean hasToken(JsonToken t) {
        return this._currToken == t;
    }

    @Override
    public boolean isExpectedStartArrayToken() {
        return this._currToken == JsonToken.START_ARRAY;
    }

    @Override
    public boolean isExpectedStartObjectToken() {
        return this._currToken == JsonToken.START_OBJECT;
    }

    @Override
    public JsonToken nextValue() throws IOException {
        JsonToken t = this.nextToken();
        if (t == JsonToken.FIELD_NAME) {
            t = this.nextToken();
        }
        return t;
    }

    @Override
    public JsonParser skipChildren() throws IOException {
        if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;
        while (true) {
            JsonToken t;
            if ((t = this.nextToken()) == null) {
                this._handleEOF();
                return this;
            }
            if (t.isStructStart()) {
                ++open;
                continue;
            }
            if (t.isStructEnd()) {
                if (--open != 0) continue;
                return this;
            }
            if (t != JsonToken.NOT_AVAILABLE) continue;
            this._reportError("Not enough content available for `skipChildren()`: non-blocking parser? (%s)", this.getClass().getName());
        }
    }

    protected abstract void _handleEOF() throws JsonParseException;

    @Override
    public abstract String getCurrentName() throws IOException;

    @Override
    public abstract void close() throws IOException;

    @Override
    public abstract boolean isClosed();

    @Override
    public abstract JsonStreamContext getParsingContext();

    @Override
    public void clearCurrentToken() {
        if (this._currToken != null) {
            this._lastClearedToken = this._currToken;
            this._currToken = null;
        }
    }

    @Override
    public JsonToken getLastClearedToken() {
        return this._lastClearedToken;
    }

    @Override
    public abstract void overrideCurrentName(String var1);

    @Override
    public abstract String getText() throws IOException;

    @Override
    public abstract char[] getTextCharacters() throws IOException;

    @Override
    public abstract boolean hasTextCharacters();

    @Override
    public abstract int getTextLength() throws IOException;

    @Override
    public abstract int getTextOffset() throws IOException;

    @Override
    public abstract byte[] getBinaryValue(Base64Variant var1) throws IOException;

    @Override
    public boolean getValueAsBoolean(boolean defaultValue) throws IOException {
        JsonToken t = this._currToken;
        if (t != null) {
            switch (t.id()) {
                case 6: {
                    String str = this.getText().trim();
                    if ("true".equals(str)) {
                        return true;
                    }
                    if ("false".equals(str)) {
                        return false;
                    }
                    if (!this._hasTextualNull(str)) break;
                    return false;
                }
                case 7: {
                    return this.getIntValue() != 0;
                }
                case 9: {
                    return true;
                }
                case 10: 
                case 11: {
                    return false;
                }
                case 12: {
                    Object value = this.getEmbeddedObject();
                    if (!(value instanceof Boolean)) break;
                    return (Boolean)value;
                }
            }
        }
        return defaultValue;
    }

    @Override
    public int getValueAsInt() throws IOException {
        JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return this.getIntValue();
        }
        return this.getValueAsInt(0);
    }

    @Override
    public int getValueAsInt(int defaultValue) throws IOException {
        JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return this.getIntValue();
        }
        if (t != null) {
            switch (t.id()) {
                case 6: {
                    String str = this.getText();
                    if (this._hasTextualNull(str)) {
                        return 0;
                    }
                    return NumberInput.parseAsInt(str, defaultValue);
                }
                case 9: {
                    return 1;
                }
                case 10: {
                    return 0;
                }
                case 11: {
                    return 0;
                }
                case 12: {
                    Object value = this.getEmbeddedObject();
                    if (!(value instanceof Number)) break;
                    return ((Number)value).intValue();
                }
            }
        }
        return defaultValue;
    }

    @Override
    public long getValueAsLong() throws IOException {
        JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return this.getLongValue();
        }
        return this.getValueAsLong(0L);
    }

    @Override
    public long getValueAsLong(long defaultValue) throws IOException {
        JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return this.getLongValue();
        }
        if (t != null) {
            switch (t.id()) {
                case 6: {
                    String str = this.getText();
                    if (this._hasTextualNull(str)) {
                        return 0L;
                    }
                    return NumberInput.parseAsLong(str, defaultValue);
                }
                case 9: {
                    return 1L;
                }
                case 10: 
                case 11: {
                    return 0L;
                }
                case 12: {
                    Object value = this.getEmbeddedObject();
                    if (!(value instanceof Number)) break;
                    return ((Number)value).longValue();
                }
            }
        }
        return defaultValue;
    }

    @Override
    public double getValueAsDouble(double defaultValue) throws IOException {
        JsonToken t = this._currToken;
        if (t != null) {
            switch (t.id()) {
                case 6: {
                    String str = this.getText();
                    if (this._hasTextualNull(str)) {
                        return 0.0;
                    }
                    return NumberInput.parseAsDouble(str, defaultValue);
                }
                case 7: 
                case 8: {
                    return this.getDoubleValue();
                }
                case 9: {
                    return 1.0;
                }
                case 10: 
                case 11: {
                    return 0.0;
                }
                case 12: {
                    Object value = this.getEmbeddedObject();
                    if (!(value instanceof Number)) break;
                    return ((Number)value).doubleValue();
                }
            }
        }
        return defaultValue;
    }

    @Override
    public String getValueAsString() throws IOException {
        return this.getValueAsString(null);
    }

    @Override
    public String getValueAsString(String defaultValue) throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            return this.getText();
        }
        if (this._currToken == JsonToken.FIELD_NAME) {
            return this.getCurrentName();
        }
        if (this._currToken == null || this._currToken == JsonToken.VALUE_NULL || !this._currToken.isScalarValue()) {
            return defaultValue;
        }
        return this.getText();
    }

    protected void _decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException {
        try {
            b64variant.decode(str, builder);
        } catch (IllegalArgumentException e) {
            this._reportError(e.getMessage());
        }
    }

    protected boolean _hasTextualNull(String value) {
        return "null".equals(value);
    }

    protected void reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
        String msg = String.format("Unexpected character (%s) in numeric value", ParserMinimalBase._getCharDesc(ch));
        if (comment != null) {
            msg = msg + ": " + comment;
        }
        this._reportError(msg);
    }

    protected void reportInvalidNumber(String msg) throws JsonParseException {
        this._reportError("Invalid numeric value: " + msg);
    }

    protected void reportOverflowInt() throws IOException {
        this.reportOverflowInt(this.getText());
    }

    protected void reportOverflowInt(String numDesc) throws IOException {
        this.reportOverflowInt(numDesc, this.currentToken());
    }

    protected void reportOverflowInt(String numDesc, JsonToken inputType) throws IOException {
        this._reportInputCoercion(String.format("Numeric value (%s) out of range of int (%d - %s)", this._longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE), inputType, Integer.TYPE);
    }

    protected void reportOverflowLong() throws IOException {
        this.reportOverflowLong(this.getText());
    }

    protected void reportOverflowLong(String numDesc) throws IOException {
        this.reportOverflowLong(numDesc, this.currentToken());
    }

    protected void reportOverflowLong(String numDesc, JsonToken inputType) throws IOException {
        this._reportInputCoercion(String.format("Numeric value (%s) out of range of long (%d - %s)", this._longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE), inputType, Long.TYPE);
    }

    protected void _reportInputCoercion(String msg, JsonToken inputType, Class<?> targetType) throws InputCoercionException {
        throw new InputCoercionException(this, msg, inputType, targetType);
    }

    protected String _longIntegerDesc(String rawNum) {
        int rawLen = rawNum.length();
        if (rawLen < 1000) {
            return rawNum;
        }
        if (rawNum.startsWith("-")) {
            --rawLen;
        }
        return String.format("[Integer with %d digits]", rawLen);
    }

    protected String _longNumberDesc(String rawNum) {
        int rawLen = rawNum.length();
        if (rawLen < 1000) {
            return rawNum;
        }
        if (rawNum.startsWith("-")) {
            --rawLen;
        }
        return String.format("[number with %d characters]", rawLen);
    }

    protected void _reportUnexpectedChar(int ch, String comment) throws JsonParseException {
        if (ch < 0) {
            this._reportInvalidEOF();
        }
        String msg = String.format("Unexpected character (%s)", ParserMinimalBase._getCharDesc(ch));
        if (comment != null) {
            msg = msg + ": " + comment;
        }
        this._reportError(msg);
    }

    protected void _reportInvalidEOF() throws JsonParseException {
        this._reportInvalidEOF(" in " + (Object)((Object)this._currToken), this._currToken);
    }

    protected void _reportInvalidEOFInValue(JsonToken type) throws JsonParseException {
        String msg = type == JsonToken.VALUE_STRING ? " in a String value" : (type == JsonToken.VALUE_NUMBER_INT || type == JsonToken.VALUE_NUMBER_FLOAT ? " in a Number value" : " in a value");
        this._reportInvalidEOF(msg, type);
    }

    protected void _reportInvalidEOF(String msg, JsonToken currToken) throws JsonParseException {
        throw new JsonEOFException((JsonParser)this, currToken, "Unexpected end-of-input" + msg);
    }

    @Deprecated
    protected void _reportInvalidEOFInValue() throws JsonParseException {
        this._reportInvalidEOF(" in a value");
    }

    @Deprecated
    protected void _reportInvalidEOF(String msg) throws JsonParseException {
        throw new JsonEOFException((JsonParser)this, null, "Unexpected end-of-input" + msg);
    }

    protected void _reportMissingRootWS(int ch) throws JsonParseException {
        this._reportUnexpectedChar(ch, "Expected space separating root-level values");
    }

    protected void _throwInvalidSpace(int i) throws JsonParseException {
        char c = (char)i;
        String msg = "Illegal character (" + ParserMinimalBase._getCharDesc(c) + "): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        this._reportError(msg);
    }

    protected static final String _getCharDesc(int ch) {
        char c = (char)ch;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code " + ch + ")";
        }
        if (ch > 255) {
            return "'" + c + "' (code " + ch + " / 0x" + Integer.toHexString(ch) + ")";
        }
        return "'" + c + "' (code " + ch + ")";
    }

    protected final void _reportError(String msg) throws JsonParseException {
        throw this._constructError(msg);
    }

    protected final void _reportError(String msg, Object arg) throws JsonParseException {
        throw this._constructError(String.format(msg, arg));
    }

    protected final void _reportError(String msg, Object arg1, Object arg2) throws JsonParseException {
        throw this._constructError(String.format(msg, arg1, arg2));
    }

    protected final void _wrapError(String msg, Throwable t) throws JsonParseException {
        throw this._constructError(msg, t);
    }

    protected final void _throwInternal() {
        VersionUtil.throwInternal();
    }

    protected final JsonParseException _constructError(String msg, Throwable t) {
        return new JsonParseException((JsonParser)this, msg, t);
    }

    @Deprecated
    protected static byte[] _asciiBytes(String str) {
        byte[] b = new byte[str.length()];
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            b[i] = (byte)str.charAt(i);
        }
        return b;
    }

    @Deprecated
    protected static String _ascii(byte[] b) {
        try {
            return new String(b, "US-ASCII");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

