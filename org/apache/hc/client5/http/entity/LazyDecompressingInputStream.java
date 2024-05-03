/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.client5.http.entity.InputStreamFactory;

class LazyDecompressingInputStream
extends InputStream {
    private final InputStream wrappedStream;
    private final InputStreamFactory inputStreamFactory;
    private InputStream wrapperStream;

    public LazyDecompressingInputStream(InputStream wrappedStream, InputStreamFactory inputStreamFactory) {
        this.wrappedStream = wrappedStream;
        this.inputStreamFactory = inputStreamFactory;
    }

    private void initWrapper() throws IOException {
        if (this.wrapperStream == null) {
            this.wrapperStream = this.inputStreamFactory.create(this.wrappedStream);
        }
    }

    @Override
    public int read() throws IOException {
        this.initWrapper();
        return this.wrapperStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        this.initWrapper();
        return this.wrapperStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.initWrapper();
        return this.wrapperStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        this.initWrapper();
        return this.wrapperStream.skip(n);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int available() throws IOException {
        this.initWrapper();
        return this.wrapperStream.available();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        try {
            if (this.wrapperStream != null) {
                this.wrapperStream.close();
            }
        } finally {
            this.wrappedStream.close();
        }
    }
}

