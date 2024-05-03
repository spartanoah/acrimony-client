/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public abstract class AbstractAsyncPushHandler<T>
implements AsyncPushConsumer {
    private final AsyncResponseConsumer<T> responseConsumer;

    public AbstractAsyncPushHandler(AsyncResponseConsumer<T> responseConsumer) {
        this.responseConsumer = Args.notNull(responseConsumer, "Response consumer");
    }

    protected abstract void handleResponse(HttpRequest var1, T var2) throws IOException, HttpException;

    protected void handleError(HttpRequest promise, Exception cause) {
    }

    @Override
    public final void consumePromise(final HttpRequest promise, HttpResponse response, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
        this.responseConsumer.consumeResponse(response, entityDetails, httpContext, new FutureCallback<T>(){

            @Override
            public void completed(T result) {
                try {
                    AbstractAsyncPushHandler.this.handleResponse(promise, result);
                } catch (Exception ex) {
                    this.failed(ex);
                }
            }

            @Override
            public void failed(Exception cause) {
                AbstractAsyncPushHandler.this.handleError(promise, cause);
                AbstractAsyncPushHandler.this.releaseResources();
            }

            @Override
            public void cancelled() {
                AbstractAsyncPushHandler.this.releaseResources();
            }
        });
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.responseConsumer.updateCapacity(capacityChannel);
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        this.responseConsumer.consume(src);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        this.responseConsumer.streamEnd(trailers);
    }

    @Override
    public final void failed(Exception cause) {
        this.responseConsumer.failed(cause);
        this.releaseResources();
    }

    @Override
    public final void releaseResources() {
        if (this.responseConsumer != null) {
            this.responseConsumer.releaseResources();
        }
    }
}

