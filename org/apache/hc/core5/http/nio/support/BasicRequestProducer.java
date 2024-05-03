/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.net.URI;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public class BasicRequestProducer
implements AsyncRequestProducer {
    private final HttpRequest request;
    private final AsyncEntityProducer dataProducer;

    public BasicRequestProducer(HttpRequest request, AsyncEntityProducer dataProducer) {
        this.request = request;
        this.dataProducer = dataProducer;
    }

    public BasicRequestProducer(String method, HttpHost host, String path, AsyncEntityProducer dataProducer) {
        this(new BasicHttpRequest(method, host, path), dataProducer);
    }

    public BasicRequestProducer(String method, HttpHost host, String path) {
        this(method, host, path, null);
    }

    public BasicRequestProducer(String method, URI requestUri, AsyncEntityProducer dataProducer) {
        this(new BasicHttpRequest(method, requestUri), dataProducer);
    }

    public BasicRequestProducer(String method, URI requestUri) {
        this(method, requestUri, null);
    }

    public BasicRequestProducer(Method method, HttpHost host, String path, AsyncEntityProducer dataProducer) {
        this(new BasicHttpRequest(method, host, path), dataProducer);
    }

    public BasicRequestProducer(Method method, HttpHost host, String path) {
        this(method, host, path, null);
    }

    public BasicRequestProducer(Method method, URI requestUri, AsyncEntityProducer dataProducer) {
        this(new BasicHttpRequest(method, requestUri), dataProducer);
    }

    public BasicRequestProducer(Method method, URI requestUri) {
        this(method, requestUri, null);
    }

    @Override
    public void sendRequest(RequestChannel requestChannel, HttpContext httpContext) throws HttpException, IOException {
        requestChannel.sendRequest(this.request, this.dataProducer, httpContext);
    }

    @Override
    public int available() {
        return this.dataProducer != null ? this.dataProducer.available() : 0;
    }

    @Override
    public void produce(DataStreamChannel channel) throws IOException {
        if (this.dataProducer != null) {
            this.dataProducer.produce(channel);
        }
    }

    @Override
    public boolean isRepeatable() {
        return this.dataProducer == null || this.dataProducer.isRepeatable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void failed(Exception cause) {
        try {
            if (this.dataProducer != null) {
                this.dataProducer.failed(cause);
            }
        } finally {
            this.releaseResources();
        }
    }

    @Override
    public void releaseResources() {
        if (this.dataProducer != null) {
            this.dataProducer.releaseResources();
        }
    }
}

