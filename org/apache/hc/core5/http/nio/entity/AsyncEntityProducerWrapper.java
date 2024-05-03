/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class AsyncEntityProducerWrapper
implements AsyncEntityProducer {
    private final AsyncEntityProducer wrappedEntityProducer;

    public AsyncEntityProducerWrapper(AsyncEntityProducer wrappedEntityProducer) {
        this.wrappedEntityProducer = Args.notNull(wrappedEntityProducer, "Wrapped entity producer");
    }

    @Override
    public boolean isRepeatable() {
        return this.wrappedEntityProducer.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return this.wrappedEntityProducer.isChunked();
    }

    @Override
    public long getContentLength() {
        return this.wrappedEntityProducer.getContentLength();
    }

    @Override
    public String getContentType() {
        return this.wrappedEntityProducer.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return this.wrappedEntityProducer.getContentEncoding();
    }

    @Override
    public Set<String> getTrailerNames() {
        return this.wrappedEntityProducer.getTrailerNames();
    }

    @Override
    public int available() {
        return this.wrappedEntityProducer.available();
    }

    @Override
    public void produce(DataStreamChannel channel) throws IOException {
        this.wrappedEntityProducer.produce(channel);
    }

    @Override
    public void failed(Exception cause) {
        this.wrappedEntityProducer.failed(cause);
    }

    @Override
    public void releaseResources() {
        this.wrappedEntityProducer.releaseResources();
    }

    public String toString() {
        return "Wrapper [" + this.wrappedEntityProducer + "]";
    }
}

