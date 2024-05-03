/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.nio.support.classic.ContentInputBuffer;
import org.apache.hc.core5.util.Args;

public class ContentInputStream
extends InputStream {
    private final ContentInputBuffer buffer;

    public ContentInputStream(ContentInputBuffer buffer) {
        Args.notNull(buffer, "Input buffer");
        this.buffer = buffer;
    }

    @Override
    public int available() throws IOException {
        return this.buffer.length();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.buffer.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (b == null) {
            return 0;
        }
        return this.buffer.read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        return this.buffer.read();
    }

    @Override
    public void close() throws IOException {
        byte[] tmp = new byte[1024];
        while (this.buffer.read(tmp, 0, tmp.length) >= 0) {
        }
        super.close();
    }
}

