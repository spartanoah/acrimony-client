/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic;

import org.apache.hc.core5.http.HttpResponse;

public interface ConnectionBackoffStrategy {
    public boolean shouldBackoff(Throwable var1);

    public boolean shouldBackoff(HttpResponse var1);
}

