/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestParserFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseWriterFactory;
import org.apache.hc.core5.http.impl.nio.ServerHttp1StreamDuplexer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.NHttpMessageParserFactory;
import org.apache.hc.core5.http.nio.NHttpMessageWriterFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
@Internal
public final class ServerHttp1StreamDuplexerFactory {
    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final Http1Config http1Config;
    private final CharCodingConfig charCodingConfig;
    private final NHttpMessageParserFactory<HttpRequest> requestParserFactory;
    private final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final Http1StreamListener streamListener;

    public ServerHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParserFactory<HttpRequest> requestParserFactory, NHttpMessageWriterFactory<HttpResponse> responseWriterFactory, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, Http1StreamListener streamListener) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.exchangeHandlerFactory = Args.notNull(exchangeHandlerFactory, "Exchange handler factory");
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.connectionReuseStrategy = connectionReuseStrategy != null ? connectionReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.requestParserFactory = requestParserFactory != null ? requestParserFactory : new DefaultHttpRequestParserFactory(http1Config);
        this.responseWriterFactory = responseWriterFactory != null ? responseWriterFactory : DefaultHttpResponseWriterFactory.INSTANCE;
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public ServerHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParserFactory<HttpRequest> requestParserFactory, NHttpMessageWriterFactory<HttpResponse> responseWriterFactory, Http1StreamListener streamListener) {
        this(httpProcessor, exchangeHandlerFactory, http1Config, charCodingConfig, connectionReuseStrategy, requestParserFactory, responseWriterFactory, null, null, streamListener);
    }

    public ServerHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory, Http1Config http1Config, CharCodingConfig charCodingConfig, Http1StreamListener streamListener) {
        this(httpProcessor, exchangeHandlerFactory, http1Config, charCodingConfig, null, null, null, streamListener);
    }

    public ServerHttp1StreamDuplexer create(String scheme, ProtocolIOSession ioSession) {
        return new ServerHttp1StreamDuplexer(ioSession, this.httpProcessor, this.exchangeHandlerFactory, scheme, this.http1Config, this.charCodingConfig, this.connectionReuseStrategy, this.requestParserFactory.create(), this.responseWriterFactory.create(), this.incomingContentStrategy, this.outgoingContentStrategy, this.streamListener);
    }
}

