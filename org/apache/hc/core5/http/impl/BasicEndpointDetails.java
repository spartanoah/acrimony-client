/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.net.SocketAddress;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpConnectionMetrics;
import org.apache.hc.core5.util.Timeout;

public final class BasicEndpointDetails
extends EndpointDetails {
    private final HttpConnectionMetrics metrics;

    public BasicEndpointDetails(SocketAddress remoteAddress, SocketAddress localAddress, HttpConnectionMetrics metrics, Timeout socketTimeout) {
        super(remoteAddress, localAddress, socketTimeout);
        this.metrics = metrics;
    }

    @Override
    public long getRequestCount() {
        return this.metrics != null ? this.metrics.getRequestCount() : 0L;
    }

    @Override
    public long getResponseCount() {
        return this.metrics != null ? this.metrics.getResponseCount() : 0L;
    }

    @Override
    public long getSentBytesCount() {
        return this.metrics != null ? this.metrics.getSentBytesCount() : 0L;
    }

    @Override
    public long getReceivedBytesCount() {
        return this.metrics != null ? this.metrics.getReceivedBytesCount() : 0L;
    }
}

