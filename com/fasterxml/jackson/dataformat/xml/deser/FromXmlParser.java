/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.StreamReadCapability
 *  com.fasterxml.jackson.core.util.JacksonFeature
 *  com.fasterxml.jackson.core.util.JacksonFeatureSet
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.JacksonFeature;
import com.fasterxml.jackson.core.util.JacksonFeatureSet;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.deser.XmlReadContext;
import com.fasterxml.jackson.dataformat.xml.deser.XmlTokenStream;
import com.fasterxml.jackson.dataformat.xml.util.CaseInsensitiveNameSet;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class FromXmlParser
extends ParserMinimalBase {
    public static final String DEFAULT_UNNAMED_TEXT_PROPERTY = "";
    protected static final JacksonFeatureSet<StreamReadCapability> XML_READ_CAPABILITIES = DEFAULT_READ_CAPABILITIES.with((JacksonFeature)StreamReadCapability.DUPLICATE_PROPERTIES).with((JacksonFeature)StreamReadCapability.SCALARS_AS_OBJECTS).with((JacksonFeature)StreamReadCapability.UNTYPED_SCALARS);
    protected String _cfgNameForTextElement = "";
    protected int _formatFeatures;
    protected ObjectCodec _objectCodec;
    protected boolean _closed;
    protected final IOContext _ioContext;
    protected XmlReadContext _parsingContext;
    protected final XmlTokenStream _xmlTokens;
    protected boolean _mayBeLeaf;
    protected JsonToken _nextToken;
    protected String _currText;
    protected boolean _nextIsLeadingMixed;
    protected ByteArrayBuilder _byteArrayBuilder = null;
    protected byte[] _binaryValue;
    protected int _numTypesValid = 0;
    protected int _numberInt;
    protected long _numberLong;
    protected BigInteger _numberBigInt;

    public FromXmlParser(IOContext ctxt, int genericParserFeatures, int xmlFeatures, ObjectCodec codec, XMLStreamReader xmlReader) throws IOException {
        super(genericParserFeatures);
        int firstToken;
        this._formatFeatures = xmlFeatures;
        this._ioContext = ctxt;
        this._objectCodec = codec;
        this._parsingContext = XmlReadContext.createRootContext(-1, -1);
        this._xmlTokens = new XmlTokenStream(xmlReader, ctxt.contentReference(), this._formatFeatures);
        try {
            firstToken = this._xmlTokens.initialize();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsParseException(e, this);
            return;
        }
        if (this._xmlTokens.hasXsiNil()) {
            this._nextToken = JsonToken.VALUE_NULL;
        } else {
            switch (firstToken) {
                case 1: 
                case 6: {
                    this._nextToken = JsonToken.START_OBJECT;
                    break;
                }
                case 7: {
                    this._currText = this._xmlTokens.getText();
                    if (this._currText == null) {
                        this._nextToken = JsonToken.VALUE_NULL;
                        break;
                    }
                    this._nextToken = JsonToken.VALUE_STRING;
                    break;
                }
                default: {
                    this._reportError("Internal problem: invalid starting state (%s)", this._xmlTokens._currentStateDesc());
                }
            }
        }
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        this._objectCodec = c;
    }

    public void setXMLTextElementName(String name) {
        this._cfgNameForTextElement = name;
    }

    @Override
    public boolean requiresCustomCodec() {
        return true;
    }

    @Override
    public boolean canReadObjectId() {
        return false;
    }

    @Override
    public boolean canReadTypeId() {
        return false;
    }

    public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() {
        return XML_READ_CAPABILITIES;
    }

    public FromXmlParser enable(Feature f) {
        this._formatFeatures |= f.getMask();
        this._xmlTokens.setFormatFeatures(this._formatFeatures);
        return this;
    }

    public FromXmlParser disable(Feature f) {
        this._formatFeatures &= ~f.getMask();
        this._xmlTokens.setFormatFeatures(this._formatFeatures);
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (this._formatFeatures & f.getMask()) != 0;
    }

    public FromXmlParser configure(Feature f, boolean state) {
        if (state) {
            this.enable(f);
        } else {
            this.disable(f);
        }
        return this;
    }

    @Override
    public int getFormatFeatures() {
        return this._formatFeatures;
    }

    @Override
    public JsonParser overrideFormatFeatures(int values, int mask) {
        this._formatFeatures = this._formatFeatures & ~mask | values & mask;
        this._xmlTokens.setFormatFeatures(this._formatFeatures);
        return this;
    }

    public XMLStreamReader getStaxReader() {
        return this._xmlTokens.getXmlReader();
    }

    public void addVirtualWrapping(Set<String> namesToWrap0, boolean caseInsensitive) {
        String name;
        CaseInsensitiveNameSet namesToWrap;
        CaseInsensitiveNameSet caseInsensitiveNameSet = namesToWrap = caseInsensitive ? CaseInsensitiveNameSet.construct(namesToWrap0) : namesToWrap0;
        if (!this._parsingContext.inRoot() && !this._parsingContext.getParent().inRoot() && (name = this._xmlTokens.getLocalName()) != null && namesToWrap.contains(name)) {
            this._xmlTokens.repeatStartElement();
        }
        this._parsingContext.setNamesToWrap(namesToWrap);
    }

    @Deprecated
    public void addVirtualWrapping(Set<String> namesToWrap) {
        this.addVirtualWrapping(namesToWrap, false);
    }

    @Override
    public String getCurrentName() throws IOException {
        String name;
        if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
            XmlReadContext parent = this._parsingContext.getParent();
            name = parent.getCurrentName();
        } else {
            name = this._parsingContext.getCurrentName();
        }
        if (name == null) {
            throw new IllegalStateException("Missing name, in state: " + (Object)((Object)this._currToken));
        }
        return name;
    }

    @Override
    public void overrideCurrentName(String name) {
        XmlReadContext ctxt = this._parsingContext;
        if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
            ctxt = ctxt.getParent();
        }
        ctxt.setCurrentName(name);
    }

    @Override
    public void close() throws IOException {
        if (!this._closed) {
            this._closed = true;
            try {
                if (this._ioContext.isResourceManaged() || this.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE)) {
                    this._xmlTokens.closeCompletely();
                } else {
                    this._xmlTokens.close();
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsParseException(e, this);
            } finally {
                this._releaseBuffers();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return this._closed;
    }

    @Override
    public XmlReadContext getParsingContext() {
        return this._parsingContext;
    }

    @Override
    public JsonLocation getTokenLocation() {
        return this._xmlTokens.getTokenLocation();
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return this._xmlTokens.getCurrentLocation();
    }

    @Override
    public boolean isExpectedStartArrayToken() {
        JsonToken t = this._currToken;
        if (t == JsonToken.START_OBJECT) {
            this._currToken = JsonToken.START_ARRAY;
            this._parsingContext.convertToArray();
            this._nextToken = this._nextToken == JsonToken.END_OBJECT ? JsonToken.END_ARRAY : null;
            this._xmlTokens.skipAttributes();
            return true;
        }
        return t == JsonToken.START_ARRAY;
    }

    public boolean isExpectedNumberIntToken() {
        String text;
        int len;
        JsonToken t = this._currToken;
        if (t == JsonToken.VALUE_STRING && (len = this._isIntNumber(text = this._currText.trim())) > 0) {
            boolean stillLong;
            if (len <= 9) {
                this._numberInt = NumberInput.parseInt(text);
                this._numTypesValid = 1;
                this._currToken = JsonToken.VALUE_NUMBER_INT;
                return true;
            }
            if (len <= 18) {
                int asInt;
                long l2;
                long l = NumberInput.parseLong(text);
                if (len == 10 && l == (l2 = (long)(asInt = (int)l))) {
                    this._numberInt = asInt;
                    this._numTypesValid = 1;
                    this._currToken = JsonToken.VALUE_NUMBER_INT;
                    return true;
                }
                this._numberLong = l;
                this._numTypesValid = 2;
                this._currToken = JsonToken.VALUE_NUMBER_INT;
                return true;
            }
            if (len == 19 && (stillLong = text.charAt(0) == '-' ? NumberInput.inLongRange(text.substring(1), true) : NumberInput.inLongRange(text, false))) {
                this._numberLong = NumberInput.parseLong(text);
                this._numTypesValid = 2;
                this._currToken = JsonToken.VALUE_NUMBER_INT;
                return true;
            }
            this._numberBigInt = new BigInteger(text);
            this._numTypesValid = 4;
            this._currToken = JsonToken.VALUE_NUMBER_INT;
            return true;
        }
        return t == JsonToken.VALUE_NUMBER_INT;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        this._binaryValue = null;
        this._numTypesValid = 0;
        if (this._nextToken != null) {
            JsonToken t;
            this._currToken = t = this._nextToken;
            this._nextToken = null;
            switch (t) {
                case START_OBJECT: {
                    this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                    break;
                }
                case START_ARRAY: {
                    this._parsingContext = this._parsingContext.createChildArrayContext(-1, -1);
                    break;
                }
                case END_OBJECT: 
                case END_ARRAY: {
                    this._parsingContext = this._parsingContext.getParent();
                    break;
                }
                case FIELD_NAME: {
                    if (this._nextIsLeadingMixed) {
                        this._nextIsLeadingMixed = false;
                        this._parsingContext.setCurrentName(this._cfgNameForTextElement);
                        this._nextToken = JsonToken.VALUE_STRING;
                        break;
                    }
                    this._parsingContext.setCurrentName(this._xmlTokens.getLocalName());
                    break;
                }
                default: {
                    this._parsingContext.valueStarted();
                }
            }
            return t;
        }
        int token = this._nextToken();
        while (token == 1) {
            if (this._mayBeLeaf) {
                this._nextToken = JsonToken.FIELD_NAME;
                this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                this._currToken = JsonToken.START_OBJECT;
                return this._currToken;
            }
            if (this._parsingContext.inArray()) {
                token = this._nextToken();
                this._mayBeLeaf = true;
                continue;
            }
            String name = this._xmlTokens.getLocalName();
            this._parsingContext.setCurrentName(name);
            if (this._parsingContext.shouldWrap(name)) {
                this._xmlTokens.repeatStartElement();
            }
            this._mayBeLeaf = true;
            this._currToken = JsonToken.FIELD_NAME;
            return this._currToken;
        }
        block14: while (true) {
            switch (token) {
                case 2: {
                    if (this._mayBeLeaf) {
                        this._mayBeLeaf = false;
                        if (this._parsingContext.inArray()) {
                            this._nextToken = JsonToken.END_OBJECT;
                            this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                            this._currToken = JsonToken.START_OBJECT;
                            return this._currToken;
                        }
                        if (this._currToken != JsonToken.VALUE_NULL) {
                            this._parsingContext.valueStarted();
                            this._currToken = JsonToken.VALUE_NULL;
                            return this._currToken;
                        }
                    }
                    this._currToken = this._parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
                    this._parsingContext = this._parsingContext.getParent();
                    return this._currToken;
                }
                case 3: {
                    if (this._mayBeLeaf) {
                        this._mayBeLeaf = false;
                        this._nextToken = JsonToken.FIELD_NAME;
                        this._currText = this._xmlTokens.getText();
                        this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                        this._currToken = JsonToken.START_OBJECT;
                        return this._currToken;
                    }
                    this._parsingContext.setCurrentName(this._xmlTokens.getLocalName());
                    this._currToken = JsonToken.FIELD_NAME;
                    return this._currToken;
                }
                case 4: {
                    this._currText = this._xmlTokens.getText();
                    this._parsingContext.valueStarted();
                    this._currToken = JsonToken.VALUE_STRING;
                    return this._currToken;
                }
                case 5: {
                    this._currText = this._xmlTokens.getText();
                    if (this._mayBeLeaf) {
                        this._mayBeLeaf = false;
                        token = this._nextToken();
                        if (token == 2) {
                            if (this._parsingContext.inArray() && XmlTokenStream._allWs(this._currText)) {
                                this._nextToken = JsonToken.END_OBJECT;
                                this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                                this._currToken = JsonToken.START_OBJECT;
                                return this._currToken;
                            }
                            this._currToken = JsonToken.VALUE_STRING;
                            return this._currToken;
                        }
                        if (token != 1) {
                            throw new JsonParseException(this, String.format("Internal error: Expected END_ELEMENT (%d) or START_ELEMENT (%d), got event of type %d", 2, 1, token));
                        }
                        this._xmlTokens.pushbackCurrentToken();
                        this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                    }
                    if (this._parsingContext.inObject()) {
                        if (this._currToken == JsonToken.FIELD_NAME) {
                            this._nextIsLeadingMixed = true;
                            this._nextToken = JsonToken.FIELD_NAME;
                            this._currToken = JsonToken.START_OBJECT;
                            return this._currToken;
                        }
                        if (XmlTokenStream._allWs(this._currText)) {
                            token = this._nextToken();
                            continue block14;
                        }
                    } else if (this._parsingContext.inArray()) {
                        if (XmlTokenStream._allWs(this._currText)) {
                            token = this._nextToken();
                            continue block14;
                        }
                        throw this._constructError("Unexpected non-whitespace text ('" + this._currText + "' in Array context: should not occur (or should be handled)");
                    }
                    this._parsingContext.setCurrentName(this._cfgNameForTextElement);
                    this._nextToken = JsonToken.VALUE_STRING;
                    this._currToken = JsonToken.FIELD_NAME;
                    return this._currToken;
                }
                case 8: {
                    this._currToken = null;
                    return null;
                }
            }
            break;
        }
        return (JsonToken)((Object)this._internalErrorUnknownToken(token));
    }

    @Override
    public String nextTextValue() throws IOException {
        this._binaryValue = null;
        if (this._nextToken != null) {
            JsonToken t;
            this._currToken = t = this._nextToken;
            this._nextToken = null;
            if (t == JsonToken.VALUE_STRING) {
                this._parsingContext.valueStarted();
                return this._currText;
            }
            this._updateState(t);
            return null;
        }
        int token = this._nextToken();
        while (token == 1) {
            if (this._mayBeLeaf) {
                this._nextToken = JsonToken.FIELD_NAME;
                this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                this._currToken = JsonToken.START_OBJECT;
                return null;
            }
            if (this._parsingContext.inArray()) {
                token = this._nextToken();
                this._mayBeLeaf = true;
                continue;
            }
            String name = this._xmlTokens.getLocalName();
            this._parsingContext.setCurrentName(name);
            if (this._parsingContext.shouldWrap(name)) {
                this._xmlTokens.repeatStartElement();
            }
            this._mayBeLeaf = true;
            this._currToken = JsonToken.FIELD_NAME;
            return null;
        }
        switch (token) {
            case 2: {
                if (this._mayBeLeaf) {
                    this._mayBeLeaf = false;
                    this._currToken = JsonToken.VALUE_STRING;
                    this._parsingContext.valueStarted();
                    this._currText = DEFAULT_UNNAMED_TEXT_PROPERTY;
                    return DEFAULT_UNNAMED_TEXT_PROPERTY;
                }
                this._currToken = this._parsingContext.inArray() ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
                this._parsingContext = this._parsingContext.getParent();
                break;
            }
            case 3: {
                if (this._mayBeLeaf) {
                    this._mayBeLeaf = false;
                    this._nextToken = JsonToken.FIELD_NAME;
                    this._currText = this._xmlTokens.getText();
                    this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                    this._currToken = JsonToken.START_OBJECT;
                    break;
                }
                this._parsingContext.setCurrentName(this._xmlTokens.getLocalName());
                this._currToken = JsonToken.FIELD_NAME;
                break;
            }
            case 4: {
                this._currToken = JsonToken.VALUE_STRING;
                this._parsingContext.valueStarted();
                this._currText = this._xmlTokens.getText();
                return this._currText;
            }
            case 5: {
                this._currText = this._xmlTokens.getText();
                if (this._mayBeLeaf) {
                    this._mayBeLeaf = false;
                    this._skipEndElement();
                    this._parsingContext.valueStarted();
                    this._currToken = JsonToken.VALUE_STRING;
                    return this._currText;
                }
                this._parsingContext.setCurrentName(this._cfgNameForTextElement);
                this._nextToken = JsonToken.VALUE_STRING;
                this._currToken = JsonToken.FIELD_NAME;
                break;
            }
            case 8: {
                this._currToken = null;
            }
            default: {
                return (String)this._internalErrorUnknownToken(token);
            }
        }
        return null;
    }

    private void _updateState(JsonToken t) {
        switch (t) {
            case START_OBJECT: {
                this._parsingContext = this._parsingContext.createChildObjectContext(-1, -1);
                break;
            }
            case START_ARRAY: {
                this._parsingContext = this._parsingContext.createChildArrayContext(-1, -1);
                break;
            }
            case END_OBJECT: 
            case END_ARRAY: {
                this._parsingContext = this._parsingContext.getParent();
                break;
            }
            case FIELD_NAME: {
                this._parsingContext.setCurrentName(this._xmlTokens.getLocalName());
                break;
            }
            default: {
                this._internalErrorUnknownToken((Object)t);
            }
        }
    }

    @Override
    public String getText() throws IOException {
        if (this._currToken == null) {
            return null;
        }
        switch (this._currToken) {
            case FIELD_NAME: {
                return this.getCurrentName();
            }
            case VALUE_STRING: {
                return this._currText;
            }
        }
        return this._currToken.asString();
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        String text = this.getText();
        return text == null ? null : text.toCharArray();
    }

    @Override
    public int getTextLength() throws IOException {
        String text = this.getText();
        return text == null ? 0 : text.length();
    }

    @Override
    public int getTextOffset() throws IOException {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        return false;
    }

    @Override
    public int getText(Writer writer) throws IOException {
        String str = this.getText();
        if (str == null) {
            return 0;
        }
        writer.write(str);
        return str.length();
    }

    @Override
    public Object getEmbeddedObject() throws IOException {
        return null;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
            this._reportError("Current token (" + (Object)((Object)this._currToken) + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        if (this._binaryValue == null) {
            try {
                this._binaryValue = this._decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw this._constructError("Failed to decode VALUE_STRING as base64 (" + b64variant + "): " + iae.getMessage());
            }
        }
        return this._binaryValue;
    }

    protected byte[] _decodeBase64(Base64Variant b64variant) throws IOException {
        ByteArrayBuilder builder = this._getByteArrayBuilder();
        String str = this.getText();
        this._decodeBase64(str, builder, b64variant);
        return builder.toByteArray();
    }

    @Override
    public boolean isNaN() {
        return false;
    }

    @Override
    public JsonParser.NumberType getNumberType() throws IOException {
        if (this._numTypesValid == 0) {
            this._checkNumericValue(0);
        }
        if ((this._numTypesValid & 1) != 0) {
            return JsonParser.NumberType.INT;
        }
        if ((this._numTypesValid & 2) != 0) {
            return JsonParser.NumberType.LONG;
        }
        return JsonParser.NumberType.BIG_INTEGER;
    }

    @Override
    public Number getNumberValue() throws IOException {
        if (this._numTypesValid == 0) {
            this._checkNumericValue(0);
        }
        if ((this._numTypesValid & 1) != 0) {
            return this._numberInt;
        }
        if ((this._numTypesValid & 2) != 0) {
            return this._numberLong;
        }
        if ((this._numTypesValid & 4) != 0) {
            return this._numberBigInt;
        }
        this._throwInternal();
        return null;
    }

    @Override
    public int getIntValue() throws IOException {
        if ((this._numTypesValid & 1) == 0) {
            if (this._numTypesValid == 0) {
                this._checkNumericValue(1);
            }
            if ((this._numTypesValid & 1) == 0) {
                this._convertNumberToInt();
            }
        }
        return this._numberInt;
    }

    @Override
    public long getLongValue() throws IOException {
        if ((this._numTypesValid & 2) == 0) {
            if (this._numTypesValid == 0) {
                this._checkNumericValue(2);
            }
            if ((this._numTypesValid & 2) == 0) {
                this._convertNumberToLong();
            }
        }
        return this._numberLong;
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        if ((this._numTypesValid & 4) == 0) {
            if (this._numTypesValid == 0) {
                this._checkNumericValue(4);
            }
            if ((this._numTypesValid & 4) == 0) {
                this._convertNumberToBigInteger();
            }
        }
        return this._numberBigInt;
    }

    @Override
    public float getFloatValue() throws IOException {
        if ((this._numTypesValid & 0x20) == 0 && this._numTypesValid == 0) {
            this._checkNumericValue(32);
        }
        return this._convertNumberToFloat();
    }

    @Override
    public double getDoubleValue() throws IOException {
        if ((this._numTypesValid & 8) == 0 && this._numTypesValid == 0) {
            this._checkNumericValue(8);
        }
        return this._convertNumberToDouble();
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        if ((this._numTypesValid & 0x10) == 0 && this._numTypesValid == 0) {
            this._checkNumericValue(16);
        }
        return this._convertNumberToBigDecimal();
    }

    protected final void _checkNumericValue(int expType) throws IOException {
        if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
            return;
        }
        this._reportError("Current token (" + (Object)((Object)this.currentToken()) + ") not numeric, can not use numeric value accessors");
    }

    protected final int _isIntNumber(String text) {
        int len = text.length();
        if (len > 0) {
            int start;
            for (int i = start = (c = text.charAt(0)) == '-' ? 1 : 0; i < len; ++i) {
                char ch = text.charAt(i);
                if (ch <= '9' && ch >= '0') continue;
                return -1;
            }
            return len - start;
        }
        return 0;
    }

    protected void _convertNumberToInt() throws IOException {
        if ((this._numTypesValid & 2) != 0) {
            int result = (int)this._numberLong;
            if ((long)result != this._numberLong) {
                this._reportError("Numeric value (" + this.getText() + ") out of range of int");
            }
            this._numberInt = result;
        } else if ((this._numTypesValid & 4) != 0) {
            if (BI_MIN_INT.compareTo(this._numberBigInt) > 0 || BI_MAX_INT.compareTo(this._numberBigInt) < 0) {
                this.reportOverflowInt();
            }
            this._numberInt = this._numberBigInt.intValue();
        } else {
            this._throwInternal();
        }
        this._numTypesValid |= 1;
    }

    protected void _convertNumberToLong() throws IOException {
        if ((this._numTypesValid & 1) != 0) {
            this._numberLong = this._numberInt;
        } else if ((this._numTypesValid & 4) != 0) {
            if (BI_MIN_LONG.compareTo(this._numberBigInt) > 0 || BI_MAX_LONG.compareTo(this._numberBigInt) < 0) {
                this.reportOverflowLong();
            }
            this._numberLong = this._numberBigInt.longValue();
        } else {
            this._throwInternal();
        }
        this._numTypesValid |= 2;
    }

    protected void _convertNumberToBigInteger() throws IOException {
        if ((this._numTypesValid & 2) != 0) {
            this._numberBigInt = BigInteger.valueOf(this._numberLong);
        } else if ((this._numTypesValid & 1) != 0) {
            this._numberBigInt = BigInteger.valueOf(this._numberInt);
        } else {
            this._throwInternal();
        }
        this._numTypesValid |= 4;
    }

    protected float _convertNumberToFloat() throws IOException {
        if ((this._numTypesValid & 4) != 0) {
            return this._numberBigInt.floatValue();
        }
        if ((this._numTypesValid & 2) != 0) {
            return this._numberLong;
        }
        if ((this._numTypesValid & 1) != 0) {
            return this._numberInt;
        }
        this._throwInternal();
        return 0.0f;
    }

    protected double _convertNumberToDouble() throws IOException {
        if ((this._numTypesValid & 4) != 0) {
            return this._numberBigInt.doubleValue();
        }
        if ((this._numTypesValid & 2) != 0) {
            return this._numberLong;
        }
        if ((this._numTypesValid & 1) != 0) {
            return this._numberInt;
        }
        this._throwInternal();
        return 0.0;
    }

    protected BigDecimal _convertNumberToBigDecimal() throws IOException {
        if ((this._numTypesValid & 4) != 0) {
            return new BigDecimal(this._numberBigInt);
        }
        if ((this._numTypesValid & 2) != 0) {
            return BigDecimal.valueOf(this._numberLong);
        }
        if ((this._numTypesValid & 1) != 0) {
            return BigDecimal.valueOf(this._numberInt);
        }
        this._throwInternal();
        return null;
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        if (!this._parsingContext.inRoot()) {
            String marker = this._parsingContext.inArray() ? "Array" : "Object";
            this._reportInvalidEOF(String.format(": expected close marker for %s (start marker at %s)", marker, this._parsingContext.startLocation(this._ioContext.contentReference())), null);
        }
    }

    protected void _releaseBuffers() throws IOException {
    }

    protected ByteArrayBuilder _getByteArrayBuilder() {
        if (this._byteArrayBuilder == null) {
            this._byteArrayBuilder = new ByteArrayBuilder();
        } else {
            this._byteArrayBuilder.reset();
        }
        return this._byteArrayBuilder;
    }

    private <T> T _internalErrorUnknownToken(Object token) {
        throw new IllegalStateException("Internal error: unrecognized XmlTokenStream token: " + token);
    }

    protected int _nextToken() throws IOException {
        try {
            return this._xmlTokens.next();
        } catch (XMLStreamException e) {
            return (Integer)StaxUtil.throwAsParseException(e, this);
        } catch (IllegalStateException e) {
            throw new JsonParseException((JsonParser)this, e.getMessage(), (Throwable)e);
        }
    }

    protected void _skipEndElement() throws IOException {
        try {
            this._xmlTokens.skipEndElement();
        } catch (XMLStreamException e) {
            StaxUtil.throwAsParseException(e, this);
        } catch (Exception e) {
            throw new JsonParseException((JsonParser)this, e.getMessage(), (Throwable)e);
        }
    }

    public static enum Feature implements FormatFeature
    {
        EMPTY_ELEMENT_AS_NULL(false),
        PROCESS_XSI_NIL(true);

        final boolean _defaultState;
        final int _mask;

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
            this._mask = 1 << this.ordinal();
        }

        @Override
        public boolean enabledByDefault() {
            return this._defaultState;
        }

        @Override
        public int getMask() {
            return this._mask;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & this.getMask()) != 0;
        }
    }
}

