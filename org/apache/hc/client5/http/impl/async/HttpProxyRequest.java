/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.net.URI;
import java.util.Iterator;
import org.apache.hc.client5.http.impl.ProtocolSupport;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;

class HttpProxyRequest
extends BasicHttpRequest {
    static HttpProxyRequest rewrite(HttpRequest original, URI requestUri) {
        HttpProxyRequest copy = new HttpProxyRequest(original.getMethod(), requestUri);
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        return copy;
    }

    private HttpProxyRequest(String method, URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public String getRequestUri() {
        return ProtocolSupport.getRequestUri(this);
    }
}

