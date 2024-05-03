/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultSchemePortResolver
implements SchemePortResolver {
    public static final DefaultSchemePortResolver INSTANCE = new DefaultSchemePortResolver();

    @Override
    public int resolve(HttpHost host) {
        Args.notNull(host, "HTTP host");
        int port = host.getPort();
        if (port > 0) {
            return port;
        }
        String name = host.getSchemeName();
        if (URIScheme.HTTP.same(name)) {
            return 80;
        }
        if (URIScheme.HTTPS.same(name)) {
            return 443;
        }
        return -1;
    }
}

