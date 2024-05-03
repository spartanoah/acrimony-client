/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedOutputStream
extends OutputStream {
    protected final ByteBuffer _b;

    public ByteBufferBackedOutputStream(ByteBuffer buf) {
        this._b = buf;
    }

    @Override
    public void write(int b) throws IOException {
        this._b.put((byte)b);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        this._b.put(bytes, off, len);
    }
}

