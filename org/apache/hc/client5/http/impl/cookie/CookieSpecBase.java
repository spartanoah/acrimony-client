/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.impl.cookie.AbstractCookieSpec;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class CookieSpecBase
extends AbstractCookieSpec {
    public CookieSpecBase() {
    }

    protected CookieSpecBase(HashMap<String, CookieAttributeHandler> map) {
        super(map);
    }

    protected CookieSpecBase(CommonCookieAttributeHandler ... handlers) {
        super(handlers);
    }

    protected static String getDefaultPath(CookieOrigin origin) {
        String defaultPath = origin.getPath();
        int lastSlashIndex = defaultPath.lastIndexOf(47);
        if (lastSlashIndex >= 0) {
            if (lastSlashIndex == 0) {
                lastSlashIndex = 1;
            }
            defaultPath = defaultPath.substring(0, lastSlashIndex);
        }
        return defaultPath;
    }

    protected static String getDefaultDomain(CookieOrigin origin) {
        return origin.getHost();
    }

    protected List<Cookie> parse(HeaderElement[] elems, CookieOrigin origin) throws MalformedCookieException {
        ArrayList<Cookie> cookies = new ArrayList<Cookie>(elems.length);
        for (HeaderElement headerelement : elems) {
            String name = headerelement.getName();
            String value = headerelement.getValue();
            if (name == null || name.isEmpty()) {
                throw new MalformedCookieException("Cookie name may not be empty");
            }
            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setPath(CookieSpecBase.getDefaultPath(origin));
            cookie.setDomain(CookieSpecBase.getDefaultDomain(origin));
            NameValuePair[] attribs = headerelement.getParameters();
            for (int j = attribs.length - 1; j >= 0; --j) {
                NameValuePair attrib = attribs[j];
                String s = attrib.getName().toLowerCase(Locale.ROOT);
                cookie.setAttribute(s, attrib.getValue());
                CookieAttributeHandler handler = this.findAttribHandler(s);
                if (handler == null) continue;
                handler.parse(cookie, attrib.getValue());
            }
            cookies.add(cookie);
        }
        return cookies;
    }

    @Override
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (CookieAttributeHandler handler : this.getAttribHandlers()) {
            handler.validate(cookie, origin);
        }
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (CookieAttributeHandler handler : this.getAttribHandlers()) {
            if (handler.match(cookie, origin)) continue;
            return false;
        }
        return true;
    }
}

