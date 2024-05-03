/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import org.apache.hc.client5.http.classic.ConnectionBackoffStrategy;
import org.apache.hc.core5.http.HttpResponse;

public class NullBackoffStrategy
implements ConnectionBackoffStrategy {
    @Override
    public boolean shouldBackoff(Throwable t) {
        return false;
    }

    @Override
    public boolean shouldBackoff(HttpResponse response) {
        return false;
    }
}

