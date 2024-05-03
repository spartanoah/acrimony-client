/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicAsyncServerExpectationDecorator
implements AsyncServerExchangeHandler {
    private final AsyncServerExchangeHandler handler;
    private final Callback<Exception> exceptionCallback;
    private final AtomicReference<AsyncResponseProducer> responseProducerRef;

    public BasicAsyncServerExpectationDecorator(AsyncServerExchangeHandler handler, Callback<Exception> exceptionCallback) {
        this.handler = Args.notNull(handler, "Handler");
        this.exceptionCallback = exceptionCallback;
        this.responseProducerRef = new AtomicReference<Object>(null);
    }

    public BasicAsyncServerExpectationDecorator(AsyncServerExchangeHandler handler) {
        this(handler, null);
    }

    protected AsyncResponseProducer verify(HttpRequest request, HttpContext context) throws IOException, HttpException {
        return null;
    }

    @Override
    public final void handleRequest(HttpRequest request, EntityDetails entityDetails, ResponseChannel responseChannel, HttpContext context) throws HttpException, IOException {
        Header h;
        if (entityDetails != null && (h = request.getFirstHeader("Expect")) != null && "100-continue".equalsIgnoreCase(h.getValue())) {
            AsyncResponseProducer producer = this.verify(request, context);
            if (producer != null) {
                this.responseProducerRef.set(producer);
                producer.sendResponse(responseChannel, context);
                return;
            }
            responseChannel.sendInformation(new BasicHttpResponse(100), context);
        }
        this.handler.handleRequest(request, entityDetails, responseChannel, context);
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        AsyncResponseProducer responseProducer = this.responseProducerRef.get();
        if (responseProducer == null) {
            this.handler.updateCapacity(capacityChannel);
        } else {
            capacityChannel.update(Integer.MAX_VALUE);
        }
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        AsyncResponseProducer responseProducer = this.responseProducerRef.get();
        if (responseProducer == null) {
            this.handler.consume(src);
        }
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        AsyncResponseProducer responseProducer = this.responseProducerRef.get();
        if (responseProducer == null) {
            this.handler.streamEnd(trailers);
        }
    }

    @Override
    public final int available() {
        AsyncResponseProducer responseProducer = this.responseProducerRef.get();
        return responseProducer == null ? this.handler.available() : responseProducer.available();
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        AsyncResponseProducer responseProducer = this.responseProducerRef.get();
        if (responseProducer == null) {
            this.handler.produce(channel);
        } else {
            responseProducer.produce(channel);
        }
    }

    @Override
    public final void failed(Exception cause) {
        AsyncResponseProducer dataProducer;
        if (this.exceptionCallback != null) {
            this.exceptionCallback.execute(cause);
        }
        if ((dataProducer = this.responseProducerRef.get()) == null) {
            this.handler.failed(cause);
        } else {
            dataProducer.failed(cause);
        }
    }

    @Override
    public final void releaseResources() {
        this.handler.releaseResources();
        AsyncResponseProducer dataProducer = this.responseProducerRef.getAndSet(null);
        if (dataProducer != null) {
            dataProducer.releaseResources();
        }
    }
}

