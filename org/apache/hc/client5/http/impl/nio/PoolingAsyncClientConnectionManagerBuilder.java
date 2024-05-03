/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.nio;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.ConscryptClientTlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.ReflectionUtils;
import org.apache.hc.core5.util.TimeValue;

public class PoolingAsyncClientConnectionManagerBuilder {
    private TlsStrategy tlsStrategy;
    private SchemePortResolver schemePortResolver;
    private DnsResolver dnsResolver;
    private PoolConcurrencyPolicy poolConcurrencyPolicy;
    private PoolReusePolicy poolReusePolicy;
    private boolean systemProperties;
    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;
    private TimeValue timeToLive;
    private TimeValue validateAfterInactivity;

    public static PoolingAsyncClientConnectionManagerBuilder create() {
        return new PoolingAsyncClientConnectionManagerBuilder();
    }

    PoolingAsyncClientConnectionManagerBuilder() {
    }

    public final PoolingAsyncClientConnectionManagerBuilder setTlsStrategy(TlsStrategy tlsStrategy) {
        this.tlsStrategy = tlsStrategy;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setDnsResolver(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setPoolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
        this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setConnPoolPolicy(PoolReusePolicy poolReusePolicy) {
        this.poolReusePolicy = poolReusePolicy;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setConnectionTimeToLive(TimeValue timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder setValidateAfterInactivity(TimeValue validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
        return this;
    }

    public final PoolingAsyncClientConnectionManagerBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public PoolingAsyncClientConnectionManager build() {
        TlsStrategy tlsStrategyCopy = this.tlsStrategy != null ? this.tlsStrategy : (ReflectionUtils.determineJRELevel() <= 8 && ConscryptClientTlsStrategy.isSupported() ? (this.systemProperties ? ConscryptClientTlsStrategy.getSystemDefault() : ConscryptClientTlsStrategy.getDefault()) : (this.systemProperties ? DefaultClientTlsStrategy.getSystemDefault() : DefaultClientTlsStrategy.getDefault()));
        PoolingAsyncClientConnectionManager poolingmgr = new PoolingAsyncClientConnectionManager(RegistryBuilder.create().register("https", tlsStrategyCopy).build(), this.poolConcurrencyPolicy, this.poolReusePolicy, this.timeToLive, this.schemePortResolver, this.dnsResolver);
        poolingmgr.setValidateAfterInactivity(this.validateAfterInactivity);
        if (this.maxConnTotal > 0) {
            poolingmgr.setMaxTotal(this.maxConnTotal);
        }
        if (this.maxConnPerRoute > 0) {
            poolingmgr.setDefaultMaxPerRoute(this.maxConnPerRoute);
        }
        return poolingmgr;
    }
}

