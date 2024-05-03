/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.util.Iterator;
import org.apache.hc.client5.http.impl.MessageCopier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;

public final class RequestCopier
implements MessageCopier<HttpRequest> {
    public static final RequestCopier INSTANCE = new RequestCopier();

    @Override
    public HttpRequest copy(HttpRequest original) {
        if (original == null) {
            return null;
        }
        BasicHttpRequest copy = new BasicHttpRequest(original.getMethod(), original.getPath());
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        copy.setScheme(original.getScheme());
        copy.setAuthority(original.getAuthority());
        return copy;
    }
}

