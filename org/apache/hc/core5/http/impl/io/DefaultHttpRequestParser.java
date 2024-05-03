/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.RequestHeaderFieldsTooLargeException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpRequestFactory;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpRequestParser
extends AbstractMessageParser<ClassicHttpRequest> {
    private final HttpRequestFactory<ClassicHttpRequest> requestFactory;

    public DefaultHttpRequestParser(LineParser lineParser, HttpRequestFactory<ClassicHttpRequest> requestFactory, Http1Config http1Config) {
        super(lineParser, http1Config);
        this.requestFactory = requestFactory != null ? requestFactory : DefaultClassicHttpRequestFactory.INSTANCE;
    }

    public DefaultHttpRequestParser(Http1Config http1Config) {
        this(null, null, http1Config);
    }

    public DefaultHttpRequestParser() {
        this(Http1Config.DEFAULT);
    }

    @Override
    protected IOException createConnectionClosedException() {
        return new ConnectionClosedException("Client closed connection");
    }

    @Override
    public ClassicHttpRequest parse(SessionInputBuffer buffer, InputStream inputStream) throws IOException, HttpException {
        try {
            return (ClassicHttpRequest)super.parse(buffer, inputStream);
        } catch (MessageConstraintException ex) {
            throw new RequestHeaderFieldsTooLargeException(ex.getMessage(), ex);
        }
    }

    @Override
    protected ClassicHttpRequest createMessage(CharArrayBuffer buffer) throws IOException, HttpException {
        RequestLine requestLine = this.getLineParser().parseRequestLine(buffer);
        ClassicHttpRequest request = this.requestFactory.newHttpRequest(requestLine.getMethod(), requestLine.getUri());
        request.setVersion(requestLine.getProtocolVersion());
        return request;
    }
}

