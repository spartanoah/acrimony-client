/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.client5.http.impl.cookie.AbstractCookieAttributeHandler;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.STATELESS)
public class LaxMaxAgeHandler
extends AbstractCookieAttributeHandler
implements CommonCookieAttributeHandler {
    private static final Pattern MAX_AGE_PATTERN = Pattern.compile("^\\-?[0-9]+$");

    @Override
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (TextUtils.isBlank(value)) {
            return;
        }
        Matcher matcher = MAX_AGE_PATTERN.matcher(value);
        if (matcher.matches()) {
            int age;
            try {
                age = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return;
            }
            Date expiryDate = age >= 0 ? new Date(System.currentTimeMillis() + (long)age * 1000L) : new Date(Long.MIN_VALUE);
            cookie.setExpiryDate(expiryDate);
        }
    }

    @Override
    public String getAttributeName() {
        return "max-age";
    }
}

