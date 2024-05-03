/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Iterator;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.MessageCopier;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.MessageSupport;

class ConditionalRequestBuilder<T extends HttpRequest> {
    private final MessageCopier<T> messageCopier;

    ConditionalRequestBuilder(MessageCopier<T> messageCopier) {
        this.messageCopier = messageCopier;
    }

    public T buildConditionalRequest(T request, HttpCacheEntry cacheEntry) {
        Header lastModified;
        HttpRequest newRequest = (HttpRequest)this.messageCopier.copy(request);
        newRequest.setHeaders(request.getHeaders());
        Header eTag = cacheEntry.getFirstHeader("ETag");
        if (eTag != null) {
            newRequest.setHeader("If-None-Match", eTag.getValue());
        }
        if ((lastModified = cacheEntry.getFirstHeader("Last-Modified")) != null) {
            newRequest.setHeader("If-Modified-Since", lastModified.getValue());
        }
        boolean mustRevalidate = false;
        Iterator<HeaderElement> it = MessageSupport.iterate(cacheEntry, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"must-revalidate".equalsIgnoreCase(elt.getName()) && !"proxy-revalidate".equalsIgnoreCase(elt.getName())) continue;
            mustRevalidate = true;
            break;
        }
        if (mustRevalidate) {
            newRequest.addHeader("Cache-Control", "max-age=0");
        }
        return (T)newRequest;
    }

    public T buildConditionalRequestFromVariants(T request, Map<String, Variant> variants) {
        HttpRequest newRequest = (HttpRequest)this.messageCopier.copy(request);
        newRequest.setHeaders(request.getHeaders());
        StringBuilder etags = new StringBuilder();
        boolean first = true;
        for (String etag : variants.keySet()) {
            if (!first) {
                etags.append(",");
            }
            first = false;
            etags.append(etag);
        }
        newRequest.setHeader("If-None-Match", etags.toString());
        return (T)newRequest;
    }

    public T buildUnconditionalRequest(T request) {
        HttpRequest newRequest = (HttpRequest)this.messageCopier.copy(request);
        newRequest.addHeader("Cache-Control", "no-cache");
        newRequest.addHeader("Pragma", "no-cache");
        newRequest.removeHeaders("If-Range");
        newRequest.removeHeaders("If-Match");
        newRequest.removeHeaders("If-None-Match");
        newRequest.removeHeaders("If-Unmodified-Since");
        newRequest.removeHeaders("If-Modified-Since");
        return (T)newRequest;
    }
}

