/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.net.URI;
import java.util.Locale;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.util.Args;

public final class BasicHttpRequests {
    private static Method normalizedValueOf(String method) {
        return Method.valueOf(Args.notNull(method, "method").toUpperCase(Locale.ROOT));
    }

    public static BasicHttpRequest create(String method, String uri) {
        return BasicHttpRequests.create(BasicHttpRequests.normalizedValueOf(method), uri);
    }

    public static BasicHttpRequest create(String method, URI uri) {
        return BasicHttpRequests.create(BasicHttpRequests.normalizedValueOf(method), uri);
    }

    public static BasicHttpRequest delete(String uri) {
        return BasicHttpRequests.delete(URI.create(uri));
    }

    public static BasicHttpRequest delete(URI uri) {
        return BasicHttpRequests.create(Method.DELETE, uri);
    }

    public static BasicHttpRequest delete(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.DELETE, host, path);
    }

    public static BasicHttpRequest get(String uri) {
        return BasicHttpRequests.get(URI.create(uri));
    }

    public static BasicHttpRequest get(URI uri) {
        return BasicHttpRequests.create(Method.GET, uri);
    }

    public static BasicHttpRequest get(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.GET, host, path);
    }

    public static BasicHttpRequest head(String uri) {
        return BasicHttpRequests.head(URI.create(uri));
    }

    public static BasicHttpRequest head(URI uri) {
        return BasicHttpRequests.create(Method.HEAD, uri);
    }

    public static BasicHttpRequest head(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.HEAD, host, path);
    }

    public static BasicHttpRequest options(String uri) {
        return BasicHttpRequests.options(URI.create(uri));
    }

    public static BasicHttpRequest options(URI uri) {
        return BasicHttpRequests.create(Method.OPTIONS, uri);
    }

    public static BasicHttpRequest options(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.OPTIONS, host, path);
    }

    public static BasicHttpRequest patch(String uri) {
        return BasicHttpRequests.patch(URI.create(uri));
    }

    public static BasicHttpRequest patch(URI uri) {
        return BasicHttpRequests.create(Method.PATCH, uri);
    }

    public static BasicHttpRequest patch(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.PATCH, host, path);
    }

    public static BasicHttpRequest post(String uri) {
        return BasicHttpRequests.post(URI.create(uri));
    }

    public static BasicHttpRequest post(URI uri) {
        return BasicHttpRequests.create(Method.POST, uri);
    }

    public static BasicHttpRequest post(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.POST, host, path);
    }

    public static BasicHttpRequest put(String uri) {
        return BasicHttpRequests.put(URI.create(uri));
    }

    public static BasicHttpRequest put(URI uri) {
        return BasicHttpRequests.create(Method.PUT, uri);
    }

    public static BasicHttpRequest put(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.PUT, host, path);
    }

    public static BasicHttpRequest trace(String uri) {
        return BasicHttpRequests.trace(URI.create(uri));
    }

    public static BasicHttpRequest trace(URI uri) {
        return BasicHttpRequests.create(Method.TRACE, uri);
    }

    public static BasicHttpRequest trace(HttpHost host, String path) {
        return BasicHttpRequests.create(Method.TRACE, host, path);
    }

    public static BasicHttpRequest create(Method method, String uri) {
        return BasicHttpRequests.create(method, URI.create(uri));
    }

    public static BasicHttpRequest create(Method method, URI uri) {
        return new BasicHttpRequest(method, uri);
    }

    public static BasicHttpRequest create(Method method, HttpHost host, String path) {
        return new BasicHttpRequest(method, host, path);
    }
}

