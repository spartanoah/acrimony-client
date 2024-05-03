/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.File;
import org.apache.hc.client5.http.impl.cache.CachingHttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public final class CachingHttpClients {
    private CachingHttpClients() {
    }

    public static CachingHttpClientBuilder custom() {
        return CachingHttpClientBuilder.create();
    }

    public static CloseableHttpClient createMemoryBound() {
        return CachingHttpClientBuilder.create().build();
    }

    public static CloseableHttpClient createFileBound(File cacheDir) {
        return CachingHttpClientBuilder.create().setCacheDir(cacheDir).build();
    }
}

