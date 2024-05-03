/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;

public abstract class AbstractBinDataConsumer
implements AsyncDataConsumer {
    private static final ByteBuffer EMPTY = ByteBuffer.wrap(new byte[0]);

    protected abstract int capacityIncrement();

    protected abstract void data(ByteBuffer var1, boolean var2) throws IOException;

    protected abstract void completed() throws IOException;

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        capacityChannel.update(this.capacityIncrement());
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        this.data(src, false);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        this.data(EMPTY, true);
        this.completed();
    }
}

