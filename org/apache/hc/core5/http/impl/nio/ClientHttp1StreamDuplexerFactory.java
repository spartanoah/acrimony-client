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
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexer;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseParserFactory;
import org.apache.hc.core5.http.nio.NHttpMessageParserFactory;
import org.apache.hc.core5.http.nio.NHttpMessageWriterFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
@Internal
public final class ClientHttp1StreamDuplexerFactory {
    private final HttpProcessor httpProcessor;
    private final Http1Config http1Config;
    private final CharCodingConfig charCodingConfig;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final NHttpMessageParserFactory<HttpResponse> responseParserFactory;
    private final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final Http1StreamListener streamListener;

    public ClientHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParserFactory<HttpResponse> responseParserFactory, NHttpMessageWriterFactory<HttpRequest> requestWriterFactory, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, Http1StreamListener streamListener) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.connectionReuseStrategy = connectionReuseStrategy != null ? connectionReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.responseParserFactory = responseParserFactory != null ? responseParserFactory : new DefaultHttpResponseParserFactory(http1Config);
        this.requestWriterFactory = requestWriterFactory != null ? requestWriterFactory : DefaultHttpRequestWriterFactory.INSTANCE;
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public ClientHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParserFactory<HttpResponse> responseParserFactory, NHttpMessageWriterFactory<HttpRequest> requestWriterFactory, Http1StreamListener streamListener) {
        this(httpProcessor, http1Config, charCodingConfig, connectionReuseStrategy, responseParserFactory, requestWriterFactory, null, null, streamListener);
    }

    public ClientHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, Http1Config http1Config, CharCodingConfig charCodingConfig, Http1StreamListener streamListener) {
        this(httpProcessor, http1Config, charCodingConfig, null, null, null, streamListener);
    }

    public ClientHttp1StreamDuplexerFactory(HttpProcessor httpProcessor, Http1Config http1Config, CharCodingConfig charCodingConfig) {
        this(httpProcessor, http1Config, charCodingConfig, null);
    }

    public ClientHttp1StreamDuplexer create(ProtocolIOSession ioSession) {
        return new ClientHttp1StreamDuplexer(ioSession, this.httpProcessor, this.http1Config, this.charCodingConfig, this.connectionReuseStrategy, this.responseParserFactory.create(), this.requestWriterFactory.create(), this.incomingContentStrategy, this.outgoingContentStrategy, this.streamListener);
    }
}

