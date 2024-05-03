/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.config;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class RequestConfig
implements Cloneable {
    private static final Timeout DEFAULT_CONNECTION_REQUEST_TIMEOUT = Timeout.ofMinutes(3L);
    private static final Timeout DEFAULT_CONNECT_TIMEOUT = Timeout.ofMinutes(3L);
    private static final TimeValue DEFAULT_CONN_KEEP_ALIVE = TimeValue.ofMinutes(3L);
    public static final RequestConfig DEFAULT = new Builder().build();
    private final boolean expectContinueEnabled;
    private final HttpHost proxy;
    private final String cookieSpec;
    private final boolean redirectsEnabled;
    private final boolean circularRedirectsAllowed;
    private final int maxRedirects;
    private final boolean authenticationEnabled;
    private final Collection<String> targetPreferredAuthSchemes;
    private final Collection<String> proxyPreferredAuthSchemes;
    private final Timeout connectionRequestTimeout;
    private final Timeout connectTimeout;
    private final Timeout responseTimeout;
    private final TimeValue connectionKeepAlive;
    private final boolean contentCompressionEnabled;
    private final boolean hardCancellationEnabled;

    protected RequestConfig() {
        this(false, null, null, false, false, 0, false, null, null, DEFAULT_CONNECTION_REQUEST_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, null, DEFAULT_CONN_KEEP_ALIVE, false, false);
    }

    RequestConfig(boolean expectContinueEnabled, HttpHost proxy, String cookieSpec, boolean redirectsEnabled, boolean circularRedirectsAllowed, int maxRedirects, boolean authenticationEnabled, Collection<String> targetPreferredAuthSchemes, Collection<String> proxyPreferredAuthSchemes, Timeout connectionRequestTimeout, Timeout connectTimeout, Timeout responseTimeout, TimeValue connectionKeepAlive, boolean contentCompressionEnabled, boolean hardCancellationEnabled) {
        this.expectContinueEnabled = expectContinueEnabled;
        this.proxy = proxy;
        this.cookieSpec = cookieSpec;
        this.redirectsEnabled = redirectsEnabled;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        this.maxRedirects = maxRedirects;
        this.authenticationEnabled = authenticationEnabled;
        this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
        this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.responseTimeout = responseTimeout;
        this.connectionKeepAlive = connectionKeepAlive;
        this.contentCompressionEnabled = contentCompressionEnabled;
        this.hardCancellationEnabled = hardCancellationEnabled;
    }

    public boolean isExpectContinueEnabled() {
        return this.expectContinueEnabled;
    }

    public HttpHost getProxy() {
        return this.proxy;
    }

    public String getCookieSpec() {
        return this.cookieSpec;
    }

    public boolean isRedirectsEnabled() {
        return this.redirectsEnabled;
    }

    public boolean isCircularRedirectsAllowed() {
        return this.circularRedirectsAllowed;
    }

    public int getMaxRedirects() {
        return this.maxRedirects;
    }

    public boolean isAuthenticationEnabled() {
        return this.authenticationEnabled;
    }

    public Collection<String> getTargetPreferredAuthSchemes() {
        return this.targetPreferredAuthSchemes;
    }

    public Collection<String> getProxyPreferredAuthSchemes() {
        return this.proxyPreferredAuthSchemes;
    }

    public Timeout getConnectionRequestTimeout() {
        return this.connectionRequestTimeout;
    }

    public Timeout getConnectTimeout() {
        return this.connectTimeout;
    }

    public Timeout getResponseTimeout() {
        return this.responseTimeout;
    }

    public TimeValue getConnectionKeepAlive() {
        return this.connectionKeepAlive;
    }

    public boolean isContentCompressionEnabled() {
        return this.contentCompressionEnabled;
    }

    public boolean isHardCancellationEnabled() {
        return this.hardCancellationEnabled;
    }

    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig)super.clone();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("expectContinueEnabled=").append(this.expectContinueEnabled);
        builder.append(", proxy=").append(this.proxy);
        builder.append(", cookieSpec=").append(this.cookieSpec);
        builder.append(", redirectsEnabled=").append(this.redirectsEnabled);
        builder.append(", maxRedirects=").append(this.maxRedirects);
        builder.append(", circularRedirectsAllowed=").append(this.circularRedirectsAllowed);
        builder.append(", authenticationEnabled=").append(this.authenticationEnabled);
        builder.append(", targetPreferredAuthSchemes=").append(this.targetPreferredAuthSchemes);
        builder.append(", proxyPreferredAuthSchemes=").append(this.proxyPreferredAuthSchemes);
        builder.append(", connectionRequestTimeout=").append(this.connectionRequestTimeout);
        builder.append(", connectTimeout=").append(this.connectTimeout);
        builder.append(", responseTimeout=").append(this.responseTimeout);
        builder.append(", connectionKeepAlive=").append(this.connectionKeepAlive);
        builder.append(", contentCompressionEnabled=").append(this.contentCompressionEnabled);
        builder.append(", hardCancellationEnabled=").append(this.hardCancellationEnabled);
        builder.append("]");
        return builder.toString();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(RequestConfig config) {
        return new Builder().setExpectContinueEnabled(config.isExpectContinueEnabled()).setProxy(config.getProxy()).setCookieSpec(config.getCookieSpec()).setRedirectsEnabled(config.isRedirectsEnabled()).setCircularRedirectsAllowed(config.isCircularRedirectsAllowed()).setMaxRedirects(config.getMaxRedirects()).setAuthenticationEnabled(config.isAuthenticationEnabled()).setTargetPreferredAuthSchemes(config.getTargetPreferredAuthSchemes()).setProxyPreferredAuthSchemes(config.getProxyPreferredAuthSchemes()).setConnectionRequestTimeout(config.getConnectionRequestTimeout()).setConnectTimeout(config.getConnectTimeout()).setResponseTimeout(config.getResponseTimeout()).setConnectionKeepAlive(config.getConnectionKeepAlive()).setContentCompressionEnabled(config.isContentCompressionEnabled()).setHardCancellationEnabled(config.isHardCancellationEnabled());
    }

    public static class Builder {
        private boolean expectContinueEnabled;
        private HttpHost proxy;
        private String cookieSpec;
        private boolean redirectsEnabled = true;
        private boolean circularRedirectsAllowed;
        private int maxRedirects = 50;
        private boolean authenticationEnabled = true;
        private Collection<String> targetPreferredAuthSchemes;
        private Collection<String> proxyPreferredAuthSchemes;
        private Timeout connectionRequestTimeout = RequestConfig.access$000();
        private Timeout connectTimeout = RequestConfig.access$100();
        private Timeout responseTimeout;
        private TimeValue connectionKeepAlive;
        private boolean contentCompressionEnabled = true;
        private boolean hardCancellationEnabled = true;

        Builder() {
        }

        public Builder setExpectContinueEnabled(boolean expectContinueEnabled) {
            this.expectContinueEnabled = expectContinueEnabled;
            return this;
        }

        public Builder setProxy(HttpHost proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder setCookieSpec(String cookieSpec) {
            this.cookieSpec = cookieSpec;
            return this;
        }

        public Builder setRedirectsEnabled(boolean redirectsEnabled) {
            this.redirectsEnabled = redirectsEnabled;
            return this;
        }

        public Builder setCircularRedirectsAllowed(boolean circularRedirectsAllowed) {
            this.circularRedirectsAllowed = circularRedirectsAllowed;
            return this;
        }

        public Builder setMaxRedirects(int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }

        public Builder setAuthenticationEnabled(boolean authenticationEnabled) {
            this.authenticationEnabled = authenticationEnabled;
            return this;
        }

        public Builder setTargetPreferredAuthSchemes(Collection<String> targetPreferredAuthSchemes) {
            this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
            return this;
        }

        public Builder setProxyPreferredAuthSchemes(Collection<String> proxyPreferredAuthSchemes) {
            this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
            return this;
        }

        public Builder setConnectionRequestTimeout(Timeout connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setConnectionRequestTimeout(long connectionRequestTimeout, TimeUnit timeUnit) {
            this.connectionRequestTimeout = Timeout.of(connectionRequestTimeout, timeUnit);
            return this;
        }

        public Builder setConnectTimeout(Timeout connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setConnectTimeout(long connectTimeout, TimeUnit timeUnit) {
            this.connectTimeout = Timeout.of(connectTimeout, timeUnit);
            return this;
        }

        public Builder setResponseTimeout(Timeout responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public Builder setResponseTimeout(long responseTimeout, TimeUnit timeUnit) {
            this.responseTimeout = Timeout.of(responseTimeout, timeUnit);
            return this;
        }

        public Builder setConnectionKeepAlive(TimeValue connectionKeepAlive) {
            this.connectionKeepAlive = connectionKeepAlive;
            return this;
        }

        public Builder setDefaultKeepAlive(long defaultKeepAlive, TimeUnit timeUnit) {
            this.connectionKeepAlive = TimeValue.of(defaultKeepAlive, timeUnit);
            return this;
        }

        public Builder setContentCompressionEnabled(boolean contentCompressionEnabled) {
            this.contentCompressionEnabled = contentCompressionEnabled;
            return this;
        }

        public Builder setHardCancellationEnabled(boolean hardCancellationEnabled) {
            this.hardCancellationEnabled = hardCancellationEnabled;
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(this.expectContinueEnabled, this.proxy, this.cookieSpec, this.redirectsEnabled, this.circularRedirectsAllowed, this.maxRedirects, this.authenticationEnabled, this.targetPreferredAuthSchemes, this.proxyPreferredAuthSchemes, this.connectionRequestTimeout != null ? this.connectionRequestTimeout : DEFAULT_CONNECTION_REQUEST_TIMEOUT, this.connectTimeout != null ? this.connectTimeout : DEFAULT_CONNECT_TIMEOUT, this.responseTimeout, this.connectionKeepAlive != null ? this.connectionKeepAlive : DEFAULT_CONN_KEEP_ALIVE, this.contentCompressionEnabled, this.hardCancellationEnabled);
        }
    }
}

