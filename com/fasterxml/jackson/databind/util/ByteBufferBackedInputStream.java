/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedInputStream
extends InputStream {
    protected final ByteBuffer _b;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this._b = buf;
    }

    @Override
    public int available() {
        return this._b.remaining();
    }

    @Override
    public int read() throws IOException {
        return this._b.hasRemaining() ? this._b.get() & 0xFF : -1;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!this._b.hasRemaining()) {
            return -1;
        }
        len = Math.min(len, this._b.remaining());
        this._b.get(bytes, off, len);
        return len;
    }
}

