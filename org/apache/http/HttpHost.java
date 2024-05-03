/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Locale;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.Args;
import org.apache.http.util.LangUtils;

@Immutable
public final class HttpHost
implements Cloneable,
Serializable {
    private static final long serialVersionUID = -7529410654042457626L;
    public static final String DEFAULT_SCHEME_NAME = "http";
    protected final String hostname;
    protected final String lcHostname;
    protected final int port;
    protected final String schemeName;
    protected final InetAddress address;

    public HttpHost(String hostname, int port, String scheme) {
        this.hostname = Args.notBlank(hostname, "Host name");
        this.lcHostname = hostname.toLowerCase(Locale.ENGLISH);
        this.schemeName = scheme != null ? scheme.toLowerCase(Locale.ENGLISH) : DEFAULT_SCHEME_NAME;
        this.port = port;
        this.address = null;
    }

    public HttpHost(String hostname, int port) {
        this(hostname, port, null);
    }

    public HttpHost(String hostname) {
        this(hostname, -1, null);
    }

    public HttpHost(InetAddress address, int port, String scheme) {
        this.address = Args.notNull(address, "Inet address");
        this.hostname = address.getHostAddress();
        this.lcHostname = this.hostname.toLowerCase(Locale.ENGLISH);
        this.schemeName = scheme != null ? scheme.toLowerCase(Locale.ENGLISH) : DEFAULT_SCHEME_NAME;
        this.port = port;
    }

    public HttpHost(InetAddress address, int port) {
        this(address, port, null);
    }

    public HttpHost(InetAddress address) {
        this(address, -1, null);
    }

    public HttpHost(HttpHost httphost) {
        Args.notNull(httphost, "HTTP host");
        this.hostname = httphost.hostname;
        this.lcHostname = httphost.lcHostname;
        this.schemeName = httphost.schemeName;
        this.port = httphost.port;
        this.address = httphost.address;
    }

    public String getHostName() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public String getSchemeName() {
        return this.schemeName;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public String toURI() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.schemeName);
        buffer.append("://");
        buffer.append(this.hostname);
        if (this.port != -1) {
            buffer.append(':');
            buffer.append(Integer.toString(this.port));
        }
        return buffer.toString();
    }

    public String toHostString() {
        if (this.port != -1) {
            StringBuilder buffer = new StringBuilder(this.hostname.length() + 6);
            buffer.append(this.hostname);
            buffer.append(":");
            buffer.append(Integer.toString(this.port));
            return buffer.toString();
        }
        return this.hostname;
    }

    public String toString() {
        return this.toURI();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HttpHost) {
            HttpHost that = (HttpHost)obj;
            return this.lcHostname.equals(that.lcHostname) && this.port == that.port && this.schemeName.equals(that.schemeName);
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.lcHostname);
        hash = LangUtils.hashCode(hash, this.port);
        hash = LangUtils.hashCode(hash, this.schemeName);
        return hash;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

