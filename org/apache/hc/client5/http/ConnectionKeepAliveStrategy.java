/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ConnectionKeepAliveStrategy {
    public TimeValue getKeepAliveDuration(HttpResponse var1, HttpContext var2);
}

