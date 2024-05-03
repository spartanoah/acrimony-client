/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.util.Iterator;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultConnectionKeepAliveStrategy
implements ConnectionKeepAliveStrategy {
    public static final DefaultConnectionKeepAliveStrategy INSTANCE = new DefaultConnectionKeepAliveStrategy();

    @Override
    public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
        Args.notNull(response, "HTTP response");
        Iterator<HeaderElement> it = MessageSupport.iterate(response, "keep-alive");
        while (it.hasNext()) {
            HeaderElement he = it.next();
            String param = he.getName();
            String value = he.getValue();
            if (value == null || !param.equalsIgnoreCase("timeout")) continue;
            try {
                return TimeValue.ofSeconds(Long.parseLong(value));
            } catch (NumberFormatException ignore) {
            }
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        RequestConfig requestConfig = clientContext.getRequestConfig();
        return requestConfig.getConnectionKeepAlive();
    }
}

