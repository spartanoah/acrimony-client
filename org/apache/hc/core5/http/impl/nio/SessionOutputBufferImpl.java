/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import org.apache.hc.core5.http.impl.nio.ExpandableBuffer;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

class SessionOutputBufferImpl
extends ExpandableBuffer
implements SessionOutputBuffer {
    private static final byte[] CRLF = new byte[]{13, 10};
    private final CharsetEncoder charEncoder;
    private final int lineBuffersize;
    private CharBuffer charbuffer;

    public SessionOutputBufferImpl(int bufferSize, int lineBuffersize, CharsetEncoder charEncoder) {
        super(bufferSize);
        this.lineBuffersize = Args.positive(lineBuffersize, "Line buffer size");
        this.charEncoder = charEncoder;
    }

    public SessionOutputBufferImpl(int bufferSize, int lineBufferSize, Charset charset) {
        this(bufferSize, lineBufferSize, charset != null ? charset.newEncoder() : null);
    }

    public SessionOutputBufferImpl(int bufferSize, int lineBufferSize) {
        this(bufferSize, lineBufferSize, (CharsetEncoder)null);
    }

    public SessionOutputBufferImpl(int bufferSize) {
        this(bufferSize, 256);
    }

    @Override
    public int length() {
        return super.length();
    }

    @Override
    public boolean hasData() {
        return super.hasData();
    }

    @Override
    public int capacity() {
        return super.capacity();
    }

    @Override
    public int flush(WritableByteChannel channel) throws IOException {
        Args.notNull(channel, "Channel");
        this.setOutputMode();
        return channel.write(this.buffer());
    }

    @Override
    public void write(ByteBuffer src) {
        if (src == null) {
            return;
        }
        this.setInputMode();
        this.ensureAdjustedCapacity(this.buffer().position() + src.remaining());
        this.buffer().put(src);
    }

    @Override
    public void write(ReadableByteChannel src) throws IOException {
        if (src == null) {
            return;
        }
        this.setInputMode();
        src.read(this.buffer());
    }

    private void write(byte[] b) {
        if (b == null) {
            return;
        }
        this.setInputMode();
        boolean off = false;
        int len = b.length;
        int requiredCapacity = this.buffer().position() + len;
        this.ensureAdjustedCapacity(requiredCapacity);
        this.buffer().put(b, 0, len);
    }

    private void writeCRLF() {
        this.write(CRLF);
    }

    @Override
    public void writeLine(CharArrayBuffer lineBuffer) throws CharacterCodingException {
        if (lineBuffer == null) {
            return;
        }
        this.setInputMode();
        if (lineBuffer.length() > 0) {
            if (this.charEncoder == null) {
                int requiredCapacity = this.buffer().position() + lineBuffer.length();
                this.ensureCapacity(requiredCapacity);
                if (this.buffer().hasArray()) {
                    byte[] b = this.buffer().array();
                    int len = lineBuffer.length();
                    int off = this.buffer().position();
                    int arrayOffset = this.buffer().arrayOffset();
                    for (int i = 0; i < len; ++i) {
                        b[arrayOffset + off + i] = (byte)lineBuffer.charAt(i);
                    }
                    this.buffer().position(off + len);
                } else {
                    for (int i = 0; i < lineBuffer.length(); ++i) {
                        this.buffer().put((byte)lineBuffer.charAt(i));
                    }
                }
            } else {
                int l;
                if (this.charbuffer == null) {
                    this.charbuffer = CharBuffer.allocate(this.lineBuffersize);
                }
                this.charEncoder.reset();
                int offset = 0;
                for (int remaining = lineBuffer.length(); remaining > 0; remaining -= l) {
                    l = this.charbuffer.remaining();
                    boolean eol = false;
                    if (remaining <= l) {
                        l = remaining;
                        eol = true;
                    }
                    this.charbuffer.put(lineBuffer.array(), offset, l);
                    this.charbuffer.flip();
                    boolean retry = true;
                    while (retry) {
                        CoderResult result = this.charEncoder.encode(this.charbuffer, this.buffer(), eol);
                        if (result.isError()) {
                            result.throwException();
                        }
                        if (result.isOverflow()) {
                            this.expand();
                        }
                        retry = !result.isUnderflow();
                    }
                    this.charbuffer.compact();
                    offset += l;
                }
                boolean retry = true;
                while (retry) {
                    CoderResult result = this.charEncoder.flush(this.buffer());
                    if (result.isError()) {
                        result.throwException();
                    }
                    if (result.isOverflow()) {
                        this.expand();
                    }
                    retry = !result.isUnderflow();
                }
            }
        }
        this.writeCRLF();
    }
}

