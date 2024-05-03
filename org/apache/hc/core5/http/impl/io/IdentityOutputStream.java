/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;

public class IdentityOutputStream
extends OutputStream {
    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;
    private boolean closed = false;

    public IdentityOutputStream(SessionOutputBuffer buffer, OutputStream outputStream) {
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
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
        this.buffer.write(b, off, len, this.outputStream);
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
        this.buffer.write(b, this.outputStream);
    }
}

