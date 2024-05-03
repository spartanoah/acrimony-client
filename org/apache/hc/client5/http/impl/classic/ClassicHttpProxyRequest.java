/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.net.URI;
import java.util.Iterator;
import org.apache.hc.client5.http.impl.ProtocolSupport;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

class ClassicHttpProxyRequest
extends BasicClassicHttpRequest {
    static ClassicHttpProxyRequest rewrite(ClassicHttpRequest original, URI requestUri) {
        ClassicHttpProxyRequest copy = new ClassicHttpProxyRequest(original.getMethod(), requestUri);
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        copy.setEntity(original.getEntity());
        return copy;
    }

    private ClassicHttpProxyRequest(String method, URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public String getRequestUri() {
        return ProtocolSupport.getRequestUri(this);
    }
}

