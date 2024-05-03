/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.net.URI;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class DefaultClassicHttpRequestFactory
implements HttpRequestFactory<ClassicHttpRequest> {
    public static final DefaultClassicHttpRequestFactory INSTANCE = new DefaultClassicHttpRequestFactory();

    @Override
    public ClassicHttpRequest newHttpRequest(String method, URI uri) {
        return new BasicClassicHttpRequest(method, uri);
    }

    @Override
    public ClassicHttpRequest newHttpRequest(String method, String uri) {
        return new BasicClassicHttpRequest(method, uri);
    }
}

