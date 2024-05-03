/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.RequestHeaderFieldsTooLargeException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.nio.AbstractMessageParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpRequestParser<T extends HttpRequest>
extends AbstractMessageParser<T> {
    private final HttpRequestFactory<T> requestFactory;

    public DefaultHttpRequestParser(HttpRequestFactory<T> requestFactory, LineParser parser, Http1Config http1Config) {
        super(parser, http1Config);
        this.requestFactory = Args.notNull(requestFactory, "Request factory");
    }

    public DefaultHttpRequestParser(HttpRequestFactory<T> requestFactory, Http1Config http1Config) {
        this(requestFactory, null, http1Config);
    }

    public DefaultHttpRequestParser(HttpRequestFactory<T> requestFactory) {
        this(requestFactory, null);
    }

    @Override
    public T parse(SessionInputBuffer sessionBuffer, boolean endOfStream) throws IOException, HttpException {
        try {
            return (T)((HttpRequest)super.parse(sessionBuffer, endOfStream));
        } catch (MessageConstraintException ex) {
            throw new RequestHeaderFieldsTooLargeException(ex.getMessage(), ex);
        }
    }

    @Override
    protected T createMessage(CharArrayBuffer buffer) throws HttpException {
        RequestLine requestLine = this.getLineParser().parseRequestLine(buffer);
        T request = this.requestFactory.newHttpRequest(requestLine.getMethod(), requestLine.getUri());
        request.setVersion(requestLine.getProtocolVersion());
        return request;
    }
}

