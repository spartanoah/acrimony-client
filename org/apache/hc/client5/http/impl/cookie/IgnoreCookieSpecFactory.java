/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.impl.cookie.IgnoreSpecSpec;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.SAFE)
public class IgnoreCookieSpecFactory
implements CookieSpecFactory {
    private volatile CookieSpec cookieSpec;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            IgnoreCookieSpecFactory ignoreCookieSpecFactory = this;
            synchronized (ignoreCookieSpecFactory) {
                if (this.cookieSpec == null) {
                    this.cookieSpec = new IgnoreSpecSpec();
                }
            }
        }
        return this.cookieSpec;
    }
}

