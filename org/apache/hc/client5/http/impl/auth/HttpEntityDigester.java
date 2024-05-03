/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

class HttpEntityDigester
extends OutputStream {
    private final MessageDigest digester;
    private boolean closed;
    private byte[] digest;

    HttpEntityDigester(MessageDigest digester) {
        this.digester = digester;
        this.digester.reset();
    }

    @Override
    public void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("Stream has been already closed");
        }
        this.digester.update((byte)b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Stream has been already closed");
        }
        this.digester.update(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.digest = this.digester.digest();
        super.close();
    }

    public byte[] getDigest() {
        return this.digest;
    }
}

