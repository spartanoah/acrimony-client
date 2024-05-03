/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.nio.AbstractMessageParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpResponseParser<T extends HttpResponse>
extends AbstractMessageParser<T> {
    private final HttpResponseFactory<T> responseFactory;

    public DefaultHttpResponseParser(HttpResponseFactory<T> responseFactory, LineParser parser, Http1Config http1Config) {
        super(parser, http1Config);
        this.responseFactory = Args.notNull(responseFactory, "Response factory");
    }

    public DefaultHttpResponseParser(HttpResponseFactory<T> responseFactory, Http1Config http1Config) {
        this(responseFactory, null, http1Config);
    }

    public DefaultHttpResponseParser(HttpResponseFactory<T> responseFactory) {
        this(responseFactory, null);
    }

    @Override
    protected T createMessage(CharArrayBuffer buffer) throws HttpException {
        StatusLine statusLine = this.getLineParser().parseStatusLine(buffer);
        T response = this.responseFactory.newHttpResponse(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        response.setVersion(statusLine.getProtocolVersion());
        return response;
    }
}

