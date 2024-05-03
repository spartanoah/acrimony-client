/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.impl.cache.RequestProtocolError;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.TimeValue;

class CachedHttpResponseGenerator {
    private final CacheValidityPolicy validityStrategy;

    CachedHttpResponseGenerator(CacheValidityPolicy validityStrategy) {
        this.validityStrategy = validityStrategy;
    }

    SimpleHttpResponse generateResponse(HttpRequest request, HttpCacheEntry entry) throws ResourceIOException {
        TimeValue age;
        Date now = new Date();
        SimpleHttpResponse response = new SimpleHttpResponse(entry.getStatus());
        response.setVersion(HttpVersion.DEFAULT);
        response.setHeaders(entry.getHeaders());
        if (this.responseShouldContainEntity(request, entry)) {
            Resource resource = entry.getResource();
            Header h = entry.getFirstHeader("Content-Type");
            ContentType contentType = h != null ? ContentType.parse(h.getValue()) : null;
            byte[] content = resource.get();
            this.addMissingContentLengthHeader(response, content);
            response.setBody(content, contentType);
        }
        if (TimeValue.isPositive(age = this.validityStrategy.getCurrentAge(entry, now))) {
            if (age.compareTo(CacheValidityPolicy.MAX_AGE) >= 0) {
                response.setHeader("Age", "" + CacheValidityPolicy.MAX_AGE.toSeconds());
            } else {
                response.setHeader("Age", "" + age.toSeconds());
            }
        }
        return response;
    }

    SimpleHttpResponse generateNotModifiedResponse(HttpCacheEntry entry) {
        Header varyHeader;
        Header cacheControlHeader;
        Header expiresHeader;
        Header contentLocationHeader;
        SimpleHttpResponse response = new SimpleHttpResponse(304, "Not Modified");
        Header dateHeader = entry.getFirstHeader("Date");
        if (dateHeader == null) {
            dateHeader = new BasicHeader("Date", DateUtils.formatDate(new Date()));
        }
        response.addHeader(dateHeader);
        Header etagHeader = entry.getFirstHeader("ETag");
        if (etagHeader != null) {
            response.addHeader(etagHeader);
        }
        if ((contentLocationHeader = entry.getFirstHeader("Content-Location")) != null) {
            response.addHeader(contentLocationHeader);
        }
        if ((expiresHeader = entry.getFirstHeader("Expires")) != null) {
            response.addHeader(expiresHeader);
        }
        if ((cacheControlHeader = entry.getFirstHeader("Cache-Control")) != null) {
            response.addHeader(cacheControlHeader);
        }
        if ((varyHeader = entry.getFirstHeader("Vary")) != null) {
            response.addHeader(varyHeader);
        }
        return response;
    }

    private void addMissingContentLengthHeader(HttpResponse response, byte[] body) {
        if (this.transferEncodingIsPresent(response)) {
            return;
        }
        response.setHeader("Content-Length", Integer.toString(body.length));
    }

    private boolean transferEncodingIsPresent(HttpResponse response) {
        Header hdr = response.getFirstHeader("Transfer-Encoding");
        return hdr != null;
    }

    private boolean responseShouldContainEntity(HttpRequest request, HttpCacheEntry cacheEntry) {
        return request.getMethod().equals("GET") && cacheEntry.getResource() != null;
    }

    public SimpleHttpResponse getErrorForRequest(RequestProtocolError errorCheck) {
        switch (errorCheck) {
            case BODY_BUT_NO_LENGTH_ERROR: {
                return SimpleHttpResponse.create(411);
            }
            case WEAK_ETAG_AND_RANGE_ERROR: {
                return SimpleHttpResponse.create(400, "Weak eTag not compatible with byte range", ContentType.DEFAULT_TEXT);
            }
            case WEAK_ETAG_ON_PUTDELETE_METHOD_ERROR: {
                return SimpleHttpResponse.create(400, "Weak eTag not compatible with PUT or DELETE requests");
            }
            case NO_CACHE_DIRECTIVE_WITH_FIELD_NAME: {
                return SimpleHttpResponse.create(400, "No-Cache directive MUST NOT include a field name");
            }
        }
        throw new IllegalStateException("The request was compliant, therefore no error can be generated for it.");
    }
}

