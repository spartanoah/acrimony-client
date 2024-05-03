/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.InputStream;

public final class EmptyInputStream
extends InputStream {
    public static final EmptyInputStream INSTANCE = new EmptyInputStream();

    private EmptyInputStream() {
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public void close() {
    }

    @Override
    public void mark(int readLimit) {
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() {
        return -1;
    }

    @Override
    public int read(byte[] buf) {
        return -1;
    }

    @Override
    public int read(byte[] buf, int off, int len) {
        return -1;
    }

    @Override
    public void reset() {
    }

    @Override
    public long skip(long n) {
        return 0L;
    }
}

