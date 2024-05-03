/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public class DataOutputAsStream
extends OutputStream {
    protected final DataOutput _output;

    public DataOutputAsStream(DataOutput out) {
        this._output = out;
    }

    @Override
    public void write(int b) throws IOException {
        this._output.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this._output.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        this._output.write(b, offset, length);
    }
}

