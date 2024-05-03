/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public interface Http1StreamListener {
    public void onRequestHead(HttpConnection var1, HttpRequest var2);

    public void onResponseHead(HttpConnection var1, HttpResponse var2);

    public void onExchangeComplete(HttpConnection var1, boolean var2);
}

