/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.util.Locale;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class AuthScope {
    private final String protocol;
    private final String host;
    private final int port;
    private final String realm;
    private final String schemeName;

    public AuthScope(String protocol, String host, int port, String realm, String schemeName) {
        this.protocol = protocol != null ? protocol.toLowerCase(Locale.ROOT) : null;
        this.host = host != null ? host.toLowerCase(Locale.ROOT) : null;
        this.port = port >= 0 ? port : -1;
        this.realm = realm;
        this.schemeName = schemeName != null ? schemeName : null;
    }

    public AuthScope(HttpHost origin, String realm, String schemeName) {
        Args.notNull(origin, "Host");
        this.protocol = origin.getSchemeName().toLowerCase(Locale.ROOT);
        this.host = origin.getHostName().toLowerCase(Locale.ROOT);
        this.port = origin.getPort() >= 0 ? origin.getPort() : -1;
        this.realm = realm;
        this.schemeName = schemeName != null ? schemeName : null;
    }

    public AuthScope(HttpHost origin) {
        this(origin, null, null);
    }

    public AuthScope(String host, int port) {
        this(null, host, port, null, null);
    }

    public AuthScope(AuthScope authScope) {
        Args.notNull(authScope, "Scope");
        this.protocol = authScope.getProtocol();
        this.host = authScope.getHost();
        this.port = authScope.getPort();
        this.realm = authScope.getRealm();
        this.schemeName = authScope.getSchemeName();
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getSchemeName() {
        return this.schemeName;
    }

    public int match(AuthScope that) {
        int factor = 0;
        if (LangUtils.equals(this.toNullSafeLowerCase(this.schemeName), this.toNullSafeLowerCase(that.schemeName))) {
            ++factor;
        } else if (this.schemeName != null && that.schemeName != null) {
            return -1;
        }
        if (LangUtils.equals(this.realm, that.realm)) {
            factor += 2;
        } else if (this.realm != null && that.realm != null) {
            return -1;
        }
        if (this.port == that.port) {
            factor += 4;
        } else if (this.port != -1 && that.port != -1) {
            return -1;
        }
        if (LangUtils.equals(this.protocol, that.protocol)) {
            factor += 8;
        } else if (this.protocol != null && that.protocol != null) {
            return -1;
        }
        if (LangUtils.equals(this.host, that.host)) {
            factor += 16;
        } else if (this.host != null && that.host != null) {
            return -1;
        }
        return factor;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AuthScope) {
            AuthScope that = (AuthScope)obj;
            return LangUtils.equals(this.protocol, that.protocol) && LangUtils.equals(this.host, that.host) && this.port == that.port && LangUtils.equals(this.realm, that.realm) && LangUtils.equals(this.toNullSafeLowerCase(this.schemeName), this.toNullSafeLowerCase(that.schemeName));
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.protocol);
        hash = LangUtils.hashCode(hash, this.host);
        hash = LangUtils.hashCode(hash, this.port);
        hash = LangUtils.hashCode(hash, this.realm);
        hash = LangUtils.hashCode(hash, this.toNullSafeLowerCase(this.schemeName));
        return hash;
    }

    private String toNullSafeLowerCase(String str) {
        return str != null ? str.toLowerCase(Locale.ROOT) : null;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (this.schemeName != null) {
            buffer.append(this.schemeName);
        } else {
            buffer.append("<any auth scheme>");
        }
        buffer.append(' ');
        if (this.realm != null) {
            buffer.append('\'');
            buffer.append(this.realm);
            buffer.append('\'');
        } else {
            buffer.append("<any realm>");
        }
        buffer.append(' ');
        if (this.protocol != null) {
            buffer.append(this.protocol);
        } else {
            buffer.append("<any protocol>");
        }
        buffer.append("://");
        if (this.host != null) {
            buffer.append(this.host);
        } else {
            buffer.append("<any host>");
        }
        buffer.append(':');
        if (this.port >= 0) {
            buffer.append(this.port);
        } else {
            buffer.append("<any port>");
        }
        return buffer.toString();
    }
}

