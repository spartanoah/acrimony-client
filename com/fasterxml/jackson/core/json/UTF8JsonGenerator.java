/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberOutput;
import com.fasterxml.jackson.core.json.JsonGeneratorImpl;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

public class UTF8JsonGenerator
extends JsonGeneratorImpl {
    private static final byte BYTE_u = 117;
    private static final byte BYTE_0 = 48;
    private static final byte BYTE_LBRACKET = 91;
    private static final byte BYTE_RBRACKET = 93;
    private static final byte BYTE_LCURLY = 123;
    private static final byte BYTE_RCURLY = 125;
    private static final byte BYTE_BACKSLASH = 92;
    private static final byte BYTE_COMMA = 44;
    private static final byte BYTE_COLON = 58;
    private static final int MAX_BYTES_TO_BUFFER = 512;
    private static final byte[] HEX_CHARS = CharTypes.copyHexBytes();
    private static final byte[] NULL_BYTES = new byte[]{110, 117, 108, 108};
    private static final byte[] TRUE_BYTES = new byte[]{116, 114, 117, 101};
    private static final byte[] FALSE_BYTES = new byte[]{102, 97, 108, 115, 101};
    protected final OutputStream _outputStream;
    protected byte _quoteChar;
    protected byte[] _outputBuffer;
    protected int _outputTail;
    protected final int _outputEnd;
    protected final int _outputMaxContiguous;
    protected char[] _charBuffer;
    protected final int _charBufferLength;
    protected byte[] _entityBuffer;
    protected boolean _bufferRecyclable;

    public UTF8JsonGenerator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out, char quoteChar) {
        super(ctxt, features, codec);
        this._outputStream = out;
        this._quoteChar = (byte)quoteChar;
        if (quoteChar != '\"') {
            this._outputEscapes = CharTypes.get7BitOutputEscapes(quoteChar);
        }
        this._bufferRecyclable = true;
        this._outputBuffer = ctxt.allocWriteEncodingBuffer();
        this._outputEnd = this._outputBuffer.length;
        this._outputMaxContiguous = this._outputEnd >> 3;
        this._charBuffer = ctxt.allocConcatBuffer();
        this._charBufferLength = this._charBuffer.length;
        if (this.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII)) {
            this.setHighestNonEscapedChar(127);
        }
    }

    public UTF8JsonGenerator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out, char quoteChar, byte[] outputBuffer, int outputOffset, boolean bufferRecyclable) {
        super(ctxt, features, codec);
        this._outputStream = out;
        this._quoteChar = (byte)quoteChar;
        if (quoteChar != '\"') {
            this._outputEscapes = CharTypes.get7BitOutputEscapes(quoteChar);
        }
        this._bufferRecyclable = bufferRecyclable;
        this._outputTail = outputOffset;
        this._outputBuffer = outputBuffer;
        this._outputEnd = this._outputBuffer.length;
        this._outputMaxContiguous = this._outputEnd >> 3;
        this._charBuffer = ctxt.allocConcatBuffer();
        this._charBufferLength = this._charBuffer.length;
    }

    @Deprecated
    public UTF8JsonGenerator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out) {
        this(ctxt, features, codec, out, '\"');
    }

    @Deprecated
    public UTF8JsonGenerator(IOContext ctxt, int features, ObjectCodec codec, OutputStream out, byte[] outputBuffer, int outputOffset, boolean bufferRecyclable) {
        this(ctxt, features, codec, out, '\"', outputBuffer, outputOffset, bufferRecyclable);
    }

    @Override
    public Object getOutputTarget() {
        return this._outputStream;
    }

    @Override
    public int getOutputBuffered() {
        return this._outputTail;
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            this._writePPFieldName(name);
            return;
        }
        int status = this._writeContext.writeFieldName(name);
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        if (status == 1) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 44;
        }
        if (this._cfgUnqNames) {
            this._writeStringSegments(name, false);
            return;
        }
        int len = name.length();
        if (len > this._charBufferLength) {
            this._writeStringSegments(name, true);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        if (len <= this._outputMaxContiguous) {
            if (this._outputTail + len > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(name, 0, len);
        } else {
            this._writeStringSegments(name, 0, len);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            this._writePPFieldName(name);
            return;
        }
        int status = this._writeContext.writeFieldName(name.getValue());
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        if (status == 1) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 44;
        }
        if (this._cfgUnqNames) {
            this._writeUnq(name);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        int len = name.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (len < 0) {
            this._writeBytes(name.asQuotedUTF8());
        } else {
            this._outputTail += len;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    private final void _writeUnq(SerializableString name) throws IOException {
        int len = name.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (len < 0) {
            this._writeBytes(name.asQuotedUTF8());
        } else {
            this._outputTail += len;
        }
    }

    @Override
    public final void writeStartArray() throws IOException {
        this._verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 91;
        }
    }

    @Override
    public void writeStartArray(int size) throws IOException {
        this._verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 91;
        }
    }

    @Override
    public final void writeEndArray() throws IOException {
        if (!this._writeContext.inArray()) {
            this._reportError("Current context not Array but " + this._writeContext.typeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 93;
        }
        this._writeContext = this._writeContext.clearAndGetParent();
    }

    @Override
    public final void writeStartObject() throws IOException {
        this._verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 123;
        }
    }

    @Override
    public void writeStartObject(Object forValue) throws IOException {
        JsonWriteContext ctxt;
        this._verifyValueWrite("start an object");
        this._writeContext = ctxt = this._writeContext.createChildObjectContext(forValue);
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 123;
        }
    }

    @Override
    public final void writeEndObject() throws IOException {
        if (!this._writeContext.inObject()) {
            this._reportError("Current context not Object but " + this._writeContext.typeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndObject(this, this._writeContext.getEntryCount());
        } else {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = 125;
        }
        this._writeContext = this._writeContext.clearAndGetParent();
    }

    protected final void _writePPFieldName(String name) throws IOException {
        int status = this._writeContext.writeFieldName(name);
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        if (status == 1) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        if (this._cfgUnqNames) {
            this._writeStringSegments(name, false);
            return;
        }
        int len = name.length();
        if (len > this._charBufferLength) {
            this._writeStringSegments(name, true);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        name.getChars(0, len, this._charBuffer, 0);
        if (len <= this._outputMaxContiguous) {
            if (this._outputTail + len > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(this._charBuffer, 0, len);
        } else {
            this._writeStringSegments(this._charBuffer, 0, len);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    protected final void _writePPFieldName(SerializableString name) throws IOException {
        int len;
        boolean addQuotes;
        int status = this._writeContext.writeFieldName(name.getValue());
        if (status == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        if (status == 1) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        boolean bl = addQuotes = !this._cfgUnqNames;
        if (addQuotes) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = this._quoteChar;
        }
        if ((len = name.appendQuotedUTF8(this._outputBuffer, this._outputTail)) < 0) {
            this._writeBytes(name.asQuotedUTF8());
        } else {
            this._outputTail += len;
        }
        if (addQuotes) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = this._quoteChar;
        }
    }

    @Override
    public void writeString(String text) throws IOException {
        this._verifyValueWrite("write a string");
        if (text == null) {
            this._writeNull();
            return;
        }
        int len = text.length();
        if (len > this._outputMaxContiguous) {
            this._writeStringSegments(text, true);
            return;
        }
        if (this._outputTail + len >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._writeStringSegment(text, 0, len);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeString(Reader reader, int len) throws IOException {
        int toReadNow;
        int numRead;
        this._verifyValueWrite("write a string");
        if (reader == null) {
            this._reportError("null reader");
        }
        int toRead = len >= 0 ? len : Integer.MAX_VALUE;
        char[] buf = this._charBuffer;
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        while (toRead > 0 && (numRead = reader.read(buf, 0, toReadNow = Math.min(toRead, buf.length))) > 0) {
            if (this._outputTail + len >= this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegments(buf, 0, numRead);
            toRead -= numRead;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        if (toRead > 0 && len >= 0) {
            this._reportError("Didn't read enough from reader");
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        this._verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        if (len <= this._outputMaxContiguous) {
            if (this._outputTail + len > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(text, offset, len);
        } else {
            this._writeStringSegments(text, offset, len);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public final void writeString(SerializableString text) throws IOException {
        this._verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        int len = text.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (len < 0) {
            this._writeBytes(text.asQuotedUTF8());
        } else {
            this._outputTail += len;
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        this._verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._writeBytes(text, offset, length);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int len) throws IOException {
        this._verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        if (len <= this._outputMaxContiguous) {
            this._writeUTF8Segment(text, offset, len);
        } else {
            this._writeUTF8Segments(text, offset, len);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeRaw(String text) throws IOException {
        char[] buf;
        int len = text.length();
        if (len <= (buf = this._charBuffer).length) {
            text.getChars(0, len, buf, 0);
            this.writeRaw(buf, 0, len);
        } else {
            this.writeRaw(text, 0, len);
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        char[] buf = this._charBuffer;
        int cbufLen = buf.length;
        if (len <= cbufLen) {
            text.getChars(offset, offset + len, buf, 0);
            this.writeRaw(buf, 0, len);
            return;
        }
        int maxChunk = Math.min(cbufLen, (this._outputEnd >> 2) + (this._outputEnd >> 4));
        int maxBytes = maxChunk * 3;
        while (len > 0) {
            char ch;
            int len2 = Math.min(maxChunk, len);
            text.getChars(offset, offset + len2, buf, 0);
            if (this._outputTail + maxBytes > this._outputEnd) {
                this._flushBuffer();
            }
            if (len2 > 1 && (ch = buf[len2 - 1]) >= '\ud800' && ch <= '\udbff') {
                --len2;
            }
            this._writeRawSegment(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    @Override
    public void writeRaw(SerializableString text) throws IOException {
        int len = text.appendUnquotedUTF8(this._outputBuffer, this._outputTail);
        if (len < 0) {
            this._writeBytes(text.asUnquotedUTF8());
        } else {
            this._outputTail += len;
        }
    }

    @Override
    public void writeRawValue(SerializableString text) throws IOException {
        this._verifyValueWrite("write a raw (unencoded) value");
        int len = text.appendUnquotedUTF8(this._outputBuffer, this._outputTail);
        if (len < 0) {
            this._writeBytes(text.asUnquotedUTF8());
        } else {
            this._outputTail += len;
        }
    }

    @Override
    public final void writeRaw(char[] cbuf, int offset, int len) throws IOException {
        int len3 = len + len + len;
        if (this._outputTail + len3 > this._outputEnd) {
            if (this._outputEnd < len3) {
                this._writeSegmentedRaw(cbuf, offset, len);
                return;
            }
            this._flushBuffer();
        }
        len += offset;
        block0: while (offset < len) {
            char ch;
            while ((ch = cbuf[offset]) <= '\u007f') {
                this._outputBuffer[this._outputTail++] = (byte)ch;
                if (++offset < len) continue;
                break block0;
            }
            ch = cbuf[offset++];
            if (ch < '\u0800') {
                this._outputBuffer[this._outputTail++] = (byte)(0xC0 | ch >> 6);
                this._outputBuffer[this._outputTail++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            offset = this._outputRawMultiByteChar(ch, cbuf, offset, len);
        }
    }

    @Override
    public void writeRaw(char ch) throws IOException {
        if (this._outputTail + 3 >= this._outputEnd) {
            this._flushBuffer();
        }
        byte[] bbuf = this._outputBuffer;
        if (ch <= '\u007f') {
            bbuf[this._outputTail++] = (byte)ch;
        } else if (ch < '\u0800') {
            bbuf[this._outputTail++] = (byte)(0xC0 | ch >> 6);
            bbuf[this._outputTail++] = (byte)(0x80 | ch & 0x3F);
        } else {
            this._outputRawMultiByteChar(ch, null, 0, 0);
        }
    }

    private final void _writeSegmentedRaw(char[] cbuf, int offset, int len) throws IOException {
        int end = this._outputEnd;
        byte[] bbuf = this._outputBuffer;
        int inputEnd = offset + len;
        block0: while (offset < inputEnd) {
            char ch;
            while ((ch = cbuf[offset]) < '\u0080') {
                if (this._outputTail >= end) {
                    this._flushBuffer();
                }
                bbuf[this._outputTail++] = (byte)ch;
                if (++offset < inputEnd) continue;
                break block0;
            }
            if (this._outputTail + 3 >= this._outputEnd) {
                this._flushBuffer();
            }
            if ((ch = cbuf[offset++]) < '\u0800') {
                bbuf[this._outputTail++] = (byte)(0xC0 | ch >> 6);
                bbuf[this._outputTail++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            offset = this._outputRawMultiByteChar(ch, cbuf, offset, inputEnd);
        }
    }

    private void _writeRawSegment(char[] cbuf, int offset, int end) throws IOException {
        block0: while (offset < end) {
            char ch;
            while ((ch = cbuf[offset]) <= '\u007f') {
                this._outputBuffer[this._outputTail++] = (byte)ch;
                if (++offset < end) continue;
                break block0;
            }
            ch = cbuf[offset++];
            if (ch < '\u0800') {
                this._outputBuffer[this._outputTail++] = (byte)(0xC0 | ch >> 6);
                this._outputBuffer[this._outputTail++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            offset = this._outputRawMultiByteChar(ch, cbuf, offset, end);
        }
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
        this._verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._writeBinary(b64variant, data, offset, offset + len);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException, JsonGenerationException {
        int bytes;
        this._verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        byte[] encodingBuffer = this._ioContext.allocBase64Buffer();
        try {
            if (dataLength < 0) {
                bytes = this._writeBinary(b64variant, data, encodingBuffer);
            } else {
                int missing = this._writeBinary(b64variant, data, encodingBuffer, dataLength);
                if (missing > 0) {
                    this._reportError("Too few bytes available: missing " + missing + " bytes (out of " + dataLength + ")");
                }
                bytes = dataLength;
            }
        } finally {
            this._ioContext.releaseBase64Buffer(encodingBuffer);
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        return bytes;
    }

    @Override
    public void writeNumber(short s) throws IOException {
        this._verifyValueWrite("write a number");
        if (this._outputTail + 6 >= this._outputEnd) {
            this._flushBuffer();
        }
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedShort(s);
            return;
        }
        this._outputTail = NumberOutput.outputInt((int)s, this._outputBuffer, this._outputTail);
    }

    private final void _writeQuotedShort(short s) throws IOException {
        if (this._outputTail + 8 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt((int)s, this._outputBuffer, this._outputTail);
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeNumber(int i) throws IOException {
        this._verifyValueWrite("write a number");
        if (this._outputTail + 11 >= this._outputEnd) {
            this._flushBuffer();
        }
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedInt(i);
            return;
        }
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
    }

    private final void _writeQuotedInt(int i) throws IOException {
        if (this._outputTail + 13 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeNumber(long l) throws IOException {
        this._verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedLong(l);
            return;
        }
        if (this._outputTail + 21 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
    }

    private final void _writeQuotedLong(long l) throws IOException {
        if (this._outputTail + 23 >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this._outputTail = NumberOutput.outputLong(l, this._outputBuffer, this._outputTail);
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeNumber(BigInteger value) throws IOException {
        this._verifyValueWrite("write a number");
        if (value == null) {
            this._writeNull();
        } else if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(value.toString());
        } else {
            this.writeRaw(value.toString());
        }
    }

    @Override
    public void writeNumber(double d) throws IOException {
        if (this._cfgNumbersAsStrings || NumberOutput.notFinite(d) && JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(this._features)) {
            this.writeString(String.valueOf(d));
            return;
        }
        this._verifyValueWrite("write a number");
        this.writeRaw(String.valueOf(d));
    }

    @Override
    public void writeNumber(float f) throws IOException {
        if (this._cfgNumbersAsStrings || NumberOutput.notFinite(f) && JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(this._features)) {
            this.writeString(String.valueOf(f));
            return;
        }
        this._verifyValueWrite("write a number");
        this.writeRaw(String.valueOf(f));
    }

    @Override
    public void writeNumber(BigDecimal value) throws IOException {
        this._verifyValueWrite("write a number");
        if (value == null) {
            this._writeNull();
        } else if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(this._asString(value));
        } else {
            this.writeRaw(this._asString(value));
        }
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException {
        this._verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(encodedValue);
        } else {
            this.writeRaw(encodedValue);
        }
    }

    @Override
    public void writeNumber(char[] encodedValueBuffer, int offset, int length) throws IOException {
        this._verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            this._writeQuotedRaw(encodedValueBuffer, offset, length);
        } else {
            this.writeRaw(encodedValueBuffer, offset, length);
        }
    }

    private final void _writeQuotedRaw(String value) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this.writeRaw(value);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    private void _writeQuotedRaw(char[] text, int offset, int length) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
        this.writeRaw(text, offset, length);
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = this._quoteChar;
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        this._verifyValueWrite("write a boolean value");
        if (this._outputTail + 5 >= this._outputEnd) {
            this._flushBuffer();
        }
        byte[] keyword = state ? TRUE_BYTES : FALSE_BYTES;
        int len = keyword.length;
        System.arraycopy(keyword, 0, this._outputBuffer, this._outputTail, len);
        this._outputTail += len;
    }

    @Override
    public void writeNull() throws IOException {
        this._verifyValueWrite("write a null");
        this._writeNull();
    }

    @Override
    protected final void _verifyValueWrite(String typeMsg) throws IOException {
        int b;
        int status = this._writeContext.writeValue();
        if (this._cfgPrettyPrinter != null) {
            this._verifyPrettyValueWrite(typeMsg, status);
            return;
        }
        switch (status) {
            default: {
                return;
            }
            case 1: {
                b = 44;
                break;
            }
            case 2: {
                b = 58;
                break;
            }
            case 3: {
                byte[] raw;
                if (this._rootValueSeparator != null && (raw = this._rootValueSeparator.asUnquotedUTF8()).length > 0) {
                    this._writeBytes(raw);
                }
                return;
            }
            case 5: {
                this._reportCantWriteValueExpectName(typeMsg);
                return;
            }
        }
        if (this._outputTail >= this._outputEnd) {
            this._flushBuffer();
        }
        this._outputBuffer[this._outputTail++] = b;
    }

    @Override
    public void flush() throws IOException {
        this._flushBuffer();
        if (this._outputStream != null && this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
            this._outputStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (this._outputBuffer != null && this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
            while (true) {
                JsonStreamContext ctxt;
                if ((ctxt = this.getOutputContext()).inArray()) {
                    this.writeEndArray();
                    continue;
                }
                if (!ctxt.inObject()) break;
                this.writeEndObject();
            }
        }
        this._flushBuffer();
        this._outputTail = 0;
        if (this._outputStream != null) {
            if (this._ioContext.isResourceManaged() || this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
                this._outputStream.close();
            } else if (this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
                this._outputStream.flush();
            }
        }
        this._releaseBuffers();
    }

    @Override
    protected void _releaseBuffers() {
        char[] cbuf;
        byte[] buf = this._outputBuffer;
        if (buf != null && this._bufferRecyclable) {
            this._outputBuffer = null;
            this._ioContext.releaseWriteEncodingBuffer(buf);
        }
        if ((cbuf = this._charBuffer) != null) {
            this._charBuffer = null;
            this._ioContext.releaseConcatBuffer(cbuf);
        }
    }

    private final void _writeBytes(byte[] bytes) throws IOException {
        int len = bytes.length;
        if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
            if (len > 512) {
                this._outputStream.write(bytes, 0, len);
                return;
            }
        }
        System.arraycopy(bytes, 0, this._outputBuffer, this._outputTail, len);
        this._outputTail += len;
    }

    private final void _writeBytes(byte[] bytes, int offset, int len) throws IOException {
        if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
            if (len > 512) {
                this._outputStream.write(bytes, offset, len);
                return;
            }
        }
        System.arraycopy(bytes, offset, this._outputBuffer, this._outputTail, len);
        this._outputTail += len;
    }

    private final void _writeStringSegments(String text, boolean addQuotes) throws IOException {
        int len;
        if (addQuotes) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = this._quoteChar;
        }
        int offset = 0;
        for (int left = text.length(); left > 0; left -= len) {
            len = Math.min(this._outputMaxContiguous, left);
            if (this._outputTail + len > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(text, offset, len);
            offset += len;
        }
        if (addQuotes) {
            if (this._outputTail >= this._outputEnd) {
                this._flushBuffer();
            }
            this._outputBuffer[this._outputTail++] = this._quoteChar;
        }
    }

    private final void _writeStringSegments(char[] cbuf, int offset, int totalLen) throws IOException {
        int len;
        do {
            if (this._outputTail + (len = Math.min(this._outputMaxContiguous, totalLen)) > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(cbuf, offset, len);
            offset += len;
        } while ((totalLen -= len) > 0);
    }

    private final void _writeStringSegments(String text, int offset, int totalLen) throws IOException {
        int len;
        do {
            if (this._outputTail + (len = Math.min(this._outputMaxContiguous, totalLen)) > this._outputEnd) {
                this._flushBuffer();
            }
            this._writeStringSegment(text, offset, len);
            offset += len;
        } while ((totalLen -= len) > 0);
    }

    private final void _writeStringSegment(char[] cbuf, int offset, int len) throws IOException {
        char ch;
        len += offset;
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        while (offset < len && (ch = cbuf[offset]) <= '\u007f' && escCodes[ch] == 0) {
            outputBuffer[outputPtr++] = (byte)ch;
            ++offset;
        }
        this._outputTail = outputPtr;
        if (offset < len) {
            if (this._characterEscapes != null) {
                this._writeCustomStringSegment2(cbuf, offset, len);
            } else if (this._maximumNonEscapedChar == 0) {
                this._writeStringSegment2(cbuf, offset, len);
            } else {
                this._writeStringSegmentASCII2(cbuf, offset, len);
            }
        }
    }

    private final void _writeStringSegment(String text, int offset, int len) throws IOException {
        char ch;
        len += offset;
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        while (offset < len && (ch = text.charAt(offset)) <= '\u007f' && escCodes[ch] == 0) {
            outputBuffer[outputPtr++] = (byte)ch;
            ++offset;
        }
        this._outputTail = outputPtr;
        if (offset < len) {
            if (this._characterEscapes != null) {
                this._writeCustomStringSegment2(text, offset, len);
            } else if (this._maximumNonEscapedChar == 0) {
                this._writeStringSegment2(text, offset, len);
            } else {
                this._writeStringSegmentASCII2(text, offset, len);
            }
        }
    }

    private final void _writeStringSegment2(char[] cbuf, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        while (offset < end) {
            char ch;
            if ((ch = cbuf[offset++]) <= '\u007f') {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch <= '\u07ff') {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final void _writeStringSegment2(String text, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        while (offset < end) {
            char ch;
            if ((ch = text.charAt(offset++)) <= '\u007f') {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch <= '\u07ff') {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final void _writeStringSegmentASCII2(char[] cbuf, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        int maxUnescaped = this._maximumNonEscapedChar;
        while (offset < end) {
            char ch;
            if ((ch = cbuf[offset++]) <= '\u007f') {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch > maxUnescaped) {
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch <= '\u07ff') {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final void _writeStringSegmentASCII2(String text, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        int maxUnescaped = this._maximumNonEscapedChar;
        while (offset < end) {
            char ch;
            if ((ch = text.charAt(offset++)) <= '\u007f') {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch > maxUnescaped) {
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch <= '\u07ff') {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final void _writeCustomStringSegment2(char[] cbuf, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        int maxUnescaped = this._maximumNonEscapedChar <= 0 ? 65535 : this._maximumNonEscapedChar;
        CharacterEscapes customEscapes = this._characterEscapes;
        while (offset < end) {
            int ch;
            if ((ch = cbuf[offset++]) <= 127) {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                if (escape == -2) {
                    SerializableString esc = customEscapes.getEscapeSequence(ch);
                    if (esc == null) {
                        this._reportError("Invalid custom escape definitions; custom escape not found for character code 0x" + Integer.toHexString(ch) + ", although was supposed to have one");
                    }
                    outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch > maxUnescaped) {
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            SerializableString esc = customEscapes.getEscapeSequence(ch);
            if (esc != null) {
                outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
                continue;
            }
            if (ch <= 2047) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final void _writeCustomStringSegment2(String text, int offset, int end) throws IOException {
        if (this._outputTail + 6 * (end - offset) > this._outputEnd) {
            this._flushBuffer();
        }
        int outputPtr = this._outputTail;
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        int maxUnescaped = this._maximumNonEscapedChar <= 0 ? 65535 : this._maximumNonEscapedChar;
        CharacterEscapes customEscapes = this._characterEscapes;
        while (offset < end) {
            int ch;
            if ((ch = text.charAt(offset++)) <= 127) {
                if (escCodes[ch] == 0) {
                    outputBuffer[outputPtr++] = (byte)ch;
                    continue;
                }
                int escape = escCodes[ch];
                if (escape > 0) {
                    outputBuffer[outputPtr++] = 92;
                    outputBuffer[outputPtr++] = (byte)escape;
                    continue;
                }
                if (escape == -2) {
                    SerializableString esc = customEscapes.getEscapeSequence(ch);
                    if (esc == null) {
                        this._reportError("Invalid custom escape definitions; custom escape not found for character code 0x" + Integer.toHexString(ch) + ", although was supposed to have one");
                    }
                    outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
                    continue;
                }
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch > maxUnescaped) {
                outputPtr = this._writeGenericEscape(ch, outputPtr);
                continue;
            }
            SerializableString esc = customEscapes.getEscapeSequence(ch);
            if (esc != null) {
                outputPtr = this._writeCustomEscape(outputBuffer, outputPtr, esc, end - offset);
                continue;
            }
            if (ch <= 2047) {
                outputBuffer[outputPtr++] = (byte)(0xC0 | ch >> 6);
                outputBuffer[outputPtr++] = (byte)(0x80 | ch & 0x3F);
                continue;
            }
            outputPtr = this._outputMultiByteChar(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    private final int _writeCustomEscape(byte[] outputBuffer, int outputPtr, SerializableString esc, int remainingChars) throws IOException, JsonGenerationException {
        byte[] raw = esc.asUnquotedUTF8();
        int len = raw.length;
        if (len > 6) {
            return this._handleLongCustomEscape(outputBuffer, outputPtr, this._outputEnd, raw, remainingChars);
        }
        System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
        return outputPtr + len;
    }

    private final int _handleLongCustomEscape(byte[] outputBuffer, int outputPtr, int outputEnd, byte[] raw, int remainingChars) throws IOException, JsonGenerationException {
        int len = raw.length;
        if (outputPtr + len > outputEnd) {
            this._outputTail = outputPtr;
            this._flushBuffer();
            outputPtr = this._outputTail;
            if (len > outputBuffer.length) {
                this._outputStream.write(raw, 0, len);
                return outputPtr;
            }
        }
        System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
        if ((outputPtr += len) + 6 * remainingChars > outputEnd) {
            this._outputTail = outputPtr;
            this._flushBuffer();
            return this._outputTail;
        }
        return outputPtr;
    }

    private final void _writeUTF8Segments(byte[] utf8, int offset, int totalLen) throws IOException, JsonGenerationException {
        int len;
        do {
            len = Math.min(this._outputMaxContiguous, totalLen);
            this._writeUTF8Segment(utf8, offset, len);
            offset += len;
        } while ((totalLen -= len) > 0);
    }

    private final void _writeUTF8Segment(byte[] utf8, int offset, int len) throws IOException, JsonGenerationException {
        int[] escCodes = this._outputEscapes;
        int ptr = offset;
        int end = offset + len;
        while (ptr < end) {
            byte ch;
            if ((ch = utf8[ptr++]) < 0 || escCodes[ch] == 0) continue;
            this._writeUTF8Segment2(utf8, offset, len);
            return;
        }
        if (this._outputTail + len > this._outputEnd) {
            this._flushBuffer();
        }
        System.arraycopy(utf8, offset, this._outputBuffer, this._outputTail, len);
        this._outputTail += len;
    }

    private final void _writeUTF8Segment2(byte[] utf8, int offset, int len) throws IOException, JsonGenerationException {
        int outputPtr = this._outputTail;
        if (outputPtr + len * 6 > this._outputEnd) {
            this._flushBuffer();
            outputPtr = this._outputTail;
        }
        byte[] outputBuffer = this._outputBuffer;
        int[] escCodes = this._outputEscapes;
        len += offset;
        while (offset < len) {
            byte b;
            byte ch;
            if ((ch = (b = utf8[offset++])) < 0 || escCodes[ch] == 0) {
                outputBuffer[outputPtr++] = b;
                continue;
            }
            int escape = escCodes[ch];
            if (escape > 0) {
                outputBuffer[outputPtr++] = 92;
                outputBuffer[outputPtr++] = (byte)escape;
                continue;
            }
            outputPtr = this._writeGenericEscape(ch, outputPtr);
        }
        this._outputTail = outputPtr;
    }

    protected final void _writeBinary(Base64Variant b64variant, byte[] input, int inputPtr, int inputEnd) throws IOException, JsonGenerationException {
        int safeInputEnd = inputEnd - 3;
        int safeOutputEnd = this._outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        while (inputPtr <= safeInputEnd) {
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            int b24 = input[inputPtr++] << 8;
            b24 |= input[inputPtr++] & 0xFF;
            b24 = b24 << 8 | input[inputPtr++] & 0xFF;
            this._outputTail = b64variant.encodeBase64Chunk(b24, this._outputBuffer, this._outputTail);
            if (--chunksBeforeLF > 0) continue;
            this._outputBuffer[this._outputTail++] = 92;
            this._outputBuffer[this._outputTail++] = 110;
            chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        }
        int inputLeft = inputEnd - inputPtr;
        if (inputLeft > 0) {
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            int b24 = input[inputPtr++] << 16;
            if (inputLeft == 2) {
                b24 |= (input[inputPtr++] & 0xFF) << 8;
            }
            this._outputTail = b64variant.encodeBase64Partial(b24, inputLeft, this._outputBuffer, this._outputTail);
        }
    }

    protected final int _writeBinary(Base64Variant b64variant, InputStream data, byte[] readBuffer, int bytesLeft) throws IOException, JsonGenerationException {
        int b24;
        int inputPtr = 0;
        int inputEnd = 0;
        int lastFullOffset = -3;
        int safeOutputEnd = this._outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        while (bytesLeft > 2) {
            if (inputPtr > lastFullOffset) {
                inputEnd = this._readMore(data, readBuffer, inputPtr, inputEnd, bytesLeft);
                inputPtr = 0;
                if (inputEnd < 3) break;
                lastFullOffset = inputEnd - 3;
            }
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            b24 = readBuffer[inputPtr++] << 8;
            b24 |= readBuffer[inputPtr++] & 0xFF;
            b24 = b24 << 8 | readBuffer[inputPtr++] & 0xFF;
            bytesLeft -= 3;
            this._outputTail = b64variant.encodeBase64Chunk(b24, this._outputBuffer, this._outputTail);
            if (--chunksBeforeLF > 0) continue;
            this._outputBuffer[this._outputTail++] = 92;
            this._outputBuffer[this._outputTail++] = 110;
            chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        }
        if (bytesLeft > 0) {
            inputEnd = this._readMore(data, readBuffer, inputPtr, inputEnd, bytesLeft);
            inputPtr = 0;
            if (inputEnd > 0) {
                int amount;
                if (this._outputTail > safeOutputEnd) {
                    this._flushBuffer();
                }
                b24 = readBuffer[inputPtr++] << 16;
                if (inputPtr < inputEnd) {
                    b24 |= (readBuffer[inputPtr] & 0xFF) << 8;
                    amount = 2;
                } else {
                    amount = 1;
                }
                this._outputTail = b64variant.encodeBase64Partial(b24, amount, this._outputBuffer, this._outputTail);
                bytesLeft -= amount;
            }
        }
        return bytesLeft;
    }

    protected final int _writeBinary(Base64Variant b64variant, InputStream data, byte[] readBuffer) throws IOException, JsonGenerationException {
        int b24;
        int inputPtr = 0;
        int inputEnd = 0;
        int lastFullOffset = -3;
        int bytesDone = 0;
        int safeOutputEnd = this._outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        while (true) {
            if (inputPtr > lastFullOffset) {
                inputEnd = this._readMore(data, readBuffer, inputPtr, inputEnd, readBuffer.length);
                inputPtr = 0;
                if (inputEnd < 3) break;
                lastFullOffset = inputEnd - 3;
            }
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            b24 = readBuffer[inputPtr++] << 8;
            b24 |= readBuffer[inputPtr++] & 0xFF;
            b24 = b24 << 8 | readBuffer[inputPtr++] & 0xFF;
            bytesDone += 3;
            this._outputTail = b64variant.encodeBase64Chunk(b24, this._outputBuffer, this._outputTail);
            if (--chunksBeforeLF > 0) continue;
            this._outputBuffer[this._outputTail++] = 92;
            this._outputBuffer[this._outputTail++] = 110;
            chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
        }
        if (inputPtr < inputEnd) {
            if (this._outputTail > safeOutputEnd) {
                this._flushBuffer();
            }
            b24 = readBuffer[inputPtr++] << 16;
            int amount = 1;
            if (inputPtr < inputEnd) {
                b24 |= (readBuffer[inputPtr] & 0xFF) << 8;
                amount = 2;
            }
            bytesDone += amount;
            this._outputTail = b64variant.encodeBase64Partial(b24, amount, this._outputBuffer, this._outputTail);
        }
        return bytesDone;
    }

    private final int _readMore(InputStream in, byte[] readBuffer, int inputPtr, int inputEnd, int maxRead) throws IOException {
        int length;
        int i = 0;
        while (inputPtr < inputEnd) {
            readBuffer[i++] = readBuffer[inputPtr++];
        }
        inputPtr = 0;
        inputEnd = i;
        maxRead = Math.min(maxRead, readBuffer.length);
        while ((length = maxRead - inputEnd) != 0) {
            int count = in.read(readBuffer, inputEnd, length);
            if (count < 0) {
                return inputEnd;
            }
            if ((inputEnd += count) < 3) continue;
        }
        return inputEnd;
    }

    private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputEnd) throws IOException {
        if (ch >= 55296 && ch <= 57343) {
            if (inputOffset >= inputEnd || cbuf == null) {
                this._reportError(String.format("Split surrogate on writeRaw() input (last character): first character 0x%4x", ch));
            }
            this._outputSurrogates(ch, cbuf[inputOffset]);
            return inputOffset + 1;
        }
        byte[] bbuf = this._outputBuffer;
        bbuf[this._outputTail++] = (byte)(0xE0 | ch >> 12);
        bbuf[this._outputTail++] = (byte)(0x80 | ch >> 6 & 0x3F);
        bbuf[this._outputTail++] = (byte)(0x80 | ch & 0x3F);
        return inputOffset;
    }

    protected final void _outputSurrogates(int surr1, int surr2) throws IOException {
        int c = this._decodeSurrogate(surr1, surr2);
        if (this._outputTail + 4 > this._outputEnd) {
            this._flushBuffer();
        }
        byte[] bbuf = this._outputBuffer;
        bbuf[this._outputTail++] = (byte)(0xF0 | c >> 18);
        bbuf[this._outputTail++] = (byte)(0x80 | c >> 12 & 0x3F);
        bbuf[this._outputTail++] = (byte)(0x80 | c >> 6 & 0x3F);
        bbuf[this._outputTail++] = (byte)(0x80 | c & 0x3F);
    }

    private final int _outputMultiByteChar(int ch, int outputPtr) throws IOException {
        byte[] bbuf = this._outputBuffer;
        if (ch >= 55296 && ch <= 57343) {
            bbuf[outputPtr++] = 92;
            bbuf[outputPtr++] = 117;
            bbuf[outputPtr++] = HEX_CHARS[ch >> 12 & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[ch >> 8 & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[ch >> 4 & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[ch & 0xF];
        } else {
            bbuf[outputPtr++] = (byte)(0xE0 | ch >> 12);
            bbuf[outputPtr++] = (byte)(0x80 | ch >> 6 & 0x3F);
            bbuf[outputPtr++] = (byte)(0x80 | ch & 0x3F);
        }
        return outputPtr;
    }

    private final void _writeNull() throws IOException {
        if (this._outputTail + 4 >= this._outputEnd) {
            this._flushBuffer();
        }
        System.arraycopy(NULL_BYTES, 0, this._outputBuffer, this._outputTail, 4);
        this._outputTail += 4;
    }

    private int _writeGenericEscape(int charToEscape, int outputPtr) throws IOException {
        byte[] bbuf = this._outputBuffer;
        bbuf[outputPtr++] = 92;
        bbuf[outputPtr++] = 117;
        if (charToEscape > 255) {
            int hi = charToEscape >> 8 & 0xFF;
            bbuf[outputPtr++] = HEX_CHARS[hi >> 4];
            bbuf[outputPtr++] = HEX_CHARS[hi & 0xF];
            charToEscape &= 0xFF;
        } else {
            bbuf[outputPtr++] = 48;
            bbuf[outputPtr++] = 48;
        }
        bbuf[outputPtr++] = HEX_CHARS[charToEscape >> 4];
        bbuf[outputPtr++] = HEX_CHARS[charToEscape & 0xF];
        return outputPtr;
    }

    protected final void _flushBuffer() throws IOException {
        int len = this._outputTail;
        if (len > 0) {
            this._outputTail = 0;
            this._outputStream.write(this._outputBuffer, 0, len);
        }
    }
}

