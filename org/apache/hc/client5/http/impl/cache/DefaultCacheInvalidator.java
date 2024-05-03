/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.URI;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheInvalidatorBase;
import org.apache.hc.client5.http.impl.cache.HttpCacheSupport;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public class DefaultCacheInvalidator
extends CacheInvalidatorBase
implements HttpCacheInvalidator {
    public static final DefaultCacheInvalidator INSTANCE = new DefaultCacheInvalidator();
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCacheInvalidator.class);

    private HttpCacheEntry getEntry(HttpCacheStorage storage, String cacheKey) {
        try {
            return storage.getEntry(cacheKey);
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unable to get cache entry with key {}", (Object)cacheKey, (Object)ex);
            }
            return null;
        }
    }

    private void removeEntry(HttpCacheStorage storage, String cacheKey) {
        block2: {
            try {
                storage.removeEntry(cacheKey);
            } catch (ResourceIOException ex) {
                if (!LOG.isWarnEnabled()) break block2;
                LOG.warn("Unable to flush cache entry with key {}", (Object)cacheKey, (Object)ex);
            }
        }
    }

    @Override
    public void flushCacheEntriesInvalidatedByRequest(HttpHost host, HttpRequest request, Resolver<URI, String> cacheKeyResolver, HttpCacheStorage storage) {
        String s = HttpCacheSupport.getRequestUri(request, host);
        URI uri = HttpCacheSupport.normalizeQuetly(s);
        String cacheKey = uri != null ? cacheKeyResolver.resolve(uri) : s;
        HttpCacheEntry parent = this.getEntry(storage, cacheKey);
        if (DefaultCacheInvalidator.requestShouldNotBeCached(request) || DefaultCacheInvalidator.shouldInvalidateHeadCacheEntry(request, parent)) {
            if (parent != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalidating parent cache entry with key {}", (Object)cacheKey);
                }
                for (String variantURI : parent.getVariantMap().values()) {
                    this.removeEntry(storage, variantURI);
                }
                this.removeEntry(storage, cacheKey);
            }
            if (uri != null) {
                URI location;
                Header lHdr;
                URI contentLocation;
                Header clHdr;
                if (LOG.isWarnEnabled()) {
                    LOG.warn("{} is not a valid URI", (Object)s);
                }
                if ((clHdr = request.getFirstHeader("Content-Location")) != null && (contentLocation = HttpCacheSupport.normalizeQuetly(clHdr.getValue())) != null && !this.flushAbsoluteUriFromSameHost(uri, contentLocation, cacheKeyResolver, storage)) {
                    this.flushRelativeUriFromSameHost(uri, contentLocation, cacheKeyResolver, storage);
                }
                if ((lHdr = request.getFirstHeader("Location")) != null && (location = HttpCacheSupport.normalizeQuetly(lHdr.getValue())) != null) {
                    this.flushAbsoluteUriFromSameHost(uri, location, cacheKeyResolver, storage);
                }
            }
        }
    }

    private void flushRelativeUriFromSameHost(URI requestUri, URI uri, Resolver<URI, String> cacheKeyResolver, HttpCacheStorage storage) {
        URI resolvedUri;
        URI uRI = resolvedUri = uri != null ? URIUtils.resolve(requestUri, uri) : null;
        if (resolvedUri != null && DefaultCacheInvalidator.isSameHost(requestUri, resolvedUri)) {
            this.removeEntry(storage, cacheKeyResolver.resolve(resolvedUri));
        }
    }

    private boolean flushAbsoluteUriFromSameHost(URI requestUri, URI uri, Resolver<URI, String> cacheKeyResolver, HttpCacheStorage storage) {
        if (uri != null && DefaultCacheInvalidator.isSameHost(requestUri, uri)) {
            this.removeEntry(storage, cacheKeyResolver.resolve(uri));
            return true;
        }
        return false;
    }

    @Override
    public void flushCacheEntriesInvalidatedByExchange(HttpHost host, HttpRequest request, HttpResponse response, Resolver<URI, String> cacheKeyResolver, HttpCacheStorage storage) {
        URI location;
        int status = response.getCode();
        if (status < 200 || status > 299) {
            return;
        }
        String s = HttpCacheSupport.getRequestUri(request, host);
        URI uri = HttpCacheSupport.normalizeQuetly(s);
        if (uri == null) {
            return;
        }
        URI contentLocation = DefaultCacheInvalidator.getContentLocationURI(uri, response);
        if (contentLocation != null && DefaultCacheInvalidator.isSameHost(uri, contentLocation)) {
            this.flushLocationCacheEntry(response, contentLocation, storage, cacheKeyResolver);
        }
        if ((location = DefaultCacheInvalidator.getLocationURI(uri, response)) != null && DefaultCacheInvalidator.isSameHost(uri, location)) {
            this.flushLocationCacheEntry(response, location, storage, cacheKeyResolver);
        }
    }

    private void flushLocationCacheEntry(HttpResponse response, URI location, HttpCacheStorage storage, Resolver<URI, String> cacheKeyResolver) {
        String cacheKey = cacheKeyResolver.resolve(location);
        HttpCacheEntry entry = this.getEntry(storage, cacheKey);
        if (entry != null && !DefaultCacheInvalidator.responseDateOlderThanEntryDate(response, entry) && DefaultCacheInvalidator.responseAndEntryEtagsDiffer(response, entry)) {
            this.removeEntry(storage, cacheKey);
        }
    }
}

