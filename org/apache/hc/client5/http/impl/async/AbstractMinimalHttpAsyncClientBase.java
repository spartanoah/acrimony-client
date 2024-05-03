/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.client5.http.impl.async.AbstractHttpAsyncClientBase;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.support.BasicClientExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;

abstract class AbstractMinimalHttpAsyncClientBase
extends AbstractHttpAsyncClientBase {
    AbstractMinimalHttpAsyncClientBase(DefaultConnectingIOReactor ioReactor, AsyncPushConsumerRegistry pushConsumerRegistry, ThreadFactory threadFactory) {
        super(ioReactor, pushConsumerRegistry, threadFactory);
    }

    @Override
    protected <T> Future<T> doExecute(HttpHost httpHost, AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context, FutureCallback<T> callback) {
        final ComplexFuture<T> future = new ComplexFuture<T>(callback);
        future.setDependency(this.execute(new BasicClientExchangeHandler<T>(requestProducer, responseConsumer, new FutureCallback<T>(){

            @Override
            public void completed(T result) {
                future.completed(result);
            }

            @Override
            public void failed(Exception ex) {
                future.failed(ex);
            }

            @Override
            public void cancelled() {
                future.cancel();
            }
        }), pushHandlerFactory, context));
        return future;
    }

    public final Cancellable execute(AsyncClientExchangeHandler exchangeHandler) {
        return this.execute(exchangeHandler, null, HttpClientContext.create());
    }

    public abstract Cancellable execute(AsyncClientExchangeHandler var1, HandlerFactory<AsyncPushConsumer> var2, HttpContext var3);
}

