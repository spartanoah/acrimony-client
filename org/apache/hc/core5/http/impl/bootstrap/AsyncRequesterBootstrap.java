/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.impl.nio.ClientHttp1IOEventHandlerFactory;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.nio.ssl.BasicClientTlsStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.DefaultDisposalCallback;
import org.apache.hc.core5.pool.LaxConnPool;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.pool.StrictConnPool;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.util.Timeout;

public class AsyncRequesterBootstrap {
    private IOReactorConfig ioReactorConfig;
    private Http1Config http1Config;
    private CharCodingConfig charCodingConfig;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private int defaultMaxPerRoute;
    private int maxTotal;
    private Timeout timeToLive;
    private PoolReusePolicy poolReusePolicy;
    private PoolConcurrencyPolicy poolConcurrencyPolicy;
    private TlsStrategy tlsStrategy;
    private Timeout handshakeTimeout;
    private Decorator<IOSession> ioSessionDecorator;
    private Callback<Exception> exceptionCallback;
    private IOSessionListener sessionListener;
    private Http1StreamListener streamListener;
    private ConnPoolListener<HttpHost> connPoolListener;

    private AsyncRequesterBootstrap() {
    }

    public static AsyncRequesterBootstrap bootstrap() {
        return new AsyncRequesterBootstrap();
    }

    public final AsyncRequesterBootstrap setIOReactorConfig(IOReactorConfig ioReactorConfig) {
        this.ioReactorConfig = ioReactorConfig;
        return this;
    }

    public final AsyncRequesterBootstrap setHttp1Config(Http1Config http1Config) {
        this.http1Config = http1Config;
        return this;
    }

    public final AsyncRequesterBootstrap setCharCodingConfig(CharCodingConfig charCodingConfig) {
        this.charCodingConfig = charCodingConfig;
        return this;
    }

    public final AsyncRequesterBootstrap setHttpProcessor(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final AsyncRequesterBootstrap setConnectionReuseStrategy(ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final AsyncRequesterBootstrap setDefaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public final AsyncRequesterBootstrap setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public final AsyncRequesterBootstrap setTimeToLive(Timeout timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public final AsyncRequesterBootstrap setPoolReusePolicy(PoolReusePolicy poolReusePolicy) {
        this.poolReusePolicy = poolReusePolicy;
        return this;
    }

    @Experimental
    public final AsyncRequesterBootstrap setPoolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
        this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        return this;
    }

    public final AsyncRequesterBootstrap setTlsStrategy(TlsStrategy tlsStrategy) {
        this.tlsStrategy = tlsStrategy;
        return this;
    }

    public final AsyncRequesterBootstrap setTlsHandshakeTimeout(Timeout handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
        return this;
    }

    public final AsyncRequesterBootstrap setIOSessionDecorator(Decorator<IOSession> ioSessionDecorator) {
        this.ioSessionDecorator = ioSessionDecorator;
        return this;
    }

    public final AsyncRequesterBootstrap setExceptionCallback(Callback<Exception> exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
        return this;
    }

    public final AsyncRequesterBootstrap setIOSessionListener(IOSessionListener sessionListener) {
        this.sessionListener = sessionListener;
        return this;
    }

    public final AsyncRequesterBootstrap setStreamListener(Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final AsyncRequesterBootstrap setConnPoolListener(ConnPoolListener<HttpHost> connPoolListener) {
        this.connPoolListener = connPoolListener;
        return this;
    }

    public HttpAsyncRequester create() {
        LaxConnPool<HttpHost, IOSession> connPool;
        switch (this.poolConcurrencyPolicy != null ? this.poolConcurrencyPolicy : PoolConcurrencyPolicy.STRICT) {
            case LAX: {
                connPool = new LaxConnPool(this.defaultMaxPerRoute > 0 ? this.defaultMaxPerRoute : 20, this.timeToLive, this.poolReusePolicy, new DefaultDisposalCallback(), this.connPoolListener);
                break;
            }
            default: {
                connPool = new StrictConnPool(this.defaultMaxPerRoute > 0 ? this.defaultMaxPerRoute : 20, this.maxTotal > 0 ? this.maxTotal : 50, this.timeToLive, this.poolReusePolicy, new DefaultDisposalCallback(), this.connPoolListener);
            }
        }
        ClientHttp1StreamDuplexerFactory streamDuplexerFactory = new ClientHttp1StreamDuplexerFactory(this.httpProcessor != null ? this.httpProcessor : HttpProcessors.client(), this.http1Config != null ? this.http1Config : Http1Config.DEFAULT, this.charCodingConfig != null ? this.charCodingConfig : CharCodingConfig.DEFAULT, this.connStrategy, null, null, this.streamListener);
        ClientHttp1IOEventHandlerFactory ioEventHandlerFactory = new ClientHttp1IOEventHandlerFactory(streamDuplexerFactory, this.tlsStrategy != null ? this.tlsStrategy : new BasicClientTlsStrategy(), this.handshakeTimeout);
        return new HttpAsyncRequester(this.ioReactorConfig, ioEventHandlerFactory, this.ioSessionDecorator, this.exceptionCallback, this.sessionListener, connPool);
    }
}

