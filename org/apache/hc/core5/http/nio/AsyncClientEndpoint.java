/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.util.concurrent.Future;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.support.BasicClientExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class AsyncClientEndpoint {
    public abstract void execute(AsyncClientExchangeHandler var1, HandlerFactory<AsyncPushConsumer> var2, HttpContext var3);

    public void execute(AsyncClientExchangeHandler exchangeHandler, HttpContext context) {
        this.execute(exchangeHandler, null, context);
    }

    public abstract void releaseAndReuse();

    public abstract void releaseAndDiscard();

    public abstract boolean isConnected();

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context, FutureCallback<T> callback) {
        final BasicFuture<T> future = new BasicFuture<T>(callback);
        this.execute(new BasicClientExchangeHandler<T>(requestProducer, responseConsumer, new FutureCallback<T>(){

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
        }), pushHandlerFactory, context != null ? context : HttpCoreContext.create());
        return future;
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, null, context, callback);
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, null, null, callback);
    }
}

