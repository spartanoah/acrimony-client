/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicPushProducer
implements AsyncPushProducer {
    private final HttpResponse response;
    private final AsyncEntityProducer dataProducer;

    public BasicPushProducer(HttpResponse response, AsyncEntityProducer dataProducer) {
        this.response = Args.notNull(response, "Response");
        this.dataProducer = Args.notNull(dataProducer, "Entity producer");
    }

    public BasicPushProducer(int code, AsyncEntityProducer dataProducer) {
        this(new BasicHttpResponse(code), dataProducer);
    }

    public BasicPushProducer(AsyncEntityProducer dataProducer) {
        this(200, dataProducer);
    }

    @Override
    public void produceResponse(ResponseChannel channel, HttpContext httpContext) throws HttpException, IOException {
        channel.sendResponse(this.response, this.dataProducer, httpContext);
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
        this.releaseResources();
    }

    @Override
    public void releaseResources() {
        if (this.dataProducer != null) {
            this.dataProducer.releaseResources();
        }
    }
}

