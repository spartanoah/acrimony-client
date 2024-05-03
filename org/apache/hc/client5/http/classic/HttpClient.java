/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface HttpClient {
    public HttpResponse execute(ClassicHttpRequest var1) throws IOException;

    public HttpResponse execute(ClassicHttpRequest var1, HttpContext var2) throws IOException;

    public ClassicHttpResponse execute(HttpHost var1, ClassicHttpRequest var2) throws IOException;

    public HttpResponse execute(HttpHost var1, ClassicHttpRequest var2, HttpContext var3) throws IOException;

    public <T> T execute(ClassicHttpRequest var1, HttpClientResponseHandler<? extends T> var2) throws IOException;

    public <T> T execute(ClassicHttpRequest var1, HttpContext var2, HttpClientResponseHandler<? extends T> var3) throws IOException;

    public <T> T execute(HttpHost var1, ClassicHttpRequest var2, HttpClientResponseHandler<? extends T> var3) throws IOException;

    public <T> T execute(HttpHost var1, ClassicHttpRequest var2, HttpContext var3, HttpClientResponseHandler<? extends T> var4) throws IOException;
}

