/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic.methods;

import java.net.URI;
import java.util.Locale;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.util.Args;

public final class ClassicHttpRequests {
    private static Method normalizedValueOf(String method) {
        return Method.valueOf(Args.notNull(method, "method").toUpperCase(Locale.ROOT));
    }

    public static HttpUriRequest create(Method method, String uri) {
        return ClassicHttpRequests.create(method, URI.create(uri));
    }

    public static HttpUriRequest create(Method method, URI uri) {
        switch (Args.notNull(method, "method")) {
            case DELETE: {
                return ClassicHttpRequests.delete(uri);
            }
            case GET: {
                return ClassicHttpRequests.get(uri);
            }
            case HEAD: {
                return ClassicHttpRequests.head(uri);
            }
            case OPTIONS: {
                return ClassicHttpRequests.options(uri);
            }
            case PATCH: {
                return ClassicHttpRequests.patch(uri);
            }
            case POST: {
                return ClassicHttpRequests.post(uri);
            }
            case PUT: {
                return ClassicHttpRequests.put(uri);
            }
            case TRACE: {
                return ClassicHttpRequests.trace(uri);
            }
        }
        throw new IllegalArgumentException(method.toString());
    }

    public static HttpUriRequest create(String method, String uri) {
        return ClassicHttpRequests.create(ClassicHttpRequests.normalizedValueOf(method), uri);
    }

    public static HttpUriRequest create(String method, URI uri) {
        return ClassicHttpRequests.create(ClassicHttpRequests.normalizedValueOf(method), uri);
    }

    public static HttpUriRequest delete(String uri) {
        return ClassicHttpRequests.delete(URI.create(uri));
    }

    public static HttpUriRequest delete(URI uri) {
        return new HttpDelete(uri);
    }

    public static HttpUriRequest get(String uri) {
        return ClassicHttpRequests.get(URI.create(uri));
    }

    public static HttpUriRequest get(URI uri) {
        return new HttpGet(uri);
    }

    public static HttpUriRequest head(String uri) {
        return ClassicHttpRequests.head(URI.create(uri));
    }

    public static HttpUriRequest head(URI uri) {
        return new HttpHead(uri);
    }

    public static HttpUriRequest options(String uri) {
        return ClassicHttpRequests.options(URI.create(uri));
    }

    public static HttpUriRequest options(URI uri) {
        return new HttpOptions(uri);
    }

    public static HttpUriRequest patch(String uri) {
        return ClassicHttpRequests.patch(URI.create(uri));
    }

    public static HttpUriRequest patch(URI uri) {
        return new HttpPatch(uri);
    }

    public static HttpUriRequest post(String uri) {
        return ClassicHttpRequests.post(URI.create(uri));
    }

    public static HttpUriRequest post(URI uri) {
        return new HttpPost(uri);
    }

    public static HttpUriRequest put(String uri) {
        return ClassicHttpRequests.put(URI.create(uri));
    }

    public static HttpUriRequest put(URI uri) {
        return new HttpPut(uri);
    }

    public static HttpUriRequest trace(String uri) {
        return ClassicHttpRequests.trace(URI.create(uri));
    }

    public static HttpUriRequest trace(URI uri) {
        return new HttpTrace(uri);
    }
}

