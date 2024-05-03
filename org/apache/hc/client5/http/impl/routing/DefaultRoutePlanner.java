/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.routing;

import java.net.InetAddress;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultRoutePlanner
implements HttpRoutePlanner {
    private final SchemePortResolver schemePortResolver;

    public DefaultRoutePlanner(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
    }

    @Override
    public final HttpRoute determineRoute(HttpHost host, HttpContext context) throws HttpException {
        HttpHost target;
        if (host == null) {
            throw new ProtocolException("Target host is not specified");
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        RequestConfig config = clientContext.getRequestConfig();
        HttpHost proxy = config.getProxy();
        if (proxy == null) {
            proxy = this.determineProxy(host, context);
        }
        if ((target = RoutingSupport.normalize(host, this.schemePortResolver)).getPort() < 0) {
            throw new ProtocolException("Unroutable protocol scheme: " + target);
        }
        boolean secure = target.getSchemeName().equalsIgnoreCase("https");
        if (proxy == null) {
            return new HttpRoute(target, this.determineLocalAddress(target, context), secure);
        }
        return new HttpRoute(target, this.determineLocalAddress(proxy, context), proxy, secure);
    }

    protected HttpHost determineProxy(HttpHost target, HttpContext context) throws HttpException {
        return null;
    }

    protected InetAddress determineLocalAddress(HttpHost firstHop, HttpContext context) throws HttpException {
        return null;
    }
}

