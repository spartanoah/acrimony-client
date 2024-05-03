/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.cache.HttpAsyncCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorageAdaptor;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.cache.AsyncCachingExec;
import org.apache.hc.client5.http.impl.cache.BasicHttpAsyncCache;
import org.apache.hc.client5.http.impl.cache.BasicHttpCacheStorage;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheKeyGenerator;
import org.apache.hc.client5.http.impl.cache.DefaultAsyncCacheInvalidator;
import org.apache.hc.client5.http.impl.cache.DefaultAsyncCacheRevalidator;
import org.apache.hc.client5.http.impl.cache.FileResourceFactory;
import org.apache.hc.client5.http.impl.cache.HeapResourceFactory;
import org.apache.hc.client5.http.impl.cache.ManagedHttpCacheStorage;
import org.apache.hc.client5.http.impl.schedule.ImmediateSchedulingStrategy;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.http.config.NamedElementChain;

@Experimental
public class CachingHttpAsyncClientBuilder
extends HttpAsyncClientBuilder {
    private ResourceFactory resourceFactory;
    private HttpAsyncCacheStorage storage;
    private File cacheDir;
    private SchedulingStrategy schedulingStrategy;
    private CacheConfig cacheConfig;
    private HttpAsyncCacheInvalidator httpCacheInvalidator;
    private boolean deleteCache = true;

    public static CachingHttpAsyncClientBuilder create() {
        return new CachingHttpAsyncClientBuilder();
    }

    protected CachingHttpAsyncClientBuilder() {
    }

    public final CachingHttpAsyncClientBuilder setResourceFactory(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setHttpCacheStorage(HttpCacheStorage storage) {
        this.storage = storage != null ? new HttpAsyncCacheStorageAdaptor(storage) : null;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setHttpCacheStorage(HttpAsyncCacheStorage storage) {
        this.storage = storage;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    public final CachingHttpAsyncClientBuilder setHttpCacheInvalidator(HttpAsyncCacheInvalidator cacheInvalidator) {
        this.httpCacheInvalidator = cacheInvalidator;
        return this;
    }

    public CachingHttpAsyncClientBuilder setDeleteCache(boolean deleteCache) {
        this.deleteCache = deleteCache;
        return this;
    }

    @Override
    protected void customizeExecChain(NamedElementChain<AsyncExecChainHandler> execChainDefinition) {
        HttpAsyncCacheStorage storageCopy;
        CacheConfig config = this.cacheConfig != null ? this.cacheConfig : CacheConfig.DEFAULT;
        ResourceFactory resourceFactoryCopy = this.resourceFactory;
        if (resourceFactoryCopy == null) {
            resourceFactoryCopy = this.cacheDir == null ? new HeapResourceFactory() : new FileResourceFactory(this.cacheDir);
        }
        if ((storageCopy = this.storage) == null) {
            if (this.cacheDir == null) {
                storageCopy = new HttpAsyncCacheStorageAdaptor(new BasicHttpCacheStorage(config));
            } else {
                final ManagedHttpCacheStorage managedStorage = new ManagedHttpCacheStorage(config);
                if (this.deleteCache) {
                    this.addCloseable(new Closeable(){

                        @Override
                        public void close() throws IOException {
                            managedStorage.shutdown();
                        }
                    });
                } else {
                    this.addCloseable(managedStorage);
                }
                storageCopy = new HttpAsyncCacheStorageAdaptor(managedStorage);
            }
        }
        BasicHttpAsyncCache httpCache = new BasicHttpAsyncCache(resourceFactoryCopy, storageCopy, CacheKeyGenerator.INSTANCE, this.httpCacheInvalidator != null ? this.httpCacheInvalidator : new DefaultAsyncCacheInvalidator());
        DefaultAsyncCacheRevalidator cacheRevalidator = null;
        if (config.getAsynchronousWorkers() > 0) {
            final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(config.getAsynchronousWorkers());
            this.addCloseable(new Closeable(){

                @Override
                public void close() throws IOException {
                    executorService.shutdownNow();
                }
            });
            cacheRevalidator = new DefaultAsyncCacheRevalidator((ScheduledExecutorService)executorService, this.schedulingStrategy != null ? this.schedulingStrategy : ImmediateSchedulingStrategy.INSTANCE);
        }
        AsyncCachingExec cachingExec = new AsyncCachingExec(httpCache, cacheRevalidator, config);
        execChainDefinition.addBefore(ChainElement.PROTOCOL.name(), cachingExec, ChainElement.CACHING.name());
    }
}

