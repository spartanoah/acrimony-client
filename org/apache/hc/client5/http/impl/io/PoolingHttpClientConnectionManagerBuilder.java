/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;

public class PoolingHttpClientConnectionManagerBuilder {
    private HttpConnectionFactory<ManagedHttpClientConnection> connectionFactory;
    private LayeredConnectionSocketFactory sslSocketFactory;
    private SchemePortResolver schemePortResolver;
    private DnsResolver dnsResolver;
    private PoolConcurrencyPolicy poolConcurrencyPolicy;
    private PoolReusePolicy poolReusePolicy;
    private SocketConfig defaultSocketConfig;
    private boolean systemProperties;
    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;
    private TimeValue timeToLive;
    private TimeValue validateAfterInactivity;

    public static PoolingHttpClientConnectionManagerBuilder create() {
        return new PoolingHttpClientConnectionManagerBuilder();
    }

    PoolingHttpClientConnectionManagerBuilder() {
    }

    public final PoolingHttpClientConnectionManagerBuilder setConnectionFactory(HttpConnectionFactory<ManagedHttpClientConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setSSLSocketFactory(LayeredConnectionSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setDnsResolver(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setPoolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
        this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setConnPoolPolicy(PoolReusePolicy poolReusePolicy) {
        this.poolReusePolicy = poolReusePolicy;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setDefaultSocketConfig(SocketConfig config) {
        this.defaultSocketConfig = config;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setConnectionTimeToLive(TimeValue timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder setValidateAfterInactivity(TimeValue validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
        return this;
    }

    public final PoolingHttpClientConnectionManagerBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public PoolingHttpClientConnectionManager build() {
        PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(RegistryBuilder.create().register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory()).register(URIScheme.HTTPS.id, (PlainConnectionSocketFactory)((Object)(this.sslSocketFactory != null ? this.sslSocketFactory : (this.systemProperties ? SSLConnectionSocketFactory.getSystemSocketFactory() : SSLConnectionSocketFactory.getSocketFactory())))).build(), this.poolConcurrencyPolicy, this.poolReusePolicy, this.timeToLive != null ? this.timeToLive : TimeValue.NEG_ONE_MILLISECOND, this.schemePortResolver, this.dnsResolver, this.connectionFactory);
        poolingmgr.setValidateAfterInactivity(this.validateAfterInactivity);
        if (this.defaultSocketConfig != null) {
            poolingmgr.setDefaultSocketConfig(this.defaultSocketConfig);
        }
        if (this.maxConnTotal > 0) {
            poolingmgr.setMaxTotal(this.maxConnTotal);
        }
        if (this.maxConnPerRoute > 0) {
            poolingmgr.setDefaultMaxPerRoute(this.maxConnPerRoute);
        }
        return poolingmgr;
    }
}

