/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class NoopUserTokenHandler
implements UserTokenHandler {
    public static final NoopUserTokenHandler INSTANCE = new NoopUserTokenHandler();

    @Override
    public Object getUserToken(HttpRoute route, HttpContext context) {
        return null;
    }
}

