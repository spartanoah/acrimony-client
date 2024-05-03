/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class ChunkedOutputStream
extends OutputStream {
    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;
    private final byte[] cache;
    private int cachePosition = 0;
    private boolean wroteLastChunk = false;
    private boolean closed = false;
    private final CharArrayBuffer lineBuffer;
    private final Supplier<List<? extends Header>> trailerSupplier;

    public ChunkedOutputStream(SessionOutputBuffer buffer, OutputStream outputStream, int chunkSizeHint, Supplier<List<? extends Header>> trailerSupplier) {
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
        this.cache = new byte[chunkSizeHint > 0 ? chunkSizeHint : 2048];
        this.lineBuffer = new CharArrayBuffer(32);
        this.trailerSupplier = trailerSupplier;
    }

    public ChunkedOutputStream(SessionOutputBuffer buffer, OutputStream outputStream, int chunkSizeHint) {
        this(buffer, outputStream, chunkSizeHint, null);
    }

    private void flushCache() throws IOException {
        if (this.cachePosition > 0) {
            this.lineBuffer.clear();
            this.lineBuffer.append(Integer.toHexString(this.cachePosition));
            this.buffer.writeLine(this.lineBuffer, this.outputStream);
            this.buffer.write(this.cache, 0, this.cachePosition, this.outputStream);
            this.lineBuffer.clear();
            this.buffer.writeLine(this.lineBuffer, this.outputStream);
            this.cachePosition = 0;
        }
    }

    private void flushCacheWithAppend(byte[] bufferToAppend, int off, int len) throws IOException {
        this.lineBuffer.clear();
        this.lineBuffer.append(Integer.toHexString(this.cachePosition + len));
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        this.buffer.write(this.cache, 0, this.cachePosition, this.outputStream);
        this.buffer.write(bufferToAppend, off, len, this.outputStream);
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        this.cachePosition = 0;
    }

    private void writeClosingChunk() throws IOException {
        this.lineBuffer.clear();
        this.lineBuffer.append('0');
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        this.writeTrailers();
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
    }

    private void writeTrailers() throws IOException {
        List<? extends Header> trailers;
        List<? extends Header> list = trailers = this.trailerSupplier != null ? this.trailerSupplier.get() : null;
        if (trailers != null) {
            for (int i = 0; i < trailers.size(); ++i) {
                Header header = trailers.get(i);
                if (header instanceof FormattedHeader) {
                    CharArrayBuffer chbuffer = ((FormattedHeader)header).getBuffer();
                    this.buffer.writeLine(chbuffer, this.outputStream);
                    continue;
                }
                this.lineBuffer.clear();
                BasicLineFormatter.INSTANCE.formatHeader(this.lineBuffer, header);
                this.buffer.writeLine(this.lineBuffer, this.outputStream);
            }
        }
    }

    public void finish() throws IOException {
        if (!this.wroteLastChunk) {
            this.flushCache();
            this.writeClosingChunk();
            this.wroteLastChunk = true;
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        this.cache[this.cachePosition] = (byte)b;
        ++this.cachePosition;
        if (this.cachePosition == this.cache.length) {
            this.flushCache();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] src, int off, int len) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (len >= this.cache.length - this.cachePosition) {
            this.flushCacheWithAppend(src, off, len);
        } else {
            System.arraycopy(src, off, this.cache, this.cachePosition, len);
            this.cachePosition += len;
        }
    }

    @Override
    public void flush() throws IOException {
        this.flushCache();
        this.buffer.flush(this.outputStream);
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.finish();
            this.buffer.flush(this.outputStream);
        }
    }
}

