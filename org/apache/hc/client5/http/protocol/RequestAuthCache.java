/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class RequestAuthCache
implements HttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAuthCache.class);

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        AuthScheme authScheme;
        AuthExchange proxyAuthExchange;
        HttpHost proxy;
        AuthScheme authScheme2;
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        AuthCache authCache = clientContext.getAuthCache();
        if (authCache == null) {
            LOG.debug("Auth cache not set in the context");
            return;
        }
        CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
        if (credsProvider == null) {
            LOG.debug("Credentials provider not set in the context");
            return;
        }
        RouteInfo route = clientContext.getHttpRoute();
        if (route == null) {
            LOG.debug("Route info not set in the context");
            return;
        }
        URIAuthority authority = request.getAuthority();
        HttpHost target = authority != null ? new HttpHost(request.getScheme(), authority.getHostName(), authority.getPort() >= 0 ? authority.getPort() : route.getTargetHost().getPort()) : route.getTargetHost();
        AuthExchange targetAuthExchange = clientContext.getAuthExchange(target);
        if (targetAuthExchange.getState() == AuthExchange.State.UNCHALLENGED && (authScheme2 = authCache.get(target)) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Re-using cached '{}' auth scheme for {}", (Object)authScheme2.getName(), (Object)target);
            }
            targetAuthExchange.select(authScheme2);
        }
        if ((proxy = route.getProxyHost()) != null && (proxyAuthExchange = clientContext.getAuthExchange(proxy)).getState() == AuthExchange.State.UNCHALLENGED && (authScheme = authCache.get(proxy)) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Re-using cached '{}' auth scheme for {}", (Object)authScheme.getName(), (Object)proxy);
            }
            proxyAuthExchange.select(authScheme);
        }
    }
}

