/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.LazyDecompressingInputStream;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.util.Args;

abstract class DecompressingEntity
extends HttpEntityWrapper {
    private static final int BUFFER_SIZE = 2048;
    private InputStream content;

    public DecompressingEntity(HttpEntity wrapped) {
        super(wrapped);
    }

    abstract InputStream decorate(InputStream var1) throws IOException;

    private InputStream getDecompressingStream() throws IOException {
        InputStream in = this.wrappedEntity.getContent();
        return new LazyDecompressingInputStream(in, this);
    }

    public InputStream getContent() throws IOException {
        if (this.wrappedEntity.isStreaming()) {
            if (this.content == null) {
                this.content = this.getDecompressingStream();
            }
            return this.content;
        }
        return this.getDecompressingStream();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        InputStream instream = this.getContent();
        try {
            int l;
            byte[] buffer = new byte[2048];
            while ((l = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, l);
            }
        } finally {
            instream.close();
        }
    }
}

