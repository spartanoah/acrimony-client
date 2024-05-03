/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.utils;

import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream
extends InputStream {
    private final InputStream in;
    private long bytesRemaining;

    public BoundedInputStream(InputStream in, long size) {
        this.in = in;
        this.bytesRemaining = size;
    }

    @Override
    public int read() throws IOException {
        if (this.bytesRemaining > 0L) {
            --this.bytesRemaining;
            return this.in.read();
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead;
        if (len == 0) {
            return 0;
        }
        if (this.bytesRemaining == 0L) {
            return -1;
        }
        int bytesToRead = len;
        if ((long)bytesToRead > this.bytesRemaining) {
            bytesToRead = (int)this.bytesRemaining;
        }
        if ((bytesRead = this.in.read(b, off, bytesToRead)) >= 0) {
            this.bytesRemaining -= (long)bytesRead;
        }
        return bytesRead;
    }

    @Override
    public void close() {
    }

    @Override
    public long skip(long n) throws IOException {
        long bytesToSkip = Math.min(this.bytesRemaining, n);
        long bytesSkipped = this.in.skip(bytesToSkip);
        this.bytesRemaining -= bytesSkipped;
        return bytesSkipped;
    }

    public long getBytesRemaining() {
        return this.bytesRemaining;
    }
}

