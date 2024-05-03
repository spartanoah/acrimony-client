/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SerializedString
implements SerializableString,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final JsonStringEncoder JSON_ENCODER = JsonStringEncoder.getInstance();
    protected final String _value;
    protected byte[] _quotedUTF8Ref;
    protected byte[] _unquotedUTF8Ref;
    protected char[] _quotedChars;
    protected transient String _jdkSerializeValue;

    public SerializedString(String v) {
        if (v == null) {
            throw new IllegalStateException("Null String illegal for SerializedString");
        }
        this._value = v;
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this._jdkSerializeValue = in.readUTF();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(this._value);
    }

    protected Object readResolve() {
        return new SerializedString(this._jdkSerializeValue);
    }

    @Override
    public final String getValue() {
        return this._value;
    }

    @Override
    public final int charLength() {
        return this._value.length();
    }

    @Override
    public final char[] asQuotedChars() {
        char[] result = this._quotedChars;
        if (result == null) {
            this._quotedChars = result = JSON_ENCODER.quoteAsString(this._value);
        }
        return result;
    }

    @Override
    public final byte[] asQuotedUTF8() {
        byte[] result = this._quotedUTF8Ref;
        if (result == null) {
            this._quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(this._value);
        }
        return result;
    }

    @Override
    public final byte[] asUnquotedUTF8() {
        byte[] result = this._unquotedUTF8Ref;
        if (result == null) {
            this._unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(this._value);
        }
        return result;
    }

    @Override
    public int appendQuoted(char[] buffer, int offset) {
        int length;
        char[] result = this._quotedChars;
        if (result == null) {
            this._quotedChars = result = JSON_ENCODER.quoteAsString(this._value);
        }
        if (offset + (length = result.length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int appendQuotedUTF8(byte[] buffer, int offset) {
        int length;
        byte[] result = this._quotedUTF8Ref;
        if (result == null) {
            this._quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(this._value);
        }
        if (offset + (length = result.length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int appendUnquoted(char[] buffer, int offset) {
        String str = this._value;
        int length = str.length();
        if (offset + length > buffer.length) {
            return -1;
        }
        str.getChars(0, length, buffer, offset);
        return length;
    }

    @Override
    public int appendUnquotedUTF8(byte[] buffer, int offset) {
        int length;
        byte[] result = this._unquotedUTF8Ref;
        if (result == null) {
            this._unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(this._value);
        }
        if (offset + (length = result.length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int writeQuotedUTF8(OutputStream out) throws IOException {
        byte[] result = this._quotedUTF8Ref;
        if (result == null) {
            this._quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(this._value);
        }
        int length = result.length;
        out.write(result, 0, length);
        return length;
    }

    @Override
    public int writeUnquotedUTF8(OutputStream out) throws IOException {
        byte[] result = this._unquotedUTF8Ref;
        if (result == null) {
            this._unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(this._value);
        }
        int length = result.length;
        out.write(result, 0, length);
        return length;
    }

    @Override
    public int putQuotedUTF8(ByteBuffer buffer) {
        int length;
        byte[] result = this._quotedUTF8Ref;
        if (result == null) {
            this._quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(this._value);
        }
        if ((length = result.length) > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

    @Override
    public int putUnquotedUTF8(ByteBuffer buffer) {
        int length;
        byte[] result = this._unquotedUTF8Ref;
        if (result == null) {
            this._unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(this._value);
        }
        if ((length = result.length) > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

    public final String toString() {
        return this._value;
    }

    public final int hashCode() {
        return this._value.hashCode();
    }

    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        SerializedString other = (SerializedString)o;
        return this._value.equals(other._value);
    }
}

