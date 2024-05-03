/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.impl.nio.ExpandableBuffer;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

class SessionInputBufferImpl
extends ExpandableBuffer
implements SessionInputBuffer {
    private final CharsetDecoder charDecoder;
    private final int lineBuffersize;
    private final int maxLineLen;
    private CharBuffer charbuffer;

    public SessionInputBufferImpl(int bufferSize, int lineBuffersize, int maxLineLen, CharsetDecoder charDecoder) {
        super(bufferSize);
        this.lineBuffersize = Args.positive(lineBuffersize, "Line buffer size");
        this.maxLineLen = maxLineLen > 0 ? maxLineLen : 0;
        this.charDecoder = charDecoder;
    }

    public SessionInputBufferImpl(int bufferSize, int lineBuffersize, int maxLineLen, Charset charset) {
        this(bufferSize, lineBuffersize, maxLineLen, charset != null ? charset.newDecoder() : null);
    }

    public SessionInputBufferImpl(int bufferSize, int lineBuffersize, int maxLineLen) {
        this(bufferSize, lineBuffersize, maxLineLen, (CharsetDecoder)null);
    }

    public SessionInputBufferImpl(int bufferSize, int lineBuffersize) {
        this(bufferSize, lineBuffersize, 0, (CharsetDecoder)null);
    }

    public SessionInputBufferImpl(int bufferSize) {
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

    public void put(ByteBuffer src) {
        if (src != null && src.hasRemaining()) {
            this.setInputMode();
            this.ensureAdjustedCapacity(this.buffer().position() + src.remaining());
            this.buffer().put(src);
        }
    }

    @Override
    public int fill(ReadableByteChannel channel) throws IOException {
        Args.notNull(channel, "Channel");
        this.setInputMode();
        if (!this.buffer().hasRemaining()) {
            this.expand();
        }
        return channel.read(this.buffer());
    }

    @Override
    public int read() {
        this.setOutputMode();
        return this.buffer().get() & 0xFF;
    }

    @Override
    public int read(ByteBuffer dst, int maxLen) {
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        int len = Math.min(dst.remaining(), maxLen);
        int chunk = Math.min(this.buffer().remaining(), len);
        if (this.buffer().remaining() > chunk) {
            int oldLimit = this.buffer().limit();
            int newLimit = this.buffer().position() + chunk;
            this.buffer().limit(newLimit);
            dst.put(this.buffer());
            this.buffer().limit(oldLimit);
            return len;
        }
        dst.put(this.buffer());
        return chunk;
    }

    @Override
    public int read(ByteBuffer dst) {
        if (dst == null) {
            return 0;
        }
        return this.read(dst, dst.remaining());
    }

    @Override
    public int read(WritableByteChannel dst, int maxLen) throws IOException {
        int bytesRead;
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        if (this.buffer().remaining() > maxLen) {
            int oldLimit = this.buffer().limit();
            int newLimit = oldLimit - (this.buffer().remaining() - maxLen);
            this.buffer().limit(newLimit);
            bytesRead = dst.write(this.buffer());
            this.buffer().limit(oldLimit);
        } else {
            bytesRead = dst.write(this.buffer());
        }
        return bytesRead;
    }

    @Override
    public int read(WritableByteChannel dst) throws IOException {
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        return dst.write(this.buffer());
    }

    @Override
    public boolean readLine(CharArrayBuffer lineBuffer, boolean endOfStream) throws IOException {
        int currentLen;
        this.setOutputMode();
        int pos = -1;
        for (int i = this.buffer().position(); i < this.buffer().limit(); ++i) {
            byte b = this.buffer().get(i);
            if (b != 10) continue;
            pos = i + 1;
            break;
        }
        if (this.maxLineLen > 0 && (currentLen = (pos > 0 ? pos : this.buffer().limit()) - this.buffer().position()) >= this.maxLineLen) {
            throw new MessageConstraintException("Maximum line length limit exceeded");
        }
        if (pos == -1) {
            if (endOfStream && this.buffer().hasRemaining()) {
                pos = this.buffer().limit();
            } else {
                return false;
            }
        }
        int origLimit = this.buffer().limit();
        this.buffer().limit(pos);
        int requiredCapacity = this.buffer().limit() - this.buffer().position();
        lineBuffer.ensureCapacity(requiredCapacity);
        if (this.charDecoder == null) {
            if (this.buffer().hasArray()) {
                byte[] b = this.buffer().array();
                int off = this.buffer().position();
                int len = this.buffer().remaining();
                lineBuffer.append(b, this.buffer().arrayOffset() + off, len);
                this.buffer().position(off + len);
            } else {
                while (this.buffer().hasRemaining()) {
                    lineBuffer.append((char)(this.buffer().get() & 0xFF));
                }
            }
        } else {
            CoderResult result;
            if (this.charbuffer == null) {
                this.charbuffer = CharBuffer.allocate(this.lineBuffersize);
            }
            this.charDecoder.reset();
            do {
                if ((result = this.charDecoder.decode(this.buffer(), this.charbuffer, true)).isError()) {
                    result.throwException();
                }
                if (!result.isOverflow()) continue;
                this.charbuffer.flip();
                lineBuffer.append(this.charbuffer.array(), this.charbuffer.arrayOffset() + this.charbuffer.position(), this.charbuffer.remaining());
                this.charbuffer.clear();
            } while (!result.isUnderflow());
            this.charDecoder.flush(this.charbuffer);
            this.charbuffer.flip();
            if (this.charbuffer.hasRemaining()) {
                lineBuffer.append(this.charbuffer.array(), this.charbuffer.arrayOffset() + this.charbuffer.position(), this.charbuffer.remaining());
            }
        }
        this.buffer().limit(origLimit);
        int l = lineBuffer.length();
        if (l > 0) {
            if (lineBuffer.charAt(l - 1) == '\n') {
                lineBuffer.setLength(--l);
            }
            if (l > 0 && lineBuffer.charAt(l - 1) == '\r') {
                lineBuffer.setLength(--l);
            }
        }
        return true;
    }
}

