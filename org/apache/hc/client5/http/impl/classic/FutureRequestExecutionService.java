/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.FutureRequestExecutionMetrics;
import org.apache.hc.client5.http.impl.classic.HttpRequestFutureTask;
import org.apache.hc.client5.http.impl.classic.HttpRequestTaskCallable;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class FutureRequestExecutionService
implements Closeable {
    private final HttpClient httpclient;
    private final ExecutorService executorService;
    private final FutureRequestExecutionMetrics metrics = new FutureRequestExecutionMetrics();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public FutureRequestExecutionService(HttpClient httpclient, ExecutorService executorService) {
        this.httpclient = httpclient;
        this.executorService = executorService;
    }

    public <T> FutureTask<T> execute(ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<T> HttpClientResponseHandler2) {
        return this.execute(request, context, HttpClientResponseHandler2, null);
    }

    public <T> FutureTask<T> execute(ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<T> HttpClientResponseHandler2, FutureCallback<T> callback) {
        if (this.closed.get()) {
            throw new IllegalStateException("Close has been called on this httpclient instance.");
        }
        this.metrics.getScheduledConnections().incrementAndGet();
        HttpRequestTaskCallable<T> callable = new HttpRequestTaskCallable<T>(this.httpclient, request, context, HttpClientResponseHandler2, callback, this.metrics);
        HttpRequestFutureTask<T> httpRequestFutureTask = new HttpRequestFutureTask<T>(request, callable);
        this.executorService.execute(httpRequestFutureTask);
        return httpRequestFutureTask;
    }

    public FutureRequestExecutionMetrics metrics() {
        return this.metrics;
    }

    @Override
    public void close() throws IOException {
        this.closed.set(true);
        this.executorService.shutdownNow();
        if (this.httpclient instanceof Closeable) {
            ((Closeable)((Object)this.httpclient)).close();
        }
    }
}

