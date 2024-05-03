/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpec;

class RFC6265CookieSpecBase
extends RFC6265CookieSpec {
    RFC6265CookieSpecBase(CommonCookieAttributeHandler ... handlers) {
        super(handlers);
    }
}

