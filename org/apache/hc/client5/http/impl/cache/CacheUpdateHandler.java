/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.HeapResourceFactory;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;

class CacheUpdateHandler {
    private final ResourceFactory resourceFactory;

    CacheUpdateHandler() {
        this(new HeapResourceFactory());
    }

    CacheUpdateHandler(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public HttpCacheEntry createtCacheEntry(HttpRequest request, HttpResponse originResponse, ByteArrayBuffer content, Date requestSent, Date responseReceived) throws ResourceIOException {
        return new HttpCacheEntry(requestSent, responseReceived, originResponse.getCode(), originResponse.getHeaders(), content != null ? this.resourceFactory.generate(request.getRequestUri(), content.array(), 0, content.length()) : null);
    }

    public HttpCacheEntry updateCacheEntry(String requestId, HttpCacheEntry entry, Date requestDate, Date responseDate, HttpResponse response) throws ResourceIOException {
        Args.check(response.getCode() == 304, "Response must have 304 status code");
        Header[] mergedHeaders = this.mergeHeaders(entry, response);
        Resource resource = null;
        if (entry.getResource() != null) {
            resource = this.resourceFactory.copy(requestId, entry.getResource());
        }
        return new HttpCacheEntry(requestDate, responseDate, entry.getStatus(), mergedHeaders, resource);
    }

    public HttpCacheEntry updateParentCacheEntry(String requestId, HttpCacheEntry existing, HttpCacheEntry entry, String variantKey, String variantCacheKey) throws ResourceIOException {
        HttpCacheEntry src = existing;
        if (src == null) {
            src = entry;
        }
        Resource resource = null;
        if (src.getResource() != null) {
            resource = this.resourceFactory.copy(requestId, src.getResource());
        }
        HashMap<String, String> variantMap = new HashMap<String, String>(src.getVariantMap());
        variantMap.put(variantKey, variantCacheKey);
        return new HttpCacheEntry(src.getRequestDate(), src.getResponseDate(), src.getStatus(), src.getHeaders(), resource, variantMap);
    }

    private Header[] mergeHeaders(HttpCacheEntry entry, HttpResponse response) {
        Header responseHeader;
        if (DateUtils.isAfter(entry, response, "Date")) {
            return entry.getHeaders();
        }
        HeaderGroup headerGroup = new HeaderGroup();
        headerGroup.setHeaders(entry.getHeaders());
        Iterator<Header> it = response.headerIterator();
        while (it.hasNext()) {
            responseHeader = it.next();
            if ("Content-Encoding".equals(responseHeader.getName()) || "Content-Length".equals(responseHeader.getName())) continue;
            headerGroup.removeHeaders(responseHeader.getName());
        }
        it = headerGroup.headerIterator();
        while (it.hasNext()) {
            String warningValue;
            Header cacheHeader = it.next();
            if (!"Warning".equalsIgnoreCase(cacheHeader.getName()) || (warningValue = cacheHeader.getValue()) == null || !warningValue.startsWith("1")) continue;
            it.remove();
        }
        it = response.headerIterator();
        while (it.hasNext()) {
            responseHeader = it.next();
            if ("Content-Encoding".equals(responseHeader.getName()) || "Content-Length".equals(responseHeader.getName())) continue;
            headerGroup.addHeader(responseHeader);
        }
        return headerGroup.getHeaders();
    }
}

