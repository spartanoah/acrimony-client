/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicResponseProducer
implements AsyncResponseProducer {
    private final HttpResponse response;
    private final AsyncEntityProducer dataProducer;

    public BasicResponseProducer(HttpResponse response, AsyncEntityProducer dataProducer) {
        this.response = Args.notNull(response, "Response");
        this.dataProducer = dataProducer;
    }

    public BasicResponseProducer(HttpResponse response) {
        this.response = Args.notNull(response, "Response");
        this.dataProducer = null;
    }

    public BasicResponseProducer(int code, AsyncEntityProducer dataProducer) {
        this((HttpResponse)new BasicHttpResponse(code), dataProducer);
    }

    public BasicResponseProducer(HttpResponse response, String message, ContentType contentType) {
        this(response, AsyncEntityProducers.create(message, contentType));
    }

    public BasicResponseProducer(HttpResponse response, String message) {
        this(response, message, ContentType.TEXT_PLAIN);
    }

    public BasicResponseProducer(int code, String message, ContentType contentType) {
        this(new BasicHttpResponse(code), message, contentType);
    }

    public BasicResponseProducer(int code, String message) {
        this((HttpResponse)new BasicHttpResponse(code), message);
    }

    public BasicResponseProducer(AsyncEntityProducer dataProducer) {
        this(200, dataProducer);
    }

    @Override
    public void sendResponse(ResponseChannel responseChannel, HttpContext httpContext) throws HttpException, IOException {
        responseChannel.sendResponse(this.response, this.dataProducer, httpContext);
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
    public void failed(Exception cause) {
        if (this.dataProducer != null) {
            this.dataProducer.failed(cause);
        }
        this.releaseResources();
    }

    @Override
    public void releaseResources() {
        if (this.dataProducer != null) {
            this.dataProducer.releaseResources();
        }
    }
}

