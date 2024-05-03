/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.net.Ports;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class HttpHost
implements NamedEndpoint,
Serializable {
    private static final long serialVersionUID = -7529410654042457626L;
    public static final URIScheme DEFAULT_SCHEME = URIScheme.HTTP;
    private final String hostname;
    private final String lcHostname;
    private final int port;
    private final String schemeName;
    private final InetAddress address;

    public HttpHost(String scheme, InetAddress address, String hostname, int port) {
        this.hostname = Args.containsNoBlanks(hostname, "Host name");
        this.port = Ports.checkWithDefault(port);
        this.lcHostname = hostname.toLowerCase(Locale.ROOT);
        this.schemeName = scheme != null ? scheme.toLowerCase(Locale.ROOT) : HttpHost.DEFAULT_SCHEME.id;
        this.address = address;
    }

    public HttpHost(String scheme, String hostname, int port) {
        this(scheme, null, hostname, port);
    }

    public HttpHost(String hostname, int port) {
        this(null, hostname, port);
    }

    public HttpHost(String scheme, String hostname) {
        this(scheme, hostname, -1);
    }

    public static HttpHost create(String s) throws URISyntaxException {
        Args.notEmpty(s, "HTTP Host");
        String text = s;
        String scheme = null;
        int schemeIdx = text.indexOf("://");
        if (schemeIdx > 0) {
            scheme = text.substring(0, schemeIdx);
            if (TextUtils.containsBlanks(scheme)) {
                throw new URISyntaxException(s, "scheme contains blanks");
            }
            text = text.substring(schemeIdx + 3);
        }
        int port = -1;
        int portIdx = text.lastIndexOf(":");
        if (portIdx > 0) {
            try {
                port = Integer.parseInt(text.substring(portIdx + 1));
            } catch (NumberFormatException ex) {
                throw new URISyntaxException(s, "invalid port");
            }
            text = text.substring(0, portIdx);
        }
        if (TextUtils.containsBlanks(text)) {
            throw new URISyntaxException(s, "hostname contains blanks");
        }
        return new HttpHost(scheme, null, text, port);
    }

    public static HttpHost create(URI uri) {
        String scheme = uri.getScheme();
        return new HttpHost(scheme != null ? scheme : URIScheme.HTTP.getId(), uri.getHost(), uri.getPort());
    }

    public HttpHost(String hostname) {
        this(null, hostname, -1);
    }

    public HttpHost(String scheme, InetAddress address, int port) {
        this(scheme, Args.notNull(address, "Inet address"), address.getHostName(), port);
    }

    public HttpHost(InetAddress address, int port) {
        this(null, address, port);
    }

    public HttpHost(InetAddress address) {
        this(null, address, -1);
    }

    public HttpHost(String scheme, NamedEndpoint namedEndpoint) {
        this(scheme, Args.notNull(namedEndpoint, "Named endpoint").getHostName(), namedEndpoint.getPort());
    }

    public HttpHost(URIAuthority authority) {
        this(null, authority);
    }

    @Override
    public String getHostName() {
        return this.hostname;
    }

    @Override
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
            return this.lcHostname.equals(that.lcHostname) && this.port == that.port && this.schemeName.equals(that.schemeName) && LangUtils.equals(this.address, that.address);
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.lcHostname);
        hash = LangUtils.hashCode(hash, this.port);
        hash = LangUtils.hashCode(hash, this.schemeName);
        hash = LangUtils.hashCode(hash, this.address);
        return hash;
    }
}

