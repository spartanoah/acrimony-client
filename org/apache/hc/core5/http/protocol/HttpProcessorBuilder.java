/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.ChainBuilder;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessor;

public class HttpProcessorBuilder {
    private ChainBuilder<HttpRequestInterceptor> requestChainBuilder;
    private ChainBuilder<HttpResponseInterceptor> responseChainBuilder;

    public static HttpProcessorBuilder create() {
        return new HttpProcessorBuilder();
    }

    HttpProcessorBuilder() {
    }

    private ChainBuilder<HttpRequestInterceptor> getRequestChainBuilder() {
        if (this.requestChainBuilder == null) {
            this.requestChainBuilder = new ChainBuilder();
        }
        return this.requestChainBuilder;
    }

    private ChainBuilder<HttpResponseInterceptor> getResponseChainBuilder() {
        if (this.responseChainBuilder == null) {
            this.responseChainBuilder = new ChainBuilder();
        }
        return this.responseChainBuilder;
    }

    public HttpProcessorBuilder addFirst(HttpRequestInterceptor e) {
        if (e == null) {
            return this;
        }
        this.getRequestChainBuilder().addFirst(e);
        return this;
    }

    public HttpProcessorBuilder addLast(HttpRequestInterceptor e) {
        if (e == null) {
            return this;
        }
        this.getRequestChainBuilder().addLast(e);
        return this;
    }

    public HttpProcessorBuilder add(HttpRequestInterceptor e) {
        return this.addLast(e);
    }

    public HttpProcessorBuilder addAllFirst(HttpRequestInterceptor ... e) {
        if (e == null) {
            return this;
        }
        this.getRequestChainBuilder().addAllFirst((HttpRequestInterceptor[])e);
        return this;
    }

    public HttpProcessorBuilder addAllLast(HttpRequestInterceptor ... e) {
        if (e == null) {
            return this;
        }
        this.getRequestChainBuilder().addAllLast((HttpRequestInterceptor[])e);
        return this;
    }

    public HttpProcessorBuilder addAll(HttpRequestInterceptor ... e) {
        return this.addAllLast(e);
    }

    public HttpProcessorBuilder addFirst(HttpResponseInterceptor e) {
        if (e == null) {
            return this;
        }
        this.getResponseChainBuilder().addFirst(e);
        return this;
    }

    public HttpProcessorBuilder addLast(HttpResponseInterceptor e) {
        if (e == null) {
            return this;
        }
        this.getResponseChainBuilder().addLast(e);
        return this;
    }

    public HttpProcessorBuilder add(HttpResponseInterceptor e) {
        return this.addLast(e);
    }

    public HttpProcessorBuilder addAllFirst(HttpResponseInterceptor ... e) {
        if (e == null) {
            return this;
        }
        this.getResponseChainBuilder().addAllFirst((HttpResponseInterceptor[])e);
        return this;
    }

    public HttpProcessorBuilder addAllLast(HttpResponseInterceptor ... e) {
        if (e == null) {
            return this;
        }
        this.getResponseChainBuilder().addAllLast((HttpResponseInterceptor[])e);
        return this;
    }

    public HttpProcessorBuilder addAll(HttpResponseInterceptor ... e) {
        return this.addAllLast(e);
    }

    public HttpProcessor build() {
        return new DefaultHttpProcessor(this.requestChainBuilder != null ? this.requestChainBuilder.build() : null, this.responseChainBuilder != null ? this.responseChainBuilder.build() : null);
    }
}

