/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class RequestClientConnControl
implements HttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RequestClientConnControl.class);

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getMethod();
        if (method.equalsIgnoreCase("CONNECT")) {
            return;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        RouteInfo route = clientContext.getHttpRoute();
        if (route == null) {
            LOG.debug("Connection route not set in the context");
            return;
        }
        if ((route.getHopCount() == 1 || route.isTunnelled()) && !request.containsHeader("Connection")) {
            request.addHeader("Connection", "keep-alive");
        }
    }
}

