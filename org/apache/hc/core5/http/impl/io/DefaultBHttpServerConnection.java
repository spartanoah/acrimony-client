/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.io.BHttpConnectionBase;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestParserFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseWriterFactory;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.io.HttpMessageWriter;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.util.Args;

public class DefaultBHttpServerConnection
extends BHttpConnectionBase
implements HttpServerConnection {
    private final String scheme;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final HttpMessageParser<ClassicHttpRequest> requestParser;
    private final HttpMessageWriter<ClassicHttpResponse> responseWriter;

    public DefaultBHttpServerConnection(String scheme, Http1Config http1Config, CharsetDecoder charDecoder, CharsetEncoder charEncoder, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory, HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory) {
        super(http1Config, charDecoder, charEncoder);
        this.scheme = scheme;
        this.requestParser = (requestParserFactory != null ? requestParserFactory : DefaultHttpRequestParserFactory.INSTANCE).create(http1Config);
        this.responseWriter = (responseWriterFactory != null ? responseWriterFactory : DefaultHttpResponseWriterFactory.INSTANCE).create();
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
    }

    public DefaultBHttpServerConnection(String scheme, Http1Config http1Config, CharsetDecoder charDecoder, CharsetEncoder charEncoder) {
        this(scheme, http1Config, charDecoder, charEncoder, null, null, null, null);
    }

    public DefaultBHttpServerConnection(String scheme, Http1Config http1Config) {
        this(scheme, http1Config, null, null);
    }

    protected void onRequestReceived(ClassicHttpRequest request) {
    }

    protected void onResponseSubmitted(ClassicHttpResponse response) {
    }

    @Override
    public void bind(Socket socket) throws IOException {
        super.bind(socket);
    }

    @Override
    public ClassicHttpRequest receiveRequestHeader() throws HttpException, IOException {
        SocketHolder socketHolder = this.ensureOpen();
        ClassicHttpRequest request = this.requestParser.parse(this.inBuffer, socketHolder.getInputStream());
        ProtocolVersion transportVersion = request.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
            throw new UnsupportedHttpVersionException(transportVersion);
        }
        request.setScheme(this.scheme);
        this.version = transportVersion;
        this.onRequestReceived(request);
        this.incrementRequestCount();
        return request;
    }

    @Override
    public void receiveRequestEntity(ClassicHttpRequest request) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        SocketHolder socketHolder = this.ensureOpen();
        long len = this.incomingContentStrategy.determineLength(request);
        if (len == -9223372036854775807L) {
            return;
        }
        request.setEntity(this.createIncomingEntity(request, this.inBuffer, socketHolder.getInputStream(), len));
    }

    @Override
    public void sendResponseHeader(ClassicHttpResponse response) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        SocketHolder socketHolder = this.ensureOpen();
        this.responseWriter.write(response, this.outbuffer, socketHolder.getOutputStream());
        this.onResponseSubmitted(response);
        if (response.getCode() >= 200) {
            this.incrementResponseCount();
        }
    }

    @Override
    public void sendResponseEntity(ClassicHttpResponse response) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        SocketHolder socketHolder = this.ensureOpen();
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return;
        }
        long len = this.outgoingContentStrategy.determineLength(response);
        try (OutputStream outStream = this.createContentOutputStream(len, this.outbuffer, socketHolder.getOutputStream(), entity.getTrailers());){
            entity.writeTo(outStream);
        }
    }
}

