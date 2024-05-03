/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.util.Args;

public class DigestingEntityConsumer<T>
implements AsyncEntityConsumer<T> {
    private final AsyncEntityConsumer<T> wrapped;
    private final List<Header> trailers;
    private final MessageDigest digester;
    private volatile byte[] digest;

    public DigestingEntityConsumer(String algo, AsyncEntityConsumer<T> wrapped) throws NoSuchAlgorithmException {
        this.wrapped = Args.notNull(wrapped, "Entity consumer");
        this.trailers = new ArrayList<Header>();
        this.digester = MessageDigest.getInstance(algo);
    }

    @Override
    public void streamStart(EntityDetails entityDetails, FutureCallback<T> resultCallback) throws IOException, HttpException {
        this.wrapped.streamStart(entityDetails, resultCallback);
    }

    @Override
    public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.wrapped.updateCapacity(capacityChannel);
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
        src.mark();
        this.digester.update(src);
        src.reset();
        this.wrapped.consume(src);
    }

    @Override
    public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        if (trailers != null) {
            this.trailers.addAll(trailers);
        }
        this.digest = this.digester.digest();
        this.wrapped.streamEnd(trailers);
    }

    @Override
    public void failed(Exception cause) {
        this.wrapped.failed(cause);
    }

    @Override
    public T getContent() {
        return this.wrapped.getContent();
    }

    @Override
    public void releaseResources() {
        this.wrapped.releaseResources();
    }

    public List<Header> getTrailers() {
        return this.trailers != null ? new ArrayList<Header>(this.trailers) : null;
    }

    public byte[] getDigest() {
        return this.digest;
    }
}

