/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityConsumer;
import org.apache.hc.core5.util.ByteArrayBuffer;

public class BasicAsyncEntityConsumer
extends AbstractBinAsyncEntityConsumer<byte[]> {
    private final ByteArrayBuffer buffer = new ByteArrayBuffer(1024);

    @Override
    protected void streamStart(ContentType contentType) throws HttpException, IOException {
    }

    @Override
    protected int capacityIncrement() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void data(ByteBuffer src, boolean endOfStream) throws IOException {
        if (src == null) {
            return;
        }
        if (src.hasArray()) {
            this.buffer.append(src.array(), src.arrayOffset() + src.position(), src.remaining());
        } else {
            while (src.hasRemaining()) {
                this.buffer.append(src.get());
            }
        }
    }

    @Override
    protected byte[] generateContent() throws IOException {
        return this.buffer.toByteArray();
    }

    @Override
    public void releaseResources() {
        this.buffer.clear();
    }
}

