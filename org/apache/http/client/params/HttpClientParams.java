/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client.params;

import org.apache.http.annotation.Immutable;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

@Deprecated
@Immutable
public class HttpClientParams {
    private HttpClientParams() {
    }

    public static boolean isRedirecting(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getBooleanParameter("http.protocol.handle-redirects", true);
    }

    public static void setRedirecting(HttpParams params, boolean value) {
        Args.notNull(params, "HTTP parameters");
        params.setBooleanParameter("http.protocol.handle-redirects", value);
    }

    public static boolean isAuthenticating(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getBooleanParameter("http.protocol.handle-authentication", true);
    }

    public static void setAuthenticating(HttpParams params, boolean value) {
        Args.notNull(params, "HTTP parameters");
        params.setBooleanParameter("http.protocol.handle-authentication", value);
    }

    public static String getCookiePolicy(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        String cookiePolicy = (String)params.getParameter("http.protocol.cookie-policy");
        if (cookiePolicy == null) {
            return "best-match";
        }
        return cookiePolicy;
    }

    public static void setCookiePolicy(HttpParams params, String cookiePolicy) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter("http.protocol.cookie-policy", cookiePolicy);
    }

    public static void setConnectionManagerTimeout(HttpParams params, long timeout) {
        Args.notNull(params, "HTTP parameters");
        params.setLongParameter("http.conn-manager.timeout", timeout);
    }

    public static long getConnectionManagerTimeout(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        Long timeout = (Long)params.getParameter("http.conn-manager.timeout");
        if (timeout != null) {
            return timeout;
        }
        return HttpConnectionParams.getConnectionTimeout(params);
    }
}

