/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.net.URI;
import java.util.Locale;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.util.Args;

public final class SimpleHttpRequests {
    private static Method normalizedValueOf(String method) {
        return Method.valueOf(Args.notNull(method, "method").toUpperCase(Locale.ROOT));
    }

    public static SimpleHttpRequest create(String method, String uri) {
        return SimpleHttpRequests.create(SimpleHttpRequests.normalizedValueOf(method), uri);
    }

    public static SimpleHttpRequest create(String method, URI uri) {
        return SimpleHttpRequests.create(SimpleHttpRequests.normalizedValueOf(method), uri);
    }

    public static SimpleHttpRequest delete(String uri) {
        return SimpleHttpRequests.delete(URI.create(uri));
    }

    public static SimpleHttpRequest delete(URI uri) {
        return SimpleHttpRequests.create(Method.DELETE, uri);
    }

    public static SimpleHttpRequest delete(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.DELETE, host, path);
    }

    public static SimpleHttpRequest get(String uri) {
        return SimpleHttpRequests.get(URI.create(uri));
    }

    public static SimpleHttpRequest get(URI uri) {
        return SimpleHttpRequests.create(Method.GET, uri);
    }

    public static SimpleHttpRequest get(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.GET, host, path);
    }

    public static SimpleHttpRequest head(String uri) {
        return SimpleHttpRequests.head(URI.create(uri));
    }

    public static SimpleHttpRequest head(URI uri) {
        return SimpleHttpRequests.create(Method.HEAD, uri);
    }

    public static SimpleHttpRequest head(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.HEAD, host, path);
    }

    public static SimpleHttpRequest options(String uri) {
        return SimpleHttpRequests.options(URI.create(uri));
    }

    public static SimpleHttpRequest options(URI uri) {
        return SimpleHttpRequests.create(Method.OPTIONS, uri);
    }

    public static SimpleHttpRequest options(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.OPTIONS, host, path);
    }

    public static SimpleHttpRequest patch(String uri) {
        return SimpleHttpRequests.patch(URI.create(uri));
    }

    public static SimpleHttpRequest patch(URI uri) {
        return SimpleHttpRequests.create(Method.PATCH, uri);
    }

    public static SimpleHttpRequest patch(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.PATCH, host, path);
    }

    public static SimpleHttpRequest post(String uri) {
        return SimpleHttpRequests.post(URI.create(uri));
    }

    public static SimpleHttpRequest post(URI uri) {
        return SimpleHttpRequests.create(Method.POST, uri);
    }

    public static SimpleHttpRequest post(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.POST, host, path);
    }

    public static SimpleHttpRequest put(String uri) {
        return SimpleHttpRequests.put(URI.create(uri));
    }

    public static SimpleHttpRequest put(URI uri) {
        return SimpleHttpRequests.create(Method.PUT, uri);
    }

    public static SimpleHttpRequest put(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.PUT, host, path);
    }

    public static SimpleHttpRequest trace(String uri) {
        return SimpleHttpRequests.trace(URI.create(uri));
    }

    public static SimpleHttpRequest trace(URI uri) {
        return SimpleHttpRequests.create(Method.TRACE, uri);
    }

    public static SimpleHttpRequest trace(HttpHost host, String path) {
        return SimpleHttpRequests.create(Method.TRACE, host, path);
    }

    public static SimpleHttpRequest create(Method method, String uri) {
        return SimpleHttpRequests.create(method, URI.create(uri));
    }

    public static SimpleHttpRequest create(Method method, URI uri) {
        return new SimpleHttpRequest(method, uri);
    }

    public static SimpleHttpRequest create(Method method, HttpHost host, String path) {
        return new SimpleHttpRequest(method, host, path);
    }
}

