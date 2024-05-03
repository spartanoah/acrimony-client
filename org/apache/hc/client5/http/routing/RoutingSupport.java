/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.routing;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.net.URIAuthority;

public final class RoutingSupport {
    public static HttpHost determineHost(HttpRequest request) throws HttpException {
        if (request == null) {
            return null;
        }
        URIAuthority authority = request.getAuthority();
        if (authority != null) {
            String scheme = request.getScheme();
            if (scheme == null) {
                throw new ProtocolException("Protocol scheme is not specified");
            }
            return new HttpHost(scheme, authority);
        }
        try {
            URI requestURI = request.getUri();
            if (requestURI.isAbsolute()) {
                HttpHost httpHost = URIUtils.extractHost(requestURI);
                if (httpHost == null) {
                    throw new ProtocolException("URI does not specify a valid host name: " + requestURI);
                }
                return httpHost;
            }
        } catch (URISyntaxException uRISyntaxException) {
            // empty catch block
        }
        return null;
    }

    public static HttpHost normalize(HttpHost host, SchemePortResolver schemePortResolver) {
        int port;
        if (host == null) {
            return null;
        }
        if (host.getPort() < 0 && (port = (schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE).resolve(host)) > 0) {
            return new HttpHost(host.getSchemeName(), host.getHostName(), port);
        }
        return host;
    }
}

