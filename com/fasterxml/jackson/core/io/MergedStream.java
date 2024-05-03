/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.io.IOContext;
import java.io.IOException;
import java.io.InputStream;

public final class MergedStream
extends InputStream {
    private final IOContext _ctxt;
    private final InputStream _in;
    private byte[] _b;
    private int _ptr;
    private final int _end;

    public MergedStream(IOContext ctxt, InputStream in, byte[] buf, int start, int end) {
        this._ctxt = ctxt;
        this._in = in;
        this._b = buf;
        this._ptr = start;
        this._end = end;
    }

    @Override
    public int available() throws IOException {
        if (this._b != null) {
            return this._end - this._ptr;
        }
        return this._in.available();
    }

    @Override
    public void close() throws IOException {
        this._free();
        this._in.close();
    }

    @Override
    public void mark(int readlimit) {
        if (this._b == null) {
            this._in.mark(readlimit);
        }
    }

    @Override
    public boolean markSupported() {
        return this._b == null && this._in.markSupported();
    }

    @Override
    public int read() throws IOException {
        if (this._b != null) {
            int c = this._b[this._ptr++] & 0xFF;
            if (this._ptr >= this._end) {
                this._free();
            }
            return c;
        }
        return this._in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this._b != null) {
            int avail = this._end - this._ptr;
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(this._b, this._ptr, b, off, len);
            this._ptr += len;
            if (this._ptr >= this._end) {
                this._free();
            }
            return len;
        }
        return this._in.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        if (this._b == null) {
            this._in.reset();
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long count = 0L;
        if (this._b != null) {
            int amount = this._end - this._ptr;
            if ((long)amount > n) {
                this._ptr += (int)n;
                return n;
            }
            this._free();
            count += (long)amount;
            n -= (long)amount;
        }
        if (n > 0L) {
            count += this._in.skip(n);
        }
        return count;
    }

    private void _free() {
        byte[] buf = this._b;
        if (buf != null) {
            this._b = null;
            if (this._ctxt != null) {
                this._ctxt.releaseReadIOBuffer(buf);
            }
        }
    }
}

