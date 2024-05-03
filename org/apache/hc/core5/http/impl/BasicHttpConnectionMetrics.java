/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.hc.core5.http.HttpConnectionMetrics;
import org.apache.hc.core5.http.io.HttpTransportMetrics;

public final class BasicHttpConnectionMetrics
implements HttpConnectionMetrics {
    private final HttpTransportMetrics inTransportMetric;
    private final HttpTransportMetrics outTransportMetric;
    private final AtomicLong requestCount;
    private final AtomicLong responseCount;

    public BasicHttpConnectionMetrics(HttpTransportMetrics inTransportMetric, HttpTransportMetrics outTransportMetric) {
        this.inTransportMetric = inTransportMetric;
        this.outTransportMetric = outTransportMetric;
        this.requestCount = new AtomicLong(0L);
        this.responseCount = new AtomicLong(0L);
    }

    @Override
    public long getReceivedBytesCount() {
        if (this.inTransportMetric != null) {
            return this.inTransportMetric.getBytesTransferred();
        }
        return -1L;
    }

    @Override
    public long getSentBytesCount() {
        if (this.outTransportMetric != null) {
            return this.outTransportMetric.getBytesTransferred();
        }
        return -1L;
    }

    @Override
    public long getRequestCount() {
        return this.requestCount.get();
    }

    public void incrementRequestCount() {
        this.requestCount.incrementAndGet();
    }

    @Override
    public long getResponseCount() {
        return this.responseCount.get();
    }

    public void incrementResponseCount() {
        this.responseCount.incrementAndGet();
    }
}

