/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;

public final class NoopEntityConsumer
implements AsyncEntityConsumer<Void> {
    private volatile FutureCallback<Void> resultCallback;

    @Override
    public final void streamStart(EntityDetails entityDetails, FutureCallback<Void> resultCallback) throws IOException, HttpException {
        this.resultCallback = resultCallback;
    }

    @Override
    public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        capacityChannel.update(Integer.MAX_VALUE);
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws IOException {
        if (this.resultCallback != null) {
            this.resultCallback.completed(null);
        }
    }

    @Override
    public void failed(Exception cause) {
        if (this.resultCallback != null) {
            this.resultCallback.failed(cause);
        }
    }

    @Override
    public Void getContent() {
        return null;
    }

    @Override
    public void releaseResources() {
    }
}

