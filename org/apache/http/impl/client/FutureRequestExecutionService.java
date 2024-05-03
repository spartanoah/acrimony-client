/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.FutureRequestExecutionMetrics;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.client.HttpRequestTaskCallable;
import org.apache.http.protocol.HttpContext;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
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

    public <T> HttpRequestFutureTask<T> execute(HttpUriRequest request, HttpContext context, ResponseHandler<T> responseHandler) {
        return this.execute(request, context, responseHandler, null);
    }

    public <T> HttpRequestFutureTask<T> execute(HttpUriRequest request, HttpContext context, ResponseHandler<T> responseHandler, FutureCallback<T> callback) {
        if (this.closed.get()) {
            throw new IllegalStateException("Close has been called on this httpclient instance.");
        }
        this.metrics.getScheduledConnections().incrementAndGet();
        HttpRequestTaskCallable<T> callable = new HttpRequestTaskCallable<T>(this.httpclient, request, context, responseHandler, callback, this.metrics);
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

