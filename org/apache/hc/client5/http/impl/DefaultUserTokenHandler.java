/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.security.Principal;
import javax.net.ssl.SSLSession;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultUserTokenHandler
implements UserTokenHandler {
    public static final DefaultUserTokenHandler INSTANCE = new DefaultUserTokenHandler();

    @Override
    public Object getUserToken(HttpRoute route, HttpContext context) {
        SSLSession sslSession;
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Principal userPrincipal = null;
        AuthExchange targetAuthExchnage = clientContext.getAuthExchange(route.getTargetHost());
        if (targetAuthExchnage != null && (userPrincipal = DefaultUserTokenHandler.getAuthPrincipal(targetAuthExchnage)) == null && route.getProxyHost() != null) {
            AuthExchange proxyAuthExchange = clientContext.getAuthExchange(route.getProxyHost());
            userPrincipal = DefaultUserTokenHandler.getAuthPrincipal(proxyAuthExchange);
        }
        if (userPrincipal == null && (sslSession = clientContext.getSSLSession()) != null) {
            userPrincipal = sslSession.getLocalPrincipal();
        }
        return userPrincipal;
    }

    private static Principal getAuthPrincipal(AuthExchange authExchange) {
        AuthScheme scheme = authExchange.getAuthScheme();
        if (scheme != null && scheme.isConnectionBased()) {
            return scheme.getPrincipal();
        }
        return null;
    }
}

