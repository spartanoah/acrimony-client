/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.File;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.cache.CachingH2AsyncClientBuilder;
import org.apache.hc.client5.http.impl.cache.CachingHttpAsyncClientBuilder;

public final class CachingHttpAsyncClients {
    private CachingHttpAsyncClients() {
    }

    public static CachingHttpAsyncClientBuilder custom() {
        return CachingHttpAsyncClientBuilder.create();
    }

    public static CloseableHttpAsyncClient createMemoryBound() {
        return CachingHttpAsyncClientBuilder.create().build();
    }

    public static CloseableHttpAsyncClient createFileBound(File cacheDir) {
        return CachingHttpAsyncClientBuilder.create().setCacheDir(cacheDir).build();
    }

    public static CachingH2AsyncClientBuilder customHttp2() {
        return CachingH2AsyncClientBuilder.create();
    }

    public static CloseableHttpAsyncClient createHttp2MemoryBound() {
        return CachingH2AsyncClientBuilder.create().build();
    }

    public static CloseableHttpAsyncClient createHttp2FileBound(File cacheDir) {
        return CachingH2AsyncClientBuilder.create().setCacheDir(cacheDir).build();
    }
}

