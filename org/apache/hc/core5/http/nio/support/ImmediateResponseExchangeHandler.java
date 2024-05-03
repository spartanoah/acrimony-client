/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public final class ImmediateResponseExchangeHandler
implements AsyncServerExchangeHandler {
    private final AsyncResponseProducer responseProducer;

    public ImmediateResponseExchangeHandler(AsyncResponseProducer responseProducer) {
        this.responseProducer = Args.notNull(responseProducer, "Response producer");
    }

    public ImmediateResponseExchangeHandler(HttpResponse response, String message) {
        this(new BasicResponseProducer(response, AsyncEntityProducers.create(message)));
    }

    public ImmediateResponseExchangeHandler(int status, String message) {
        this(new BasicHttpResponse(status), message);
    }

    @Override
    public void handleRequest(HttpRequest request, EntityDetails entityDetails, ResponseChannel responseChannel, HttpContext context) throws HttpException, IOException {
        this.responseProducer.sendResponse(responseChannel, context);
    }

    @Override
    public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        capacityChannel.update(Integer.MAX_VALUE);
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
    }

    @Override
    public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
    }

    @Override
    public final int available() {
        return this.responseProducer.available();
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        this.responseProducer.produce(channel);
    }

    @Override
    public final void failed(Exception cause) {
        this.responseProducer.failed(cause);
        this.releaseResources();
    }

    @Override
    public final void releaseResources() {
        this.responseProducer.releaseResources();
    }
}

