/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ConnectionReuseStrategy {
    public boolean keepAlive(HttpRequest var1, HttpResponse var2, HttpContext var3);
}

