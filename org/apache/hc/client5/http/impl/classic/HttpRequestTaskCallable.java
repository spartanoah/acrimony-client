/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.FutureRequestExecutionMetrics;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

class HttpRequestTaskCallable<V>
implements Callable<V> {
    private final ClassicHttpRequest request;
    private final HttpClient httpclient;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final long scheduled = System.currentTimeMillis();
    private long started = -1L;
    private long ended = -1L;
    private final HttpContext context;
    private final HttpClientResponseHandler<V> responseHandler;
    private final FutureCallback<V> callback;
    private final FutureRequestExecutionMetrics metrics;

    HttpRequestTaskCallable(HttpClient httpClient, ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<V> responseHandler, FutureCallback<V> callback, FutureRequestExecutionMetrics metrics) {
        this.httpclient = httpClient;
        this.responseHandler = responseHandler;
        this.request = request;
        this.context = context;
        this.callback = callback;
        this.metrics = metrics;
    }

    public long getScheduled() {
        return this.scheduled;
    }

    public long getStarted() {
        return this.started;
    }

    public long getEnded() {
        return this.ended;
    }

    @Override
    public V call() throws Exception {
        if (!this.cancelled.get()) {
            try {
                V v;
                this.metrics.getActiveConnections().incrementAndGet();
                this.started = System.currentTimeMillis();
                try {
                    this.metrics.getScheduledConnections().decrementAndGet();
                    V result = this.httpclient.execute(this.request, this.context, this.responseHandler);
                    this.ended = System.currentTimeMillis();
                    this.metrics.getSuccessfulConnections().increment(this.started);
                    if (this.callback != null) {
                        this.callback.completed(result);
                    }
                    v = result;
                } catch (Exception e) {
                    this.metrics.getFailedConnections().increment(this.started);
                    this.ended = System.currentTimeMillis();
                    if (this.callback != null) {
                        this.callback.failed(e);
                    }
                    throw e;
                }
                return v;
            } finally {
                this.metrics.getRequests().increment(this.started);
                this.metrics.getTasks().increment(this.started);
                this.metrics.getActiveConnections().decrementAndGet();
            }
        }
        throw new CancellationException();
    }

    public void cancel() {
        this.cancelled.set(true);
        if (this.callback != null) {
            this.callback.cancelled();
        }
    }
}

