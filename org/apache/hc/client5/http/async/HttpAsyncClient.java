/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async;

import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface HttpAsyncClient {
    public <T> Future<T> execute(AsyncRequestProducer var1, AsyncResponseConsumer<T> var2, HandlerFactory<AsyncPushConsumer> var3, HttpContext var4, FutureCallback<T> var5);
}

