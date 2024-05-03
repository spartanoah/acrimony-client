/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.io.HttpTransportMetrics;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.apache.hc.core5.util.CharArrayBuffer;

public class SessionInputBufferImpl
implements SessionInputBuffer {
    private final BasicHttpTransportMetrics metrics;
    private final byte[] buffer;
    private final ByteArrayBuffer lineBuffer;
    private final int minChunkLimit;
    private final int maxLineLen;
    private final CharsetDecoder decoder;
    private int bufferPos;
    private int bufferLen;
    private CharBuffer cbuf;

    public SessionInputBufferImpl(BasicHttpTransportMetrics metrics, int bufferSize, int minChunkLimit, int maxLineLen, CharsetDecoder charDecoder) {
        Args.notNull(metrics, "HTTP transport metrcis");
        Args.positive(bufferSize, "Buffer size");
        this.metrics = metrics;
        this.buffer = new byte[bufferSize];
        this.bufferPos = 0;
        this.bufferLen = 0;
        this.minChunkLimit = minChunkLimit >= 0 ? minChunkLimit : 512;
        this.maxLineLen = maxLineLen > 0 ? maxLineLen : 0;
        this.lineBuffer = new ByteArrayBuffer(bufferSize);
        this.decoder = charDecoder;
    }

    public SessionInputBufferImpl(BasicHttpTransportMetrics metrics, int bufferSize) {
        this(metrics, bufferSize, bufferSize, 0, null);
    }

    public SessionInputBufferImpl(int bufferSize, int maxLineLen) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, maxLineLen, null);
    }

    public SessionInputBufferImpl(int bufferSize, CharsetDecoder decoder) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, 0, decoder);
    }

    public SessionInputBufferImpl(int bufferSize) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, 0, null);
    }

    @Override
    public int capacity() {
        return this.buffer.length;
    }

    @Override
    public int length() {
        return this.bufferLen - this.bufferPos;
    }

    @Override
    public int available() {
        return this.capacity() - this.length();
    }

    public int fillBuffer(InputStream inputStream) throws IOException {
        int len;
        int off;
        int readLen;
        Args.notNull(inputStream, "Input stream");
        if (this.bufferPos > 0) {
            int len2 = this.bufferLen - this.bufferPos;
            if (len2 > 0) {
                System.arraycopy(this.buffer, this.bufferPos, this.buffer, 0, len2);
            }
            this.bufferPos = 0;
            this.bufferLen = len2;
        }
        if ((readLen = inputStream.read(this.buffer, off = this.bufferLen, len = this.buffer.length - off)) == -1) {
            return -1;
        }
        this.bufferLen = off + readLen;
        this.metrics.incrementBytesTransferred(readLen);
        return readLen;
    }

    public boolean hasBufferedData() {
        return this.bufferPos < this.bufferLen;
    }

    public void clear() {
        this.bufferPos = 0;
        this.bufferLen = 0;
    }

    @Override
    public int read(InputStream inputStream) throws IOException {
        Args.notNull(inputStream, "Input stream");
        while (!this.hasBufferedData()) {
            int readLen = this.fillBuffer(inputStream);
            if (readLen != -1) continue;
            return -1;
        }
        return this.buffer[this.bufferPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len, InputStream inputStream) throws IOException {
        Args.notNull(inputStream, "Input stream");
        if (b == null) {
            return 0;
        }
        if (this.hasBufferedData()) {
            int chunk = Math.min(len, this.bufferLen - this.bufferPos);
            System.arraycopy(this.buffer, this.bufferPos, b, off, chunk);
            this.bufferPos += chunk;
            return chunk;
        }
        if (len > this.minChunkLimit) {
            int read = inputStream.read(b, off, len);
            if (read > 0) {
                this.metrics.incrementBytesTransferred(read);
            }
            return read;
        }
        while (!this.hasBufferedData()) {
            int readLen = this.fillBuffer(inputStream);
            if (readLen != -1) continue;
            return -1;
        }
        int chunk = Math.min(len, this.bufferLen - this.bufferPos);
        System.arraycopy(this.buffer, this.bufferPos, b, off, chunk);
        this.bufferPos += chunk;
        return chunk;
    }

    @Override
    public int read(byte[] b, InputStream inputStream) throws IOException {
        if (b == null) {
            return 0;
        }
        return this.read(b, 0, b.length, inputStream);
    }

    @Override
    public int readLine(CharArrayBuffer charBuffer, InputStream inputStream) throws IOException {
        Args.notNull(charBuffer, "Char array buffer");
        Args.notNull(inputStream, "Input stream");
        int readLen = 0;
        boolean retry = true;
        while (retry) {
            int len;
            int currentLen;
            int pos = -1;
            for (int i = this.bufferPos; i < this.bufferLen; ++i) {
                if (this.buffer[i] != 10) continue;
                pos = i;
                break;
            }
            if (this.maxLineLen > 0 && (currentLen = this.lineBuffer.length() + (pos >= 0 ? pos : this.bufferLen) - this.bufferPos) >= this.maxLineLen) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            if (pos != -1) {
                if (this.lineBuffer.isEmpty()) {
                    return this.lineFromReadBuffer(charBuffer, pos);
                }
                retry = false;
                len = pos + 1 - this.bufferPos;
                this.lineBuffer.append(this.buffer, this.bufferPos, len);
                this.bufferPos = pos + 1;
                continue;
            }
            if (this.hasBufferedData()) {
                len = this.bufferLen - this.bufferPos;
                this.lineBuffer.append(this.buffer, this.bufferPos, len);
                this.bufferPos = this.bufferLen;
            }
            if ((readLen = this.fillBuffer(inputStream)) != -1) continue;
            retry = false;
        }
        if (readLen == -1 && this.lineBuffer.isEmpty()) {
            return -1;
        }
        return this.lineFromLineBuffer(charBuffer);
    }

    private int lineFromLineBuffer(CharArrayBuffer charBuffer) throws IOException {
        int len = this.lineBuffer.length();
        if (len > 0) {
            if (this.lineBuffer.byteAt(len - 1) == 10) {
                --len;
            }
            if (len > 0 && this.lineBuffer.byteAt(len - 1) == 13) {
                --len;
            }
        }
        if (this.decoder == null) {
            charBuffer.append(this.lineBuffer, 0, len);
        } else {
            ByteBuffer bbuf = ByteBuffer.wrap(this.lineBuffer.array(), 0, len);
            len = this.appendDecoded(charBuffer, bbuf);
        }
        this.lineBuffer.clear();
        return len;
    }

    private int lineFromReadBuffer(CharArrayBuffer charbuffer, int position) throws IOException {
        int pos = position;
        int off = this.bufferPos;
        this.bufferPos = pos + 1;
        if (pos > off && this.buffer[pos - 1] == 13) {
            --pos;
        }
        int len = pos - off;
        if (this.decoder == null) {
            charbuffer.append(this.buffer, off, len);
        } else {
            ByteBuffer bbuf = ByteBuffer.wrap(this.buffer, off, len);
            len = this.appendDecoded(charbuffer, bbuf);
        }
        return len;
    }

    private int appendDecoded(CharArrayBuffer charbuffer, ByteBuffer bbuf) throws IOException {
        CoderResult result;
        if (!bbuf.hasRemaining()) {
            return 0;
        }
        if (this.cbuf == null) {
            this.cbuf = CharBuffer.allocate(1024);
        }
        this.decoder.reset();
        int len = 0;
        while (bbuf.hasRemaining()) {
            result = this.decoder.decode(bbuf, this.cbuf, true);
            len += this.handleDecodingResult(result, charbuffer);
        }
        result = this.decoder.flush(this.cbuf);
        this.cbuf.clear();
        return len += this.handleDecodingResult(result, charbuffer);
    }

    private int handleDecodingResult(CoderResult result, CharArrayBuffer charBuffer) throws IOException {
        if (result.isError()) {
            result.throwException();
        }
        this.cbuf.flip();
        int len = this.cbuf.remaining();
        while (this.cbuf.hasRemaining()) {
            charBuffer.append(this.cbuf.get());
        }
        this.cbuf.compact();
        return len;
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}

