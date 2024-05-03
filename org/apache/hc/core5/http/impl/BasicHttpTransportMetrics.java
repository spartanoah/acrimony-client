/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.hc.core5.http.io.HttpTransportMetrics;

public class BasicHttpTransportMetrics
implements HttpTransportMetrics {
    private final AtomicLong bytesTransferred = new AtomicLong(0L);

    @Override
    public long getBytesTransferred() {
        return this.bytesTransferred.get();
    }

    public void incrementBytesTransferred(long count) {
        this.bytesTransferred.addAndGet(count);
    }
}

