/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.hc.client5.http.cache.HttpAsyncCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheUpdateException;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.impl.cache.CacheKeyGenerator;
import org.apache.hc.client5.http.impl.cache.CacheUpdateHandler;
import org.apache.hc.client5.http.impl.cache.DefaultAsyncCacheInvalidator;
import org.apache.hc.client5.http.impl.cache.HeapResourceFactory;
import org.apache.hc.client5.http.impl.cache.HttpAsyncCache;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexCancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BasicHttpAsyncCache
implements HttpAsyncCache {
    private static final Logger LOG = LoggerFactory.getLogger(BasicHttpAsyncCache.class);
    private final CacheUpdateHandler cacheUpdateHandler;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final HttpAsyncCacheInvalidator cacheInvalidator;
    private final HttpAsyncCacheStorage storage;

    public BasicHttpAsyncCache(ResourceFactory resourceFactory, HttpAsyncCacheStorage storage, CacheKeyGenerator cacheKeyGenerator, HttpAsyncCacheInvalidator cacheInvalidator) {
        this.cacheUpdateHandler = new CacheUpdateHandler(resourceFactory);
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.storage = storage;
        this.cacheInvalidator = cacheInvalidator;
    }

    public BasicHttpAsyncCache(ResourceFactory resourceFactory, HttpAsyncCacheStorage storage, CacheKeyGenerator cacheKeyGenerator) {
        this(resourceFactory, storage, cacheKeyGenerator, DefaultAsyncCacheInvalidator.INSTANCE);
    }

    public BasicHttpAsyncCache(ResourceFactory resourceFactory, HttpAsyncCacheStorage storage) {
        this(resourceFactory, storage, CacheKeyGenerator.INSTANCE);
    }

    @Override
    public String generateKey(HttpHost host, HttpRequest request, HttpCacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return this.cacheKeyGenerator.generateKey(host, request);
        }
        return this.cacheKeyGenerator.generateKey(host, request, cacheEntry);
    }

    @Override
    public Cancellable flushCacheEntriesFor(HttpHost host, HttpRequest request, final FutureCallback<Boolean> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flush cache entries: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        if (!Method.isSafe(request.getMethod())) {
            final String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
            return this.storage.removeEntry(cacheKey, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                    callback.completed(result);
                }

                @Override
                public void failed(Exception ex) {
                    if (ex instanceof ResourceIOException) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("I/O error removing cache entry with key {}", (Object)cacheKey);
                        }
                        callback.completed(Boolean.TRUE);
                    } else {
                        callback.failed(ex);
                    }
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }
            });
        }
        callback.completed(Boolean.TRUE);
        return Operations.nonCancellable();
    }

    @Override
    public Cancellable flushCacheEntriesInvalidatedByRequest(HttpHost host, HttpRequest request, FutureCallback<Boolean> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flush cache entries invalidated by request: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        return this.cacheInvalidator.flushCacheEntriesInvalidatedByRequest(host, request, this.cacheKeyGenerator, this.storage, callback);
    }

    @Override
    public Cancellable flushCacheEntriesInvalidatedByExchange(HttpHost host, HttpRequest request, HttpResponse response, FutureCallback<Boolean> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flush cache entries invalidated by exchange: {}; {} -> {}", host, new RequestLine(request), new StatusLine(response));
        }
        if (!Method.isSafe(request.getMethod())) {
            return this.cacheInvalidator.flushCacheEntriesInvalidatedByExchange(host, request, response, this.cacheKeyGenerator, this.storage, callback);
        }
        callback.completed(Boolean.TRUE);
        return Operations.nonCancellable();
    }

    Cancellable storeInCache(String cacheKey, HttpHost host, HttpRequest request, HttpCacheEntry entry, FutureCallback<Boolean> callback) {
        if (entry.hasVariants()) {
            return this.storeVariantEntry(cacheKey, host, request, entry, callback);
        }
        return this.storeEntry(cacheKey, entry, callback);
    }

    Cancellable storeEntry(final String cacheKey, HttpCacheEntry entry, final FutureCallback<Boolean> callback) {
        return this.storage.putEntry(cacheKey, entry, new FutureCallback<Boolean>(){

            @Override
            public void completed(Boolean result) {
                callback.completed(result);
            }

            @Override
            public void failed(Exception ex) {
                if (ex instanceof ResourceIOException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error storing cache entry with key {}", (Object)cacheKey);
                    }
                    callback.completed(Boolean.TRUE);
                } else {
                    callback.failed(ex);
                }
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }

    Cancellable storeVariantEntry(final String cacheKey, HttpHost host, final HttpRequest req, final HttpCacheEntry entry, final FutureCallback<Boolean> callback) {
        final String variantKey = this.cacheKeyGenerator.generateVariantKey(req, entry);
        final String variantCacheKey = this.cacheKeyGenerator.generateKey(host, req, entry);
        return this.storage.putEntry(variantCacheKey, entry, new FutureCallback<Boolean>(){

            @Override
            public void completed(Boolean result) {
                BasicHttpAsyncCache.this.storage.updateEntry(cacheKey, new HttpCacheCASOperation(){

                    @Override
                    public HttpCacheEntry execute(HttpCacheEntry existing) throws ResourceIOException {
                        return BasicHttpAsyncCache.this.cacheUpdateHandler.updateParentCacheEntry(req.getRequestUri(), existing, entry, variantKey, variantCacheKey);
                    }
                }, new FutureCallback<Boolean>(){

                    @Override
                    public void completed(Boolean result) {
                        callback.completed(result);
                    }

                    @Override
                    public void failed(Exception ex) {
                        if (ex instanceof HttpCacheUpdateException) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Cannot update cache entry with key {}", (Object)cacheKey);
                            }
                        } else if (ex instanceof ResourceIOException) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
                            }
                        } else {
                            callback.failed(ex);
                        }
                    }

                    @Override
                    public void cancelled() {
                        callback.cancelled();
                    }
                });
            }

            @Override
            public void failed(Exception ex) {
                if (ex instanceof ResourceIOException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error updating cache entry with key {}", (Object)variantCacheKey);
                    }
                    callback.completed(Boolean.TRUE);
                } else {
                    callback.failed(ex);
                }
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }

    @Override
    public Cancellable reuseVariantEntryFor(HttpHost host, final HttpRequest request, Variant variant, final FutureCallback<Boolean> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Re-use variant entry: {}; {} / {}", host, new RequestLine(request), variant);
        }
        final String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        final HttpCacheEntry entry = variant.getEntry();
        final String variantKey = this.cacheKeyGenerator.generateVariantKey(request, entry);
        final String variantCacheKey = variant.getCacheKey();
        return this.storage.updateEntry(cacheKey, new HttpCacheCASOperation(){

            @Override
            public HttpCacheEntry execute(HttpCacheEntry existing) throws ResourceIOException {
                return BasicHttpAsyncCache.this.cacheUpdateHandler.updateParentCacheEntry(request.getRequestUri(), existing, entry, variantKey, variantCacheKey);
            }
        }, new FutureCallback<Boolean>(){

            @Override
            public void completed(Boolean result) {
                callback.completed(result);
            }

            @Override
            public void failed(Exception ex) {
                if (ex instanceof HttpCacheUpdateException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Cannot update cache entry with key {}", (Object)cacheKey);
                    }
                } else if (ex instanceof ResourceIOException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
                    }
                } else {
                    callback.failed(ex);
                }
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }

    @Override
    public Cancellable updateCacheEntry(HttpHost host, HttpRequest request, HttpCacheEntry stale, HttpResponse originResponse, Date requestSent, Date responseReceived, final FutureCallback<HttpCacheEntry> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            final HttpCacheEntry updatedEntry = this.cacheUpdateHandler.updateCacheEntry(request.getRequestUri(), stale, requestSent, responseReceived, originResponse);
            return this.storeInCache(cacheKey, host, request, updatedEntry, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                    callback.completed(updatedEntry);
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
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
            callback.completed(stale);
            return Operations.nonCancellable();
        }
    }

    @Override
    public Cancellable updateVariantCacheEntry(HttpHost host, HttpRequest request, HttpResponse originResponse, Variant variant, Date requestSent, Date responseReceived, final FutureCallback<HttpCacheEntry> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update variant cache entry: {}; {} / {}", host, new RequestLine(request), variant);
        }
        HttpCacheEntry entry = variant.getEntry();
        String cacheKey = variant.getCacheKey();
        try {
            final HttpCacheEntry updatedEntry = this.cacheUpdateHandler.updateCacheEntry(request.getRequestUri(), entry, requestSent, responseReceived, originResponse);
            return this.storeEntry(cacheKey, updatedEntry, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                    callback.completed(updatedEntry);
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
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
            callback.completed(entry);
            return Operations.nonCancellable();
        }
    }

    @Override
    public Cancellable createCacheEntry(HttpHost host, HttpRequest request, HttpResponse originResponse, ByteArrayBuffer content, Date requestSent, Date responseReceived, final FutureCallback<HttpCacheEntry> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            final HttpCacheEntry entry = this.cacheUpdateHandler.createtCacheEntry(request, originResponse, content, requestSent, responseReceived);
            return this.storeInCache(cacheKey, host, request, entry, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                    callback.completed(entry);
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
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error creating cache entry with key {}", (Object)cacheKey);
            }
            callback.completed(new HttpCacheEntry(requestSent, responseReceived, originResponse.getCode(), originResponse.getHeaders(), content != null ? HeapResourceFactory.INSTANCE.generate(null, content.array(), 0, content.length()) : null));
            return Operations.nonCancellable();
        }
    }

    @Override
    public Cancellable getCacheEntry(HttpHost host, final HttpRequest request, final FutureCallback<HttpCacheEntry> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        final ComplexCancellable complexCancellable = new ComplexCancellable();
        final String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        complexCancellable.setDependency(this.storage.getEntry(cacheKey, new FutureCallback<HttpCacheEntry>(){

            @Override
            public void completed(HttpCacheEntry root) {
                if (root != null && root.hasVariants()) {
                    String variantKey = BasicHttpAsyncCache.this.cacheKeyGenerator.generateVariantKey(request, root);
                    final String variantCacheKey = root.getVariantMap().get(variantKey);
                    if (variantCacheKey != null) {
                        complexCancellable.setDependency(BasicHttpAsyncCache.this.storage.getEntry(variantCacheKey, new FutureCallback<HttpCacheEntry>(){

                            @Override
                            public void completed(HttpCacheEntry result) {
                                callback.completed(result);
                            }

                            @Override
                            public void failed(Exception ex) {
                                if (ex instanceof ResourceIOException) {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn("I/O error retrieving cache entry with key {}", (Object)variantCacheKey);
                                    }
                                    callback.completed(null);
                                } else {
                                    callback.failed(ex);
                                }
                            }

                            @Override
                            public void cancelled() {
                                callback.cancelled();
                            }
                        }));
                        return;
                    }
                }
                callback.completed(root);
            }

            @Override
            public void failed(Exception ex) {
                if (ex instanceof ResourceIOException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error retrieving cache entry with key {}", (Object)cacheKey);
                    }
                    callback.completed(null);
                } else {
                    callback.failed(ex);
                }
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        }));
        return complexCancellable;
    }

    @Override
    public Cancellable getVariantCacheEntriesWithEtags(HttpHost host, HttpRequest request, final FutureCallback<Map<String, Variant>> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get variant cache entries: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        final ComplexCancellable complexCancellable = new ComplexCancellable();
        final String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        final HashMap variants = new HashMap();
        complexCancellable.setDependency(this.storage.getEntry(cacheKey, new FutureCallback<HttpCacheEntry>(){

            @Override
            public void completed(HttpCacheEntry rootEntry) {
                if (rootEntry != null && rootEntry.hasVariants()) {
                    final Set<String> variantCacheKeys = rootEntry.getVariantMap().keySet();
                    complexCancellable.setDependency(BasicHttpAsyncCache.this.storage.getEntries(variantCacheKeys, new FutureCallback<Map<String, HttpCacheEntry>>(){

                        @Override
                        public void completed(Map<String, HttpCacheEntry> resultMap) {
                            for (Map.Entry<String, HttpCacheEntry> resultMapEntry : resultMap.entrySet()) {
                                String cacheKey = resultMapEntry.getKey();
                                HttpCacheEntry cacheEntry = resultMapEntry.getValue();
                                Header etagHeader = cacheEntry.getFirstHeader("ETag");
                                if (etagHeader == null) continue;
                                variants.put(etagHeader.getValue(), new Variant(cacheKey, cacheEntry));
                            }
                            callback.completed(variants);
                        }

                        @Override
                        public void failed(Exception ex) {
                            if (ex instanceof ResourceIOException) {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn("I/O error retrieving cache entry with keys {}", (Object)variantCacheKeys);
                                }
                                callback.completed(variants);
                            } else {
                                callback.failed(ex);
                            }
                        }

                        @Override
                        public void cancelled() {
                            callback.cancelled();
                        }
                    }));
                } else {
                    callback.completed(variants);
                }
            }

            @Override
            public void failed(Exception ex) {
                if (ex instanceof ResourceIOException) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error retrieving cache entry with key {}", (Object)cacheKey);
                    }
                    callback.completed(variants);
                } else {
                    callback.failed(ex);
                }
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        }));
        return complexCancellable;
    }
}

