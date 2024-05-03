/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpAsyncCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.impl.cache.CacheInvalidatorBase;
import org.apache.hc.client5.http.impl.cache.HttpCacheSupport;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public class DefaultAsyncCacheInvalidator
extends CacheInvalidatorBase
implements HttpAsyncCacheInvalidator {
    public static final DefaultAsyncCacheInvalidator INSTANCE = new DefaultAsyncCacheInvalidator();
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAsyncCacheInvalidator.class);

    private void removeEntry(HttpAsyncCacheStorage storage, final String cacheKey) {
        storage.removeEntry(cacheKey, new FutureCallback<Boolean>(){

            @Override
            public void completed(Boolean result) {
                if (LOG.isDebugEnabled()) {
                    if (result.booleanValue()) {
                        LOG.debug("Cache entry with key {} successfully flushed", (Object)cacheKey);
                    } else {
                        LOG.debug("Cache entry with key {} could not be flushed", (Object)cacheKey);
                    }
                }
            }

            @Override
            public void failed(Exception ex) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Unable to flush cache entry with key {}", (Object)cacheKey, (Object)ex);
                }
            }

            @Override
            public void cancelled() {
            }
        });
    }

    @Override
    public Cancellable flushCacheEntriesInvalidatedByRequest(HttpHost host, final HttpRequest request, final Resolver<URI, String> cacheKeyResolver, final HttpAsyncCacheStorage storage, final FutureCallback<Boolean> callback) {
        final String s = HttpCacheSupport.getRequestUri(request, host);
        final URI uri = HttpCacheSupport.normalizeQuetly(s);
        final String cacheKey = uri != null ? cacheKeyResolver.resolve(uri) : s;
        return storage.getEntry(cacheKey, new FutureCallback<HttpCacheEntry>(){

            @Override
            public void completed(HttpCacheEntry parentEntry) {
                if (CacheInvalidatorBase.requestShouldNotBeCached(request) || CacheInvalidatorBase.shouldInvalidateHeadCacheEntry(request, parentEntry)) {
                    if (parentEntry != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Invalidating parentEntry cache entry with key {}", (Object)cacheKey);
                        }
                        for (String variantURI : parentEntry.getVariantMap().values()) {
                            DefaultAsyncCacheInvalidator.this.removeEntry(storage, variantURI);
                        }
                        DefaultAsyncCacheInvalidator.this.removeEntry(storage, cacheKey);
                    }
                    if (uri != null) {
                        URI location;
                        Header lHdr;
                        URI contentLocation;
                        Header clHdr;
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("{} is not a valid URI", (Object)s);
                        }
                        if ((clHdr = request.getFirstHeader("Content-Location")) != null && (contentLocation = HttpCacheSupport.normalizeQuetly(clHdr.getValue())) != null && !DefaultAsyncCacheInvalidator.this.flushAbsoluteUriFromSameHost(uri, contentLocation, cacheKeyResolver, storage)) {
                            DefaultAsyncCacheInvalidator.this.flushRelativeUriFromSameHost(uri, contentLocation, cacheKeyResolver, storage);
                        }
                        if ((lHdr = request.getFirstHeader("Location")) != null && (location = HttpCacheSupport.normalizeQuetly(lHdr.getValue())) != null) {
                            DefaultAsyncCacheInvalidator.this.flushAbsoluteUriFromSameHost(uri, location, cacheKeyResolver, storage);
                        }
                    }
                }
                callback.completed(Boolean.TRUE);
            }

            @Override
            public void failed(Exception ex) {
                callback.failed(ex);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }

    private void flushRelativeUriFromSameHost(URI requestUri, URI uri, Resolver<URI, String> cacheKeyResolver, HttpAsyncCacheStorage storage) {
        URI resolvedUri;
        URI uRI = resolvedUri = uri != null ? URIUtils.resolve(requestUri, uri) : null;
        if (resolvedUri != null && DefaultAsyncCacheInvalidator.isSameHost(requestUri, resolvedUri)) {
            this.removeEntry(storage, cacheKeyResolver.resolve(resolvedUri));
        }
    }

    private boolean flushAbsoluteUriFromSameHost(URI requestUri, URI uri, Resolver<URI, String> cacheKeyResolver, HttpAsyncCacheStorage storage) {
        if (uri != null && DefaultAsyncCacheInvalidator.isSameHost(requestUri, uri)) {
            this.removeEntry(storage, cacheKeyResolver.resolve(uri));
            return true;
        }
        return false;
    }

    @Override
    public Cancellable flushCacheEntriesInvalidatedByExchange(HttpHost host, HttpRequest request, final HttpResponse response, Resolver<URI, String> cacheKeyResolver, final HttpAsyncCacheStorage storage, final FutureCallback<Boolean> callback) {
        String s;
        URI requestUri;
        int status = response.getCode();
        if (status >= 200 && status < 300 && (requestUri = HttpCacheSupport.normalizeQuetly(s = HttpCacheSupport.getRequestUri(request, host))) != null) {
            URI location;
            ArrayList<String> cacheKeys = new ArrayList<String>(2);
            URI contentLocation = DefaultAsyncCacheInvalidator.getContentLocationURI(requestUri, response);
            if (contentLocation != null && DefaultAsyncCacheInvalidator.isSameHost(requestUri, contentLocation)) {
                cacheKeys.add(cacheKeyResolver.resolve(contentLocation));
            }
            if ((location = DefaultAsyncCacheInvalidator.getLocationURI(requestUri, response)) != null && DefaultAsyncCacheInvalidator.isSameHost(requestUri, location)) {
                cacheKeys.add(cacheKeyResolver.resolve(location));
            }
            if (cacheKeys.size() == 1) {
                final String key = (String)cacheKeys.get(0);
                storage.getEntry(key, new FutureCallback<HttpCacheEntry>(){

                    @Override
                    public void completed(HttpCacheEntry entry) {
                        if (entry != null && !CacheInvalidatorBase.responseDateOlderThanEntryDate(response, entry) && CacheInvalidatorBase.responseAndEntryEtagsDiffer(response, entry)) {
                            DefaultAsyncCacheInvalidator.this.removeEntry(storage, key);
                        }
                        callback.completed(Boolean.TRUE);
                    }

                    @Override
                    public void failed(Exception ex) {
                        callback.failed(ex);
                    }

                    @Override
                    public void cancelled() {
                        callback.cancelled();
                    }
                });
            } else if (cacheKeys.size() > 1) {
                storage.getEntries(cacheKeys, new FutureCallback<Map<String, HttpCacheEntry>>(){

                    @Override
                    public void completed(Map<String, HttpCacheEntry> resultMap) {
                        for (Map.Entry<String, HttpCacheEntry> resultEntry : resultMap.entrySet()) {
                            String key = resultEntry.getKey();
                            HttpCacheEntry entry = resultEntry.getValue();
                            if (CacheInvalidatorBase.responseDateOlderThanEntryDate(response, entry) || !CacheInvalidatorBase.responseAndEntryEtagsDiffer(response, entry)) continue;
                            DefaultAsyncCacheInvalidator.this.removeEntry(storage, key);
                        }
                        callback.completed(Boolean.TRUE);
                    }

                    @Override
                    public void failed(Exception ex) {
                        callback.failed(ex);
                    }

                    @Override
                    public void cancelled() {
                        callback.cancelled();
                    }
                });
            }
        }
        callback.completed(Boolean.TRUE);
        return Operations.nonCancellable();
    }
}

