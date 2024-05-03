/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

public interface HttpConnectionMetrics {
    public long getRequestCount();

    public long getResponseCount();

    public long getSentBytesCount();

    public long getReceivedBytesCount();
}

