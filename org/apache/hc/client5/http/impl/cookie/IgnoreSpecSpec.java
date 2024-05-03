/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.Collections;
import java.util.List;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.impl.cookie.CookieSpecBase;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;

@Contract(threading=ThreadingBehavior.STATELESS)
public class IgnoreSpecSpec
extends CookieSpecBase {
    @Override
    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        return Collections.emptyList();
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        return false;
    }

    @Override
    public List<Header> formatCookies(List<Cookie> cookies) {
        return Collections.emptyList();
    }
}

