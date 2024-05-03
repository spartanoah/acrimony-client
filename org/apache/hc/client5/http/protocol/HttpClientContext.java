/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.Args;

public class HttpClientContext
extends HttpCoreContext {
    public static final String HTTP_ROUTE = "http.route";
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    public static final String COOKIESPEC_REGISTRY = "http.cookiespec-registry";
    public static final String COOKIE_SPEC = "http.cookie-spec";
    public static final String COOKIE_ORIGIN = "http.cookie-origin";
    public static final String COOKIE_STORE = "http.cookie-store";
    public static final String CREDS_PROVIDER = "http.auth.credentials-provider";
    public static final String AUTH_CACHE = "http.auth.auth-cache";
    public static final String AUTH_EXCHANGE_MAP = "http.auth.exchanges";
    public static final String USER_TOKEN = "http.user-token";
    public static final String AUTHSCHEME_REGISTRY = "http.authscheme-registry";
    public static final String REQUEST_CONFIG = "http.request-config";

    public static HttpClientContext adapt(HttpContext context) {
        Args.notNull(context, "HTTP context");
        if (context instanceof HttpClientContext) {
            return (HttpClientContext)context;
        }
        return new HttpClientContext(context);
    }

    public static HttpClientContext create() {
        return new HttpClientContext(new BasicHttpContext());
    }

    public HttpClientContext(HttpContext context) {
        super(context);
    }

    public HttpClientContext() {
    }

    public RouteInfo getHttpRoute() {
        return this.getAttribute(HTTP_ROUTE, HttpRoute.class);
    }

    public RedirectLocations getRedirectLocations() {
        return this.getAttribute(REDIRECT_LOCATIONS, RedirectLocations.class);
    }

    public CookieStore getCookieStore() {
        return this.getAttribute(COOKIE_STORE, CookieStore.class);
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.setAttribute(COOKIE_STORE, cookieStore);
    }

    public CookieSpec getCookieSpec() {
        return this.getAttribute(COOKIE_SPEC, CookieSpec.class);
    }

    public CookieOrigin getCookieOrigin() {
        return this.getAttribute(COOKIE_ORIGIN, CookieOrigin.class);
    }

    private <T> Lookup<T> getLookup(String name, Class<T> clazz) {
        return this.getAttribute(name, Lookup.class);
    }

    public Lookup<CookieSpecFactory> getCookieSpecRegistry() {
        return this.getLookup(COOKIESPEC_REGISTRY, CookieSpecFactory.class);
    }

    public void setCookieSpecRegistry(Lookup<CookieSpecFactory> lookup) {
        this.setAttribute(COOKIESPEC_REGISTRY, lookup);
    }

    public Lookup<AuthSchemeFactory> getAuthSchemeRegistry() {
        return this.getLookup(AUTHSCHEME_REGISTRY, AuthSchemeFactory.class);
    }

    public void setAuthSchemeRegistry(Lookup<AuthSchemeFactory> lookup) {
        this.setAttribute(AUTHSCHEME_REGISTRY, lookup);
    }

    public CredentialsProvider getCredentialsProvider() {
        return this.getAttribute(CREDS_PROVIDER, CredentialsProvider.class);
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.setAttribute(CREDS_PROVIDER, credentialsProvider);
    }

    public AuthCache getAuthCache() {
        return this.getAttribute(AUTH_CACHE, AuthCache.class);
    }

    public void setAuthCache(AuthCache authCache) {
        this.setAttribute(AUTH_CACHE, authCache);
    }

    public Map<HttpHost, AuthExchange> getAuthExchanges() {
        HashMap map = (HashMap)this.getAttribute(AUTH_EXCHANGE_MAP);
        if (map == null) {
            map = new HashMap();
            this.setAttribute(AUTH_EXCHANGE_MAP, map);
        }
        return map;
    }

    public AuthExchange getAuthExchange(HttpHost host) {
        Map<HttpHost, AuthExchange> authExchangeMap = this.getAuthExchanges();
        AuthExchange authExchange = authExchangeMap.get(host);
        if (authExchange == null) {
            authExchange = new AuthExchange();
            authExchangeMap.put(host, authExchange);
        }
        return authExchange;
    }

    public void setAuthExchange(HttpHost host, AuthExchange authExchange) {
        Map<HttpHost, AuthExchange> authExchangeMap = this.getAuthExchanges();
        authExchangeMap.put(host, authExchange);
    }

    public void resetAuthExchange(HttpHost host, AuthScheme authScheme) {
        AuthExchange authExchange = new AuthExchange();
        authExchange.select(authScheme);
        Map<HttpHost, AuthExchange> authExchangeMap = this.getAuthExchanges();
        authExchangeMap.put(host, authExchange);
    }

    public <T> T getUserToken(Class<T> clazz) {
        return this.getAttribute(USER_TOKEN, clazz);
    }

    public Object getUserToken() {
        return this.getAttribute(USER_TOKEN);
    }

    public void setUserToken(Object obj) {
        this.setAttribute(USER_TOKEN, obj);
    }

    public RequestConfig getRequestConfig() {
        RequestConfig config = this.getAttribute(REQUEST_CONFIG, RequestConfig.class);
        return config != null ? config : RequestConfig.DEFAULT;
    }

    public void setRequestConfig(RequestConfig config) {
        this.setAttribute(REQUEST_CONFIG, config);
    }
}

