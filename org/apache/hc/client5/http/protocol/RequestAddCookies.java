/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class RequestAddCookies
implements HttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAddCookies.class);

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        int port;
        String hostName;
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        String method = request.getMethod();
        if (method.equalsIgnoreCase("CONNECT") || method.equalsIgnoreCase("TRACE")) {
            return;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        CookieStore cookieStore = clientContext.getCookieStore();
        if (cookieStore == null) {
            LOG.debug("Cookie store not specified in HTTP context");
            return;
        }
        Lookup<CookieSpecFactory> registry = clientContext.getCookieSpecRegistry();
        if (registry == null) {
            LOG.debug("CookieSpec registry not specified in HTTP context");
            return;
        }
        RouteInfo route = clientContext.getHttpRoute();
        if (route == null) {
            LOG.debug("Connection route not set in the context");
            return;
        }
        RequestConfig config = clientContext.getRequestConfig();
        String cookieSpecName = config.getCookieSpec();
        if (cookieSpecName == null) {
            cookieSpecName = "strict";
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cookie spec selected: {}", (Object)cookieSpecName);
        }
        URIAuthority authority = request.getAuthority();
        String path = request.getPath();
        if (TextUtils.isEmpty(path)) {
            path = "/";
        }
        String string = hostName = authority != null ? authority.getHostName() : null;
        if (hostName == null) {
            hostName = route.getTargetHost().getHostName();
        }
        int n = port = authority != null ? authority.getPort() : -1;
        if (port < 0) {
            port = route.getTargetHost().getPort();
        }
        CookieOrigin cookieOrigin = new CookieOrigin(hostName, port, path, route.isSecure());
        CookieSpecFactory factory = registry.lookup(cookieSpecName);
        if (factory == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unsupported cookie spec: {}", (Object)cookieSpecName);
            }
            return;
        }
        CookieSpec cookieSpec = factory.create(clientContext);
        List<Cookie> cookies = cookieStore.getCookies();
        ArrayList<Cookie> matchedCookies = new ArrayList<Cookie>();
        Date now = new Date();
        boolean expired = false;
        for (Cookie cookie : cookies) {
            if (!cookie.isExpired(now)) {
                if (!cookieSpec.match(cookie, cookieOrigin)) continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cookie {} match {}", (Object)cookie, (Object)cookieOrigin);
                }
                matchedCookies.add(cookie);
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cookie {} expired", (Object)cookie);
            }
            expired = true;
        }
        if (expired) {
            cookieStore.clearExpired(now);
        }
        if (!matchedCookies.isEmpty()) {
            List<Header> headers = cookieSpec.formatCookies(matchedCookies);
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        context.setAttribute("http.cookie-spec", cookieSpec);
        context.setAttribute("http.cookie-origin", cookieOrigin);
    }
}

