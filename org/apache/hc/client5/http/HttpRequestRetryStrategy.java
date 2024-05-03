/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface HttpRequestRetryStrategy {
    public boolean retryRequest(HttpRequest var1, IOException var2, int var3, HttpContext var4);

    public boolean retryRequest(HttpResponse var1, int var2, HttpContext var3);

    public TimeValue getRetryInterval(HttpResponse var1, int var2, HttpContext var3);
}

