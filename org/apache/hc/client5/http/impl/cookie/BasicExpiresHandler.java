/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.Date;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.client5.http.impl.cookie.AbstractCookieAttributeHandler;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class BasicExpiresHandler
extends AbstractCookieAttributeHandler
implements CommonCookieAttributeHandler {
    private final String[] datePatterns;

    public BasicExpiresHandler(String[] datePatterns) {
        Args.notNull(datePatterns, "Array of date patterns");
        this.datePatterns = (String[])datePatterns.clone();
    }

    @Override
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (value == null) {
            throw new MalformedCookieException("Missing value for 'expires' attribute");
        }
        Date expiry = DateUtils.parseDate(value, this.datePatterns);
        if (expiry == null) {
            throw new MalformedCookieException("Invalid 'expires' attribute: " + value);
        }
        cookie.setExpiryDate(expiry);
    }

    @Override
    public String getAttributeName() {
        return "expires";
    }
}

