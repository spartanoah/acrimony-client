/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.util.Args;

public class ContentLengthInputStream
extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private final SessionInputBuffer buffer;
    private final InputStream inputStream;
    private final long contentLength;
    private long pos = 0L;
    private boolean closed = false;

    public ContentLengthInputStream(SessionInputBuffer buffer, InputStream inputStream, long contentLength) {
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
        this.contentLength = Args.notNegative(contentLength, "Content length");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            try {
                if (this.pos < this.contentLength) {
                    byte[] buffer = new byte[2048];
                    while (this.read(buffer) >= 0) {
                    }
                }
            } finally {
                this.closed = true;
            }
        }
    }

    @Override
    public int available() throws IOException {
        int len = this.buffer.length();
        return Math.min(len, (int)(this.contentLength - this.pos));
    }

    @Override
    public int read() throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.pos >= this.contentLength) {
            return -1;
        }
        int b = this.buffer.read(this.inputStream);
        if (b == -1) {
            if (this.pos < this.contentLength) {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: %d; received: %d)", this.contentLength, this.pos);
            }
        } else {
            ++this.pos;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count;
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.pos >= this.contentLength) {
            return -1;
        }
        int chunk = len;
        if (this.pos + (long)len > this.contentLength) {
            chunk = (int)(this.contentLength - this.pos);
        }
        if ((count = this.buffer.read(b, off, chunk, this.inputStream)) == -1 && this.pos < this.contentLength) {
            throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: %d; received: %d)", this.contentLength, this.pos);
        }
        if (count > 0) {
            this.pos += (long)count;
        }
        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        int readLen;
        if (n <= 0L) {
            return 0L;
        }
        byte[] buffer = new byte[2048];
        long count = 0L;
        for (long remaining = Math.min(n, this.contentLength - this.pos); remaining > 0L && (readLen = this.read(buffer, 0, (int)Math.min(2048L, remaining))) != -1; remaining -= (long)readLen) {
            count += (long)readLen;
        }
        return count;
    }
}

