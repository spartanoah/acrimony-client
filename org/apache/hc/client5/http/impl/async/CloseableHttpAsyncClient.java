/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.util.concurrent.Future;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public abstract class CloseableHttpAsyncClient
implements HttpAsyncClient,
ModalCloseable {
    public abstract void start();

    public abstract IOReactorStatus getStatus();

    public abstract void awaitShutdown(TimeValue var1) throws InterruptedException;

    public abstract void initiateShutdown();

    protected abstract <T> Future<T> doExecute(HttpHost var1, AsyncRequestProducer var2, AsyncResponseConsumer<T> var3, HandlerFactory<AsyncPushConsumer> var4, HttpContext var5, FutureCallback<T> var6);

    public final <T> Future<T> execute(HttpHost target, AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        return this.doExecute(target, requestProducer, responseConsumer, pushHandlerFactory, context, callback);
    }

    @Override
    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        return this.doExecute(null, requestProducer, responseConsumer, pushHandlerFactory, context, callback);
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        return this.execute(requestProducer, responseConsumer, null, context, callback);
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        return this.execute(requestProducer, responseConsumer, HttpClientContext.create(), callback);
    }

    public final Future<SimpleHttpResponse> execute(SimpleHttpRequest request, HttpContext context, FutureCallback<SimpleHttpResponse> callback) {
        Args.notNull(request, "Request");
        return this.execute(SimpleRequestProducer.create(request), SimpleResponseConsumer.create(), context, callback);
    }

    public final Future<SimpleHttpResponse> execute(SimpleHttpRequest request, FutureCallback<SimpleHttpResponse> callback) {
        return this.execute(request, HttpClientContext.create(), callback);
    }

    public abstract void register(String var1, String var2, Supplier<AsyncPushConsumer> var3);

    public final void register(String uriPattern, Supplier<AsyncPushConsumer> supplier) {
        this.register(null, uriPattern, supplier);
    }
}

