/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.STATELESS)
public class BasicPathHandler
implements CommonCookieAttributeHandler {
    @Override
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        cookie.setPath(!TextUtils.isBlank(value) ? value : "/");
    }

    @Override
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
    }

    static boolean pathMatch(String uriPath, String cookiePath) {
        String normalizedCookiePath = cookiePath;
        if (normalizedCookiePath == null) {
            normalizedCookiePath = "/";
        }
        if (normalizedCookiePath.length() > 1 && normalizedCookiePath.endsWith("/")) {
            normalizedCookiePath = normalizedCookiePath.substring(0, normalizedCookiePath.length() - 1);
        }
        if (uriPath.startsWith(normalizedCookiePath)) {
            if (normalizedCookiePath.equals("/")) {
                return true;
            }
            if (uriPath.length() == normalizedCookiePath.length()) {
                return true;
            }
            if (uriPath.charAt(normalizedCookiePath.length()) == '/') {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        return BasicPathHandler.pathMatch(origin.getPath(), cookie.getPath());
    }

    @Override
    public String getAttributeName() {
        return "path";
    }
}

