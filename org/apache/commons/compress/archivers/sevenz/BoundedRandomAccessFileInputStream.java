/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.sevenz;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

class BoundedRandomAccessFileInputStream
extends InputStream {
    private final RandomAccessFile file;
    private long bytesRemaining;

    public BoundedRandomAccessFileInputStream(RandomAccessFile file, long size) {
        this.file = file;
        this.bytesRemaining = size;
    }

    public int read() throws IOException {
        if (this.bytesRemaining > 0L) {
            --this.bytesRemaining;
            return this.file.read();
        }
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead;
        if (this.bytesRemaining == 0L) {
            return -1;
        }
        int bytesToRead = len;
        if ((long)bytesToRead > this.bytesRemaining) {
            bytesToRead = (int)this.bytesRemaining;
        }
        if ((bytesRead = this.file.read(b, off, bytesToRead)) >= 0) {
            this.bytesRemaining -= (long)bytesRead;
        }
        return bytesRead;
    }

    public void close() {
    }
}

