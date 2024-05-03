/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;

public class ContentLengthOutputStream
extends OutputStream {
    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;
    private final long contentLength;
    private long total;
    private boolean closed;

    public ContentLengthOutputStream(SessionOutputBuffer buffer, OutputStream outputStream, long contentLength) {
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
        this.contentLength = Args.notNegative(contentLength, "Content length");
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.buffer.flush(this.outputStream);
        }
    }

    @Override
    public void flush() throws IOException {
        this.buffer.flush(this.outputStream);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.total < this.contentLength) {
            int chunk = len;
            long max = this.contentLength - this.total;
            if ((long)chunk > max) {
                chunk = (int)max;
            }
            this.buffer.write(b, off, chunk, this.outputStream);
            this.total += (long)chunk;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.total < this.contentLength) {
            this.buffer.write(b, this.outputStream);
            ++this.total;
        }
    }
}

