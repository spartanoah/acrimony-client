/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHeaderIterator;
import org.apache.http.message.BasicTokenIterator;
import org.apache.http.protocol.HttpContext;

public class DefaultClientConnectionReuseStrategy
extends DefaultConnectionReuseStrategy {
    public static final DefaultClientConnectionReuseStrategy INSTANCE = new DefaultClientConnectionReuseStrategy();

    @Override
    public boolean keepAlive(HttpResponse response, HttpContext context) {
        Header[] connHeaders;
        HttpRequest request = (HttpRequest)context.getAttribute("http.request");
        if (request != null && (connHeaders = request.getHeaders("Connection")).length != 0) {
            BasicTokenIterator ti = new BasicTokenIterator(new BasicHeaderIterator(connHeaders, null));
            while (ti.hasNext()) {
                String token = ti.nextToken();
                if (!"Close".equalsIgnoreCase(token)) continue;
                return false;
            }
        }
        return super.keepAlive(response, context);
    }
}

