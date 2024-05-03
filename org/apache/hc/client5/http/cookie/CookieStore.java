/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cookie;

import java.util.Date;
import java.util.List;
import org.apache.hc.client5.http.cookie.Cookie;

public interface CookieStore {
    public void addCookie(Cookie var1);

    public List<Cookie> getCookies();

    public boolean clearExpired(Date var1);

    public void clear();
}

