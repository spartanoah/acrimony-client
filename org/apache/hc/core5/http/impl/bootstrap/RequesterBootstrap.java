/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultAddressResolver;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.io.DefaultBHttpClientConnectionFactory;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.ssl.DefaultTlsSetupHandler;
import org.apache.hc.core5.http.io.ssl.SSLSessionVerifier;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.DefaultDisposalCallback;
import org.apache.hc.core5.pool.LaxConnPool;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.pool.StrictConnPool;
import org.apache.hc.core5.util.Timeout;

public class RequesterBootstrap {
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connReuseStrategy;
    private SocketConfig socketConfig;
    private HttpConnectionFactory<? extends HttpClientConnection> connectFactory;
    private SSLSocketFactory sslSocketFactory;
    private Callback<SSLParameters> sslSetupHandler;
    private SSLSessionVerifier sslSessionVerifier;
    private int defaultMaxPerRoute;
    private int maxTotal;
    private Timeout timeToLive;
    private PoolReusePolicy poolReusePolicy;
    private PoolConcurrencyPolicy poolConcurrencyPolicy;
    private Http1StreamListener streamListener;
    private ConnPoolListener<HttpHost> connPoolListener;

    private RequesterBootstrap() {
    }

    public static RequesterBootstrap bootstrap() {
        return new RequesterBootstrap();
    }

    public final RequesterBootstrap setHttpProcessor(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final RequesterBootstrap setConnectionReuseStrategy(ConnectionReuseStrategy connStrategy) {
        this.connReuseStrategy = connStrategy;
        return this;
    }

    public final RequesterBootstrap setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    public final RequesterBootstrap setConnectionFactory(HttpConnectionFactory<? extends HttpClientConnection> connectFactory) {
        this.connectFactory = connectFactory;
        return this;
    }

    public final RequesterBootstrap setSslContext(SSLContext sslContext) {
        this.sslSocketFactory = sslContext != null ? sslContext.getSocketFactory() : null;
        return this;
    }

    public final RequesterBootstrap setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public final RequesterBootstrap setSslSetupHandler(Callback<SSLParameters> sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    public final RequesterBootstrap setSslSessionVerifier(SSLSessionVerifier sslSessionVerifier) {
        this.sslSessionVerifier = sslSessionVerifier;
        return this;
    }

    public final RequesterBootstrap setDefaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public final RequesterBootstrap setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public final RequesterBootstrap setTimeToLive(Timeout timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public final RequesterBootstrap setPoolReusePolicy(PoolReusePolicy poolReusePolicy) {
        this.poolReusePolicy = poolReusePolicy;
        return this;
    }

    @Experimental
    public final RequesterBootstrap setPoolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
        this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        return this;
    }

    public final RequesterBootstrap setStreamListener(Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final RequesterBootstrap setConnPoolListener(ConnPoolListener<HttpHost> connPoolListener) {
        this.connPoolListener = connPoolListener;
        return this;
    }

    public HttpRequester create() {
        LaxConnPool<HttpHost, HttpClientConnection> connPool;
        HttpRequestExecutor requestExecutor = new HttpRequestExecutor(HttpRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE, this.connReuseStrategy != null ? this.connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE, this.streamListener);
        switch (this.poolConcurrencyPolicy != null ? this.poolConcurrencyPolicy : PoolConcurrencyPolicy.STRICT) {
            case LAX: {
                connPool = new LaxConnPool(this.defaultMaxPerRoute > 0 ? this.defaultMaxPerRoute : 20, this.timeToLive, this.poolReusePolicy, new DefaultDisposalCallback(), this.connPoolListener);
                break;
            }
            default: {
                connPool = new StrictConnPool(this.defaultMaxPerRoute > 0 ? this.defaultMaxPerRoute : 20, this.maxTotal > 0 ? this.maxTotal : 50, this.timeToLive, this.poolReusePolicy, new DefaultDisposalCallback(), this.connPoolListener);
            }
        }
        return new HttpRequester(requestExecutor, this.httpProcessor != null ? this.httpProcessor : HttpProcessors.client(), connPool, this.socketConfig != null ? this.socketConfig : SocketConfig.DEFAULT, this.connectFactory != null ? this.connectFactory : new DefaultBHttpClientConnectionFactory(Http1Config.DEFAULT, CharCodingConfig.DEFAULT), this.sslSocketFactory, this.sslSetupHandler != null ? this.sslSetupHandler : new DefaultTlsSetupHandler(), this.sslSessionVerifier, DefaultAddressResolver.INSTANCE);
    }
}

