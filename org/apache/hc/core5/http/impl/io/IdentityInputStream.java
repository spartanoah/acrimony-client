/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.util.Args;

public class IdentityInputStream
extends InputStream {
    private final SessionInputBuffer buffer;
    private final InputStream inputStream;
    private boolean closed = false;

    public IdentityInputStream(SessionInputBuffer buffer, InputStream inputStream) {
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
    }

    @Override
    public int available() throws IOException {
        if (this.closed) {
            return 0;
        }
        int n = this.buffer.length();
        return n > 0 ? n : this.inputStream.available();
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public int read() throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        return this.buffer.read(this.inputStream);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        return this.buffer.read(b, off, len, this.inputStream);
    }
}

