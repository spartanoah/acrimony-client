/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Args;

public final class HttpCacheSupport {
    private static final URI BASE_URI = URI.create("http://example.com/");

    public static String getRequestUri(HttpRequest request, HttpHost target) {
        Args.notNull(request, "HTTP request");
        Args.notNull(target, "Target");
        StringBuilder buf = new StringBuilder();
        URIAuthority authority = request.getAuthority();
        if (authority != null) {
            String scheme = request.getScheme();
            buf.append(scheme != null ? scheme : URIScheme.HTTP.id).append("://");
            buf.append(authority.getHostName());
            if (authority.getPort() >= 0) {
                buf.append(":").append(authority.getPort());
            }
        } else {
            buf.append(target.getSchemeName()).append("://");
            buf.append(target.getHostName());
            if (target.getPort() >= 0) {
                buf.append(":").append(target.getPort());
            }
        }
        String path = request.getPath();
        if (path == null) {
            buf.append("/");
        } else {
            if (buf.length() > 0 && !path.startsWith("/")) {
                buf.append("/");
            }
            buf.append(path);
        }
        return buf.toString();
    }

    public static URI normalize(URI requestUri) throws URISyntaxException {
        Args.notNull(requestUri, "URI");
        URIBuilder builder = new URIBuilder(requestUri.isAbsolute() ? URIUtils.resolve(BASE_URI, requestUri) : requestUri);
        if (builder.getHost() != null) {
            if (builder.getScheme() == null) {
                builder.setScheme(URIScheme.HTTP.id);
            }
            if (builder.getPort() <= -1) {
                if (URIScheme.HTTP.same(builder.getScheme())) {
                    builder.setPort(80);
                } else if (URIScheme.HTTPS.same(builder.getScheme())) {
                    builder.setPort(443);
                }
            }
        }
        builder.setFragment(null);
        if (builder.getPath() == null) {
            builder.setPath("/");
        }
        return builder.build();
    }

    public static URI normalizeQuetly(String requestUri) {
        if (requestUri == null) {
            return null;
        }
        try {
            return HttpCacheSupport.normalize(new URI(requestUri));
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}

