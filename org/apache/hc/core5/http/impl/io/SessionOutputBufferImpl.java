/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.io.HttpTransportMetrics;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.apache.hc.core5.util.CharArrayBuffer;

public class SessionOutputBufferImpl
implements SessionOutputBuffer {
    private static final byte[] CRLF = new byte[]{13, 10};
    private final BasicHttpTransportMetrics metrics;
    private final ByteArrayBuffer buffer;
    private final int fragementSizeHint;
    private final CharsetEncoder encoder;
    private ByteBuffer bbuf;

    public SessionOutputBufferImpl(BasicHttpTransportMetrics metrics, int bufferSize, int fragementSizeHint, CharsetEncoder charEncoder) {
        Args.positive(bufferSize, "Buffer size");
        Args.notNull(metrics, "HTTP transport metrcis");
        this.metrics = metrics;
        this.buffer = new ByteArrayBuffer(bufferSize);
        this.fragementSizeHint = fragementSizeHint >= 0 ? fragementSizeHint : bufferSize;
        this.encoder = charEncoder;
    }

    public SessionOutputBufferImpl(int bufferSize) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, null);
    }

    public SessionOutputBufferImpl(int bufferSize, CharsetEncoder encoder) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, encoder);
    }

    @Override
    public int capacity() {
        return this.buffer.capacity();
    }

    @Override
    public int length() {
        return this.buffer.length();
    }

    @Override
    public int available() {
        return this.capacity() - this.length();
    }

    private void flushBuffer(OutputStream outputStream) throws IOException {
        int len = this.buffer.length();
        if (len > 0) {
            outputStream.write(this.buffer.array(), 0, len);
            this.buffer.clear();
            this.metrics.incrementBytesTransferred(len);
        }
    }

    @Override
    public void flush(OutputStream outputStream) throws IOException {
        Args.notNull(outputStream, "Output stream");
        this.flushBuffer(outputStream);
        outputStream.flush();
    }

    @Override
    public void write(byte[] b, int off, int len, OutputStream outputStream) throws IOException {
        if (b == null) {
            return;
        }
        Args.notNull(outputStream, "Output stream");
        if (len > this.fragementSizeHint || len > this.buffer.capacity()) {
            this.flushBuffer(outputStream);
            outputStream.write(b, off, len);
            this.metrics.incrementBytesTransferred(len);
        } else {
            int freecapacity = this.buffer.capacity() - this.buffer.length();
            if (len > freecapacity) {
                this.flushBuffer(outputStream);
            }
            this.buffer.append(b, off, len);
        }
    }

    @Override
    public void write(byte[] b, OutputStream outputStream) throws IOException {
        if (b == null) {
            return;
        }
        this.write(b, 0, b.length, outputStream);
    }

    @Override
    public void write(int b, OutputStream outputStream) throws IOException {
        Args.notNull(outputStream, "Output stream");
        if (this.fragementSizeHint > 0) {
            if (this.buffer.isFull()) {
                this.flushBuffer(outputStream);
            }
            this.buffer.append(b);
        } else {
            this.flushBuffer(outputStream);
            outputStream.write(b);
        }
    }

    @Override
    public void writeLine(CharArrayBuffer charbuffer, OutputStream outputStream) throws IOException {
        if (charbuffer == null) {
            return;
        }
        Args.notNull(outputStream, "Output stream");
        if (this.encoder == null) {
            int chunk;
            int off = 0;
            for (int remaining = charbuffer.length(); remaining > 0; remaining -= chunk) {
                chunk = this.buffer.capacity() - this.buffer.length();
                if ((chunk = Math.min(chunk, remaining)) > 0) {
                    this.buffer.append(charbuffer, off, chunk);
                }
                if (this.buffer.isFull()) {
                    this.flushBuffer(outputStream);
                }
                off += chunk;
            }
        } else {
            CharBuffer cbuf = CharBuffer.wrap(charbuffer.array(), 0, charbuffer.length());
            this.writeEncoded(cbuf, outputStream);
        }
        this.write(CRLF, outputStream);
    }

    private void writeEncoded(CharBuffer cbuf, OutputStream outputStream) throws IOException {
        CoderResult result;
        if (!cbuf.hasRemaining()) {
            return;
        }
        if (this.bbuf == null) {
            this.bbuf = ByteBuffer.allocate(1024);
        }
        this.encoder.reset();
        while (cbuf.hasRemaining()) {
            result = this.encoder.encode(cbuf, this.bbuf, true);
            this.handleEncodingResult(result, outputStream);
        }
        result = this.encoder.flush(this.bbuf);
        this.handleEncodingResult(result, outputStream);
        this.bbuf.clear();
    }

    private void handleEncodingResult(CoderResult result, OutputStream outputStream) throws IOException {
        if (result.isError()) {
            result.throwException();
        }
        this.bbuf.flip();
        while (this.bbuf.hasRemaining()) {
            this.write(this.bbuf.get(), outputStream);
        }
        this.bbuf.compact();
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}

