/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.impl.cookie.BasicDomainHandler;
import org.apache.hc.client5.http.impl.cookie.BasicExpiresHandler;
import org.apache.hc.client5.http.impl.cookie.BasicMaxAgeHandler;
import org.apache.hc.client5.http.impl.cookie.BasicPathHandler;
import org.apache.hc.client5.http.impl.cookie.BasicSecureHandler;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpecBase;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.SAFE)
public class RFC6265StrictSpec
extends RFC6265CookieSpecBase {
    static final String[] DATE_PATTERNS = new String[]{"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy"};

    public RFC6265StrictSpec() {
        super(new BasicPathHandler(), new BasicDomainHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(DATE_PATTERNS));
    }

    RFC6265StrictSpec(CommonCookieAttributeHandler ... handlers) {
        super(handlers);
    }

    public String toString() {
        return "rfc6265-strict";
    }
}

