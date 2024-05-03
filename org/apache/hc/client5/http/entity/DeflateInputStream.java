/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class DeflateInputStream
extends InputStream {
    private final InputStream sourceStream;

    public DeflateInputStream(InputStream wrapped) throws IOException {
        PushbackInputStream pushback = new PushbackInputStream(wrapped, 2);
        int i1 = pushback.read();
        int i2 = pushback.read();
        if (i1 == -1 || i2 == -1) {
            throw new ZipException("Unexpected end of stream");
        }
        pushback.unread(i2);
        pushback.unread(i1);
        boolean nowrap = true;
        int b1 = i1 & 0xFF;
        int compressionMethod = b1 & 0xF;
        int compressionInfo = b1 >> 4 & 0xF;
        int b2 = i2 & 0xFF;
        if (compressionMethod == 8 && compressionInfo <= 7 && (b1 << 8 | b2) % 31 == 0) {
            nowrap = false;
        }
        this.sourceStream = new DeflateStream(pushback, new Inflater(nowrap));
    }

    @Override
    public int read() throws IOException {
        return this.sourceStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.sourceStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.sourceStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.sourceStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.sourceStream.available();
    }

    @Override
    public void mark(int readLimit) {
        this.sourceStream.mark(readLimit);
    }

    @Override
    public void reset() throws IOException {
        this.sourceStream.reset();
    }

    @Override
    public boolean markSupported() {
        return this.sourceStream.markSupported();
    }

    @Override
    public void close() throws IOException {
        this.sourceStream.close();
    }

    static class DeflateStream
    extends InflaterInputStream {
        private boolean closed = false;

        public DeflateStream(InputStream in, Inflater inflater) {
            super(in, inflater);
        }

        @Override
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.inf.end();
            super.close();
        }
    }
}

