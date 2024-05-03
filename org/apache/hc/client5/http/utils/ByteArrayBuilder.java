/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public final class ByteArrayBuilder {
    private CharsetEncoder charsetEncoder;
    private ByteBuffer buffer;

    public ByteArrayBuilder() {
    }

    public ByteArrayBuilder(int initialCapacity) {
        this.buffer = ByteBuffer.allocate(initialCapacity);
    }

    public int capacity() {
        return this.buffer != null ? this.buffer.capacity() : 0;
    }

    static ByteBuffer ensureFreeCapacity(ByteBuffer buffer, int capacity) {
        if (buffer == null) {
            return ByteBuffer.allocate(capacity);
        }
        if (buffer.remaining() < capacity) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.position() + capacity);
            buffer.flip();
            newBuffer.put(buffer);
            return newBuffer;
        }
        return buffer;
    }

    static ByteBuffer encode(ByteBuffer buffer, CharBuffer in, CharsetEncoder encoder) throws CharacterCodingException {
        int capacity = (int)((float)in.remaining() * encoder.averageBytesPerChar());
        ByteBuffer out = ByteArrayBuilder.ensureFreeCapacity(buffer, capacity);
        while (true) {
            CoderResult result;
            CoderResult coderResult = result = in.hasRemaining() ? encoder.encode(in, out, true) : CoderResult.UNDERFLOW;
            if (result.isError()) {
                result.throwException();
            }
            if (result.isUnderflow()) {
                result = encoder.flush(out);
            }
            if (result.isUnderflow()) break;
            if (!result.isOverflow()) continue;
            out = ByteArrayBuilder.ensureFreeCapacity(out, capacity);
        }
        return out;
    }

    public void ensureFreeCapacity(int freeCapacity) {
        this.buffer = ByteArrayBuilder.ensureFreeCapacity(this.buffer, freeCapacity);
    }

    private void doAppend(CharBuffer charBuffer) {
        if (this.charsetEncoder == null) {
            this.charsetEncoder = StandardCharsets.US_ASCII.newEncoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        this.charsetEncoder.reset();
        try {
            this.buffer = ByteArrayBuilder.encode(this.buffer, charBuffer, this.charsetEncoder);
        } catch (CharacterCodingException ex) {
            throw new IllegalStateException("Unexpected character coding error", ex);
        }
    }

    public ByteArrayBuilder charset(Charset charset) {
        this.charsetEncoder = charset == null ? null : charset.newEncoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        return this;
    }

    public ByteArrayBuilder append(byte[] b, int off, int len) {
        if (b == null) {
            return this;
        }
        if (off < 0 || off > b.length || len < 0 || off + len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        this.ensureFreeCapacity(len);
        this.buffer.put(b, off, len);
        return this;
    }

    public ByteArrayBuilder append(byte[] b) {
        if (b == null) {
            return this;
        }
        return this.append(b, 0, b.length);
    }

    public ByteArrayBuilder append(CharBuffer charBuffer) {
        if (charBuffer == null) {
            return this;
        }
        this.doAppend(charBuffer);
        return this;
    }

    public ByteArrayBuilder append(char[] b, int off, int len) {
        if (b == null) {
            return this;
        }
        if (off < 0 || off > b.length || len < 0 || off + len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        return this.append(CharBuffer.wrap(b, off, len));
    }

    public ByteArrayBuilder append(char[] b) {
        if (b == null) {
            return this;
        }
        return this.append(b, 0, b.length);
    }

    public ByteArrayBuilder append(String s) {
        if (s == null) {
            return this;
        }
        return this.append(CharBuffer.wrap(s));
    }

    public ByteBuffer toByteBuffer() {
        return this.buffer != null ? this.buffer.duplicate() : ByteBuffer.allocate(0);
    }

    public byte[] toByteArray() {
        if (this.buffer == null) {
            return new byte[0];
        }
        this.buffer.flip();
        byte[] b = new byte[this.buffer.remaining()];
        this.buffer.get(b);
        this.buffer.clear();
        return b;
    }

    public void reset() {
        if (this.charsetEncoder != null) {
            this.charsetEncoder.reset();
        }
        if (this.buffer != null) {
            this.buffer.clear();
        }
    }

    public String toString() {
        return this.buffer != null ? this.buffer.toString() : "null";
    }
}

