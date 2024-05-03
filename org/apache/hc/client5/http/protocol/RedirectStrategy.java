/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.net.URI;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface RedirectStrategy {
    public boolean isRedirected(HttpRequest var1, HttpResponse var2, HttpContext var3) throws HttpException;

    public URI getLocationURI(HttpRequest var1, HttpResponse var2, HttpContext var3) throws HttpException;
}

