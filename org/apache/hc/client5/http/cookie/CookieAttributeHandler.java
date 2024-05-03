/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cookie;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface CookieAttributeHandler {
    public void parse(SetCookie var1, String var2) throws MalformedCookieException;

    public void validate(Cookie var1, CookieOrigin var2) throws MalformedCookieException;

    public boolean match(Cookie var1, CookieOrigin var2);
}

