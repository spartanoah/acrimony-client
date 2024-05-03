/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.routing;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class SystemDefaultRoutePlanner
extends DefaultRoutePlanner {
    private final ProxySelector proxySelector;

    public SystemDefaultRoutePlanner(SchemePortResolver schemePortResolver, ProxySelector proxySelector) {
        super(schemePortResolver);
        this.proxySelector = proxySelector;
    }

    public SystemDefaultRoutePlanner(ProxySelector proxySelector) {
        this(null, proxySelector);
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpContext context) throws HttpException {
        URI targetURI;
        try {
            targetURI = new URI(target.toURI());
        } catch (URISyntaxException ex) {
            throw new HttpException("Cannot convert host to URI: " + target, ex);
        }
        ProxySelector proxySelectorInstance = this.proxySelector;
        if (proxySelectorInstance == null) {
            proxySelectorInstance = ProxySelector.getDefault();
        }
        if (proxySelectorInstance == null) {
            return null;
        }
        List<Proxy> proxies = proxySelectorInstance.select(targetURI);
        Proxy p = this.chooseProxy(proxies);
        HttpHost result = null;
        if (p.type() == Proxy.Type.HTTP) {
            if (!(p.address() instanceof InetSocketAddress)) {
                throw new HttpException("Unable to handle non-Inet proxy address: " + p.address());
            }
            InetSocketAddress isa = (InetSocketAddress)p.address();
            result = new HttpHost(null, isa.getAddress(), isa.getHostString(), isa.getPort());
        }
        return result;
    }

    private Proxy chooseProxy(List<Proxy> proxies) {
        Proxy result = null;
        block3: for (int i = 0; result == null && i < proxies.size(); ++i) {
            Proxy p = proxies.get(i);
            switch (p.type()) {
                case DIRECT: 
                case HTTP: {
                    result = p;
                    continue block3;
                }
            }
        }
        if (result == null) {
            result = Proxy.NO_PROXY;
        }
        return result;
    }
}

