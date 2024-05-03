/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class RequestConnControl
implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getMethod();
        if (Method.CONNECT.isSame(method)) {
            return;
        }
        if (!request.containsHeader("Connection")) {
            if (request.containsHeader("Upgrade")) {
                request.addHeader("Connection", "upgrade");
            } else {
                request.addHeader("Connection", "keep-alive");
            }
        }
    }
}

