/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.net.URI;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.message.BasicHttpRequest;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class DefaultHttpRequestFactory
implements HttpRequestFactory<HttpRequest> {
    public static final DefaultHttpRequestFactory INSTANCE = new DefaultHttpRequestFactory();

    @Override
    public HttpRequest newHttpRequest(String method, URI uri) {
        return new BasicHttpRequest(method, uri);
    }

    @Override
    public HttpRequest newHttpRequest(String method, String uri) {
        return new BasicHttpRequest(method, uri);
    }
}

