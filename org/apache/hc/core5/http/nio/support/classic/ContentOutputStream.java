/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.core5.http.nio.support.classic.ContentOutputBuffer;
import org.apache.hc.core5.util.Args;

public class ContentOutputStream
extends OutputStream {
    private final ContentOutputBuffer buffer;

    public ContentOutputStream(ContentOutputBuffer buffer) {
        Args.notNull(buffer, "Output buffer");
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        this.buffer.writeCompleted();
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.buffer.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (b == null) {
            return;
        }
        this.buffer.write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
    }
}

