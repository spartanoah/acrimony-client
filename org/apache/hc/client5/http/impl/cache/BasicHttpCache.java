/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheUpdateException;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.BasicHttpCacheStorage;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheKeyGenerator;
import org.apache.hc.client5.http.impl.cache.CacheUpdateHandler;
import org.apache.hc.client5.http.impl.cache.DefaultCacheInvalidator;
import org.apache.hc.client5.http.impl.cache.HeapResourceFactory;
import org.apache.hc.client5.http.impl.cache.HttpCache;
import org.apache.hc.client5.http.impl.cache.Variant;
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

class BasicHttpCache
implements HttpCache {
    private static final Logger LOG = LoggerFactory.getLogger(BasicHttpCache.class);
    private final CacheUpdateHandler cacheUpdateHandler;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final HttpCacheInvalidator cacheInvalidator;
    private final HttpCacheStorage storage;

    public BasicHttpCache(ResourceFactory resourceFactory, HttpCacheStorage storage, CacheKeyGenerator cacheKeyGenerator, HttpCacheInvalidator cacheInvalidator) {
        this.cacheUpdateHandler = new CacheUpdateHandler(resourceFactory);
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.storage = storage;
        this.cacheInvalidator = cacheInvalidator;
    }

    public BasicHttpCache(ResourceFactory resourceFactory, HttpCacheStorage storage, CacheKeyGenerator cacheKeyGenerator) {
        this(resourceFactory, storage, cacheKeyGenerator, new DefaultCacheInvalidator());
    }

    public BasicHttpCache(ResourceFactory resourceFactory, HttpCacheStorage storage) {
        this(resourceFactory, storage, new CacheKeyGenerator());
    }

    public BasicHttpCache(CacheConfig config) {
        this(new HeapResourceFactory(), new BasicHttpCacheStorage(config));
    }

    public BasicHttpCache() {
        this(CacheConfig.DEFAULT);
    }

    @Override
    public String generateKey(HttpHost host, HttpRequest request, HttpCacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return this.cacheKeyGenerator.generateKey(host, request);
        }
        return this.cacheKeyGenerator.generateKey(host, request, cacheEntry);
    }

    @Override
    public void flushCacheEntriesFor(HttpHost host, HttpRequest request) {
        block4: {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flush cache entries: {}; {}", (Object)host, (Object)new RequestLine(request));
            }
            if (!Method.isSafe(request.getMethod())) {
                String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
                try {
                    this.storage.removeEntry(cacheKey);
                } catch (ResourceIOException ex) {
                    if (!LOG.isWarnEnabled()) break block4;
                    LOG.warn("I/O error removing cache entry with key {}", (Object)cacheKey);
                }
            }
        }
    }

    @Override
    public void flushCacheEntriesInvalidatedByRequest(HttpHost host, HttpRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flush cache entries invalidated by request: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        this.cacheInvalidator.flushCacheEntriesInvalidatedByRequest(host, request, this.cacheKeyGenerator, this.storage);
    }

    @Override
    public void flushCacheEntriesInvalidatedByExchange(HttpHost host, HttpRequest request, HttpResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flush cache entries invalidated by exchange: {}; {} -> {}", host, new RequestLine(request), new StatusLine(response));
        }
        if (!Method.isSafe(request.getMethod())) {
            this.cacheInvalidator.flushCacheEntriesInvalidatedByExchange(host, request, response, this.cacheKeyGenerator, this.storage);
        }
    }

    void storeInCache(String cacheKey, HttpHost host, HttpRequest request, HttpCacheEntry entry) {
        if (entry.hasVariants()) {
            this.storeVariantEntry(cacheKey, host, request, entry);
        } else {
            this.storeEntry(cacheKey, entry);
        }
    }

    void storeEntry(String cacheKey, HttpCacheEntry entry) {
        block2: {
            try {
                this.storage.putEntry(cacheKey, entry);
            } catch (ResourceIOException ex) {
                if (!LOG.isWarnEnabled()) break block2;
                LOG.warn("I/O error storing cache entry with key {}", (Object)cacheKey);
            }
        }
    }

    void storeVariantEntry(String cacheKey, HttpHost host, final HttpRequest req, final HttpCacheEntry entry) {
        block4: {
            final String variantKey = this.cacheKeyGenerator.generateVariantKey(req, entry);
            final String variantCacheKey = this.cacheKeyGenerator.generateKey(host, req, entry);
            this.storeEntry(variantCacheKey, entry);
            try {
                this.storage.updateEntry(cacheKey, new HttpCacheCASOperation(){

                    @Override
                    public HttpCacheEntry execute(HttpCacheEntry existing) throws ResourceIOException {
                        return BasicHttpCache.this.cacheUpdateHandler.updateParentCacheEntry(req.getRequestUri(), existing, entry, variantKey, variantCacheKey);
                    }
                });
            } catch (HttpCacheUpdateException ex) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Cannot update cache entry with key {}", (Object)cacheKey);
                }
            } catch (ResourceIOException ex) {
                if (!LOG.isWarnEnabled()) break block4;
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
        }
    }

    @Override
    public void reuseVariantEntryFor(HttpHost host, final HttpRequest request, Variant variant) {
        block5: {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Re-use variant entry: {}; {} / {}", host, new RequestLine(request), variant);
            }
            String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
            final HttpCacheEntry entry = variant.getEntry();
            final String variantKey = this.cacheKeyGenerator.generateVariantKey(request, entry);
            final String variantCacheKey = variant.getCacheKey();
            try {
                this.storage.updateEntry(cacheKey, new HttpCacheCASOperation(){

                    @Override
                    public HttpCacheEntry execute(HttpCacheEntry existing) throws ResourceIOException {
                        return BasicHttpCache.this.cacheUpdateHandler.updateParentCacheEntry(request.getRequestUri(), existing, entry, variantKey, variantCacheKey);
                    }
                });
            } catch (HttpCacheUpdateException ex) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Cannot update cache entry with key {}", (Object)cacheKey);
                }
            } catch (ResourceIOException ex) {
                if (!LOG.isWarnEnabled()) break block5;
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
        }
    }

    @Override
    public HttpCacheEntry updateCacheEntry(HttpHost host, HttpRequest request, HttpCacheEntry stale, HttpResponse originResponse, Date requestSent, Date responseReceived) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            HttpCacheEntry updatedEntry = this.cacheUpdateHandler.updateCacheEntry(request.getRequestUri(), stale, requestSent, responseReceived, originResponse);
            this.storeInCache(cacheKey, host, request, updatedEntry);
            return updatedEntry;
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
            return stale;
        }
    }

    @Override
    public HttpCacheEntry updateVariantCacheEntry(HttpHost host, HttpRequest request, HttpResponse originResponse, Variant variant, Date requestSent, Date responseReceived) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update variant cache entry: {}; {} / {}", host, new RequestLine(request), variant);
        }
        HttpCacheEntry entry = variant.getEntry();
        String cacheKey = variant.getCacheKey();
        try {
            HttpCacheEntry updatedEntry = this.cacheUpdateHandler.updateCacheEntry(request.getRequestUri(), entry, requestSent, responseReceived, originResponse);
            this.storeEntry(cacheKey, updatedEntry);
            return updatedEntry;
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error updating cache entry with key {}", (Object)cacheKey);
            }
            return entry;
        }
    }

    @Override
    public HttpCacheEntry createCacheEntry(HttpHost host, HttpRequest request, HttpResponse originResponse, ByteArrayBuffer content, Date requestSent, Date responseReceived) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            HttpCacheEntry entry = this.cacheUpdateHandler.createtCacheEntry(request, originResponse, content, requestSent, responseReceived);
            this.storeInCache(cacheKey, host, request, entry);
            return entry;
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error creating cache entry with key {}", (Object)cacheKey);
            }
            return new HttpCacheEntry(requestSent, responseReceived, originResponse.getCode(), originResponse.getHeaders(), content != null ? HeapResourceFactory.INSTANCE.generate(null, content.array(), 0, content.length()) : null);
        }
    }

    @Override
    public HttpCacheEntry getCacheEntry(HttpHost host, HttpRequest request) {
        HttpCacheEntry root;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get cache entry: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            root = this.storage.getEntry(cacheKey);
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error retrieving cache entry with key {}", (Object)cacheKey);
            }
            return null;
        }
        if (root == null) {
            return null;
        }
        if (!root.hasVariants()) {
            return root;
        }
        String variantKey = this.cacheKeyGenerator.generateVariantKey(request, root);
        String variantCacheKey = root.getVariantMap().get(variantKey);
        if (variantCacheKey == null) {
            return null;
        }
        try {
            return this.storage.getEntry(variantCacheKey);
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error retrieving cache entry with key {}", (Object)variantCacheKey);
            }
            return null;
        }
    }

    @Override
    public Map<String, Variant> getVariantCacheEntriesWithEtags(HttpHost host, HttpRequest request) {
        HttpCacheEntry root;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get variant cache entries: {}; {}", (Object)host, (Object)new RequestLine(request));
        }
        HashMap<String, Variant> variants = new HashMap<String, Variant>();
        String cacheKey = this.cacheKeyGenerator.generateKey(host, request);
        try {
            root = this.storage.getEntry(cacheKey);
        } catch (ResourceIOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error retrieving cache entry with key {}", (Object)cacheKey);
            }
            return variants;
        }
        if (root != null && root.hasVariants()) {
            for (Map.Entry<String, String> variant : root.getVariantMap().entrySet()) {
                String variantCacheKey = variant.getValue();
                try {
                    Header etagHeader;
                    HttpCacheEntry entry = this.storage.getEntry(variantCacheKey);
                    if (entry == null || (etagHeader = entry.getFirstHeader("ETag")) == null) continue;
                    variants.put(etagHeader.getValue(), new Variant(variantCacheKey, entry));
                } catch (ResourceIOException ex) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("I/O error retrieving cache entry with key {}", (Object)variantCacheKey);
                    }
                    return variants;
                }
            }
        }
        return variants;
    }
}

