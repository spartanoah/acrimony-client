/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.net.InetSocketAddress;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;

public final class DefaultAddressResolver
implements Resolver<HttpHost, InetSocketAddress> {
    public static final DefaultAddressResolver INSTANCE = new DefaultAddressResolver();

    @Override
    public InetSocketAddress resolve(HttpHost host) {
        if (host == null) {
            return null;
        }
        int port = host.getPort();
        if (port < 0) {
            String scheme = host.getSchemeName();
            if (URIScheme.HTTP.same(scheme)) {
                port = 80;
            } else if (URIScheme.HTTPS.same(scheme)) {
                port = 443;
            }
        }
        return new InetSocketAddress(host.getHostName(), port);
    }
}

