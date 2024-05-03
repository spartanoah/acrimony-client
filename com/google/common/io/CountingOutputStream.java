/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

@Beta
public final class CountingOutputStream
extends FilterOutputStream {
    private long count;

    public CountingOutputStream(@Nullable OutputStream out) {
        super(out);
    }

    public long getCount() {
        return this.count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.count += (long)len;
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
        ++this.count;
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}

