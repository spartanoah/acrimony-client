/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.client5.http.impl.Wire;

class LoggingInputStream
extends InputStream {
    private final InputStream in;
    private final Wire wire;

    public LoggingInputStream(InputStream in, Wire wire) {
        this.in = in;
        this.wire = wire;
    }

    @Override
    public int read() throws IOException {
        try {
            int b = this.in.read();
            if (b == -1) {
                this.wire.input("end of stream");
            } else {
                this.wire.input(b);
            }
            return b;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            int bytesRead = this.in.read(b);
            if (bytesRead == -1) {
                this.wire.input("end of stream");
            } else if (bytesRead > 0) {
                this.wire.input(b, 0, bytesRead);
            }
            return bytesRead;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int bytesRead = this.in.read(b, off, len);
            if (bytesRead == -1) {
                this.wire.input("end of stream");
            } else if (bytesRead > 0) {
                this.wire.input(b, off, bytesRead);
            }
            return bytesRead;
        } catch (IOException ex) {
            this.wire.input("[read] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return super.skip(n);
        } catch (IOException ex) {
            this.wire.input("[skip] I/O error: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return this.in.available();
        } catch (IOException ex) {
            this.wire.input("[available] I/O error : " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        try {
            this.in.close();
        } catch (IOException ex) {
            this.wire.input("[close] I/O error: " + ex.getMessage());
            throw ex;
        }
    }
}

