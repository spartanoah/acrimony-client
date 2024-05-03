/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

public class BasicHttpRequest
extends HeaderGroup
implements HttpRequest {
    private static final long serialVersionUID = 1L;
    private final String method;
    private String path;
    private String scheme;
    private URIAuthority authority;
    private ProtocolVersion version;
    private URI requestUri;

    public BasicHttpRequest(String method, String path) {
        this.method = method;
        if (path != null) {
            try {
                this.setUri(new URI(path));
            } catch (URISyntaxException ex) {
                this.path = path;
            }
        }
    }

    public BasicHttpRequest(String method, HttpHost host, String path) {
        this.method = Args.notNull(method, "Method name");
        this.scheme = host != null ? host.getSchemeName() : null;
        this.authority = host != null ? new URIAuthority(host) : null;
        this.path = path;
    }

    public BasicHttpRequest(String method, URI requestUri) {
        this.method = Args.notNull(method, "Method name");
        this.setUri(Args.notNull(requestUri, "Request URI"));
    }

    public BasicHttpRequest(Method method, String path) {
        this.method = Args.notNull(method, "Method").name();
        if (path != null) {
            try {
                this.setUri(new URI(path));
            } catch (URISyntaxException ex) {
                this.path = path;
            }
        }
    }

    public BasicHttpRequest(Method method, HttpHost host, String path) {
        this.method = Args.notNull(method, "Method").name();
        this.scheme = host != null ? host.getSchemeName() : null;
        this.authority = host != null ? new URIAuthority(host) : null;
        this.path = path;
    }

    public BasicHttpRequest(Method method, URI requestUri) {
        this.method = Args.notNull(method, "Method").name();
        this.setUri(Args.notNull(requestUri, "Request URI"));
    }

    @Override
    public void addHeader(String name, Object value) {
        Args.notNull(name, "Header name");
        this.addHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(String name, Object value) {
        Args.notNull(name, "Header name");
        this.setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
        this.requestUri = null;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
        this.requestUri = null;
    }

    @Override
    public URIAuthority getAuthority() {
        return this.authority;
    }

    @Override
    public void setAuthority(URIAuthority authority) {
        this.authority = authority;
        this.requestUri = null;
    }

    @Override
    public String getRequestUri() {
        return this.getPath();
    }

    @Override
    public void setUri(URI requestUri) {
        this.scheme = requestUri.getScheme();
        this.authority = requestUri.getHost() != null ? new URIAuthority(requestUri.getRawUserInfo(), requestUri.getHost(), requestUri.getPort()) : null;
        StringBuilder buf = new StringBuilder();
        String rawPath = requestUri.getRawPath();
        if (!TextUtils.isBlank(rawPath)) {
            buf.append(rawPath);
        } else {
            buf.append("/");
        }
        String query = requestUri.getRawQuery();
        if (query != null) {
            buf.append('?').append(query);
        }
        this.path = buf.toString();
    }

    @Override
    public URI getUri() throws URISyntaxException {
        if (this.requestUri == null) {
            StringBuilder buf = new StringBuilder();
            if (this.authority != null) {
                buf.append(this.scheme != null ? this.scheme : URIScheme.HTTP.id).append("://");
                buf.append(this.authority.getHostName());
                if (this.authority.getPort() >= 0) {
                    buf.append(":").append(this.authority.getPort());
                }
            }
            if (this.path == null) {
                buf.append("/");
            } else {
                if (buf.length() > 0 && !this.path.startsWith("/")) {
                    buf.append("/");
                }
                buf.append(this.path);
            }
            this.requestUri = new URI(buf.toString());
        }
        return this.requestUri;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.method).append(" ").append(this.scheme).append("://").append(this.authority).append(this.path);
        return sb.toString();
    }
}

