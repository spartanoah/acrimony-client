/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.URI;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.cache.HttpCacheSupport;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

class CacheInvalidatorBase {
    CacheInvalidatorBase() {
    }

    static boolean shouldInvalidateHeadCacheEntry(HttpRequest req, HttpCacheEntry parentCacheEntry) {
        return CacheInvalidatorBase.requestIsGet(req) && CacheInvalidatorBase.isAHeadCacheEntry(parentCacheEntry);
    }

    static boolean requestIsGet(HttpRequest req) {
        return req.getMethod().equals("GET");
    }

    static boolean isAHeadCacheEntry(HttpCacheEntry parentCacheEntry) {
        return parentCacheEntry != null && parentCacheEntry.getRequestMethod().equals("HEAD");
    }

    static boolean isSameHost(URI requestURI, URI targetURI) {
        return targetURI.isAbsolute() && targetURI.getAuthority().equalsIgnoreCase(requestURI.getAuthority());
    }

    static boolean requestShouldNotBeCached(HttpRequest req) {
        String method = req.getMethod();
        return CacheInvalidatorBase.notGetOrHeadRequest(method);
    }

    static boolean notGetOrHeadRequest(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method);
    }

    private static URI getLocationURI(URI requestUri, HttpResponse response, String headerName) {
        Header h = response.getFirstHeader(headerName);
        if (h == null) {
            return null;
        }
        URI locationUri = HttpCacheSupport.normalizeQuetly(h.getValue());
        if (locationUri == null) {
            return requestUri;
        }
        if (locationUri.isAbsolute()) {
            return locationUri;
        }
        return URIUtils.resolve(requestUri, locationUri);
    }

    static URI getContentLocationURI(URI requestUri, HttpResponse response) {
        return CacheInvalidatorBase.getLocationURI(requestUri, response, "Content-Location");
    }

    static URI getLocationURI(URI requestUri, HttpResponse response) {
        return CacheInvalidatorBase.getLocationURI(requestUri, response, "Location");
    }

    static boolean responseAndEntryEtagsDiffer(HttpResponse response, HttpCacheEntry entry) {
        Header entryEtag = entry.getFirstHeader("ETag");
        Header responseEtag = response.getFirstHeader("ETag");
        if (entryEtag == null || responseEtag == null) {
            return false;
        }
        return !entryEtag.getValue().equals(responseEtag.getValue());
    }

    static boolean responseDateOlderThanEntryDate(HttpResponse response, HttpCacheEntry entry) {
        return DateUtils.isBefore(response, entry, "Date");
    }
}

