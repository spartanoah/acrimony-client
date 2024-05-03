/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.MinimalHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;

public final class HttpClients {
    private HttpClients() {
    }

    public static HttpClientBuilder custom() {
        return HttpClientBuilder.create();
    }

    public static CloseableHttpClient createDefault() {
        return HttpClientBuilder.create().build();
    }

    public static CloseableHttpClient createSystem() {
        return HttpClientBuilder.create().useSystemProperties().build();
    }

    public static MinimalHttpClient createMinimal() {
        return new MinimalHttpClient(new PoolingHttpClientConnectionManager());
    }

    public static MinimalHttpClient createMinimal(HttpClientConnectionManager connManager) {
        return new MinimalHttpClient(connManager);
    }
}

