/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import java.util.Collection;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class RequestDefaultHeaders
implements HttpRequestInterceptor {
    private final Collection<? extends Header> defaultHeaders;

    public RequestDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public RequestDefaultHeaders() {
        this(null);
    }

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getMethod();
        if (method.equalsIgnoreCase("CONNECT")) {
            return;
        }
        if (this.defaultHeaders != null) {
            for (Header header : this.defaultHeaders) {
                if (request.containsHeader(header.getName())) continue;
                request.addHeader(header);
            }
        }
    }
}

