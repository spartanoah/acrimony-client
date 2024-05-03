/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.cookie;

import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpec;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class IgnoreSpecFactory
implements CookieSpecFactory,
CookieSpecProvider {
    public CookieSpec newInstance(HttpParams params) {
        return new IgnoreSpec();
    }

    public CookieSpec create(HttpContext context) {
        return new IgnoreSpec();
    }
}

