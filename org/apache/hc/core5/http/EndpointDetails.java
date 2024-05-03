/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.net.SocketAddress;
import org.apache.hc.core5.http.HttpConnectionMetrics;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.Timeout;

public abstract class EndpointDetails
implements HttpConnectionMetrics {
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final Timeout socketTimeout;

    protected EndpointDetails(SocketAddress remoteAddress, SocketAddress localAddress, Timeout socketTimeout) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.socketTimeout = socketTimeout;
    }

    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override
    public abstract long getRequestCount();

    @Override
    public abstract long getResponseCount();

    @Override
    public abstract long getSentBytesCount();

    @Override
    public abstract long getReceivedBytesCount();

    public Timeout getSocketTimeout() {
        return this.socketTimeout;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(90);
        InetAddressUtils.formatAddress(buffer, this.localAddress);
        buffer.append("<->");
        InetAddressUtils.formatAddress(buffer, this.remoteAddress);
        return buffer.toString();
    }
}

