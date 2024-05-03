/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

public class CacheConfig
implements Cloneable {
    public static final int DEFAULT_MAX_OBJECT_SIZE_BYTES = 8192;
    public static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;
    public static final int DEFAULT_MAX_UPDATE_RETRIES = 1;
    public static final boolean DEFAULT_303_CACHING_ENABLED = false;
    public static final boolean DEFAULT_WEAK_ETAG_ON_PUTDELETE_ALLOWED = false;
    public static final boolean DEFAULT_HEURISTIC_CACHING_ENABLED = false;
    public static final float DEFAULT_HEURISTIC_COEFFICIENT = 0.1f;
    public static final TimeValue DEFAULT_HEURISTIC_LIFETIME = TimeValue.ZERO_MILLISECONDS;
    public static final int DEFAULT_ASYNCHRONOUS_WORKERS = 1;
    public static final CacheConfig DEFAULT = new Builder().build();
    private final long maxObjectSize;
    private final int maxCacheEntries;
    private final int maxUpdateRetries;
    private final boolean allow303Caching;
    private final boolean weakETagOnPutDeleteAllowed;
    private final boolean heuristicCachingEnabled;
    private final float heuristicCoefficient;
    private final TimeValue heuristicDefaultLifetime;
    private final boolean sharedCache;
    private final boolean freshnessCheckEnabled;
    private final int asynchronousWorkers;
    private final boolean neverCacheHTTP10ResponsesWithQuery;

    CacheConfig(long maxObjectSize, int maxCacheEntries, int maxUpdateRetries, boolean allow303Caching, boolean weakETagOnPutDeleteAllowed, boolean heuristicCachingEnabled, float heuristicCoefficient, TimeValue heuristicDefaultLifetime, boolean sharedCache, boolean freshnessCheckEnabled, int asynchronousWorkers, boolean neverCacheHTTP10ResponsesWithQuery) {
        this.maxObjectSize = maxObjectSize;
        this.maxCacheEntries = maxCacheEntries;
        this.maxUpdateRetries = maxUpdateRetries;
        this.allow303Caching = allow303Caching;
        this.weakETagOnPutDeleteAllowed = weakETagOnPutDeleteAllowed;
        this.heuristicCachingEnabled = heuristicCachingEnabled;
        this.heuristicCoefficient = heuristicCoefficient;
        this.heuristicDefaultLifetime = heuristicDefaultLifetime;
        this.sharedCache = sharedCache;
        this.freshnessCheckEnabled = freshnessCheckEnabled;
        this.asynchronousWorkers = asynchronousWorkers;
        this.neverCacheHTTP10ResponsesWithQuery = neverCacheHTTP10ResponsesWithQuery;
    }

    public long getMaxObjectSize() {
        return this.maxObjectSize;
    }

    public boolean isNeverCacheHTTP10ResponsesWithQuery() {
        return this.neverCacheHTTP10ResponsesWithQuery;
    }

    public int getMaxCacheEntries() {
        return this.maxCacheEntries;
    }

    public int getMaxUpdateRetries() {
        return this.maxUpdateRetries;
    }

    public boolean is303CachingEnabled() {
        return this.allow303Caching;
    }

    public boolean isWeakETagOnPutDeleteAllowed() {
        return this.weakETagOnPutDeleteAllowed;
    }

    public boolean isHeuristicCachingEnabled() {
        return this.heuristicCachingEnabled;
    }

    public float getHeuristicCoefficient() {
        return this.heuristicCoefficient;
    }

    public TimeValue getHeuristicDefaultLifetime() {
        return this.heuristicDefaultLifetime;
    }

    public boolean isSharedCache() {
        return this.sharedCache;
    }

    public boolean isFreshnessCheckEnabled() {
        return this.freshnessCheckEnabled;
    }

    public int getAsynchronousWorkers() {
        return this.asynchronousWorkers;
    }

    protected CacheConfig clone() throws CloneNotSupportedException {
        return (CacheConfig)super.clone();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(CacheConfig config) {
        Args.notNull(config, "Cache config");
        return new Builder().setMaxObjectSize(config.getMaxObjectSize()).setMaxCacheEntries(config.getMaxCacheEntries()).setMaxUpdateRetries(config.getMaxUpdateRetries()).setHeuristicCachingEnabled(config.isHeuristicCachingEnabled()).setHeuristicCoefficient(config.getHeuristicCoefficient()).setHeuristicDefaultLifetime(config.getHeuristicDefaultLifetime()).setSharedCache(config.isSharedCache()).setAsynchronousWorkers(config.getAsynchronousWorkers()).setNeverCacheHTTP10ResponsesWithQueryString(config.isNeverCacheHTTP10ResponsesWithQuery());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[maxObjectSize=").append(this.maxObjectSize).append(", maxCacheEntries=").append(this.maxCacheEntries).append(", maxUpdateRetries=").append(this.maxUpdateRetries).append(", 303CachingEnabled=").append(this.allow303Caching).append(", weakETagOnPutDeleteAllowed=").append(this.weakETagOnPutDeleteAllowed).append(", heuristicCachingEnabled=").append(this.heuristicCachingEnabled).append(", heuristicCoefficient=").append(this.heuristicCoefficient).append(", heuristicDefaultLifetime=").append(this.heuristicDefaultLifetime).append(", sharedCache=").append(this.sharedCache).append(", freshnessCheckEnabled=").append(this.freshnessCheckEnabled).append(", asynchronousWorkers=").append(this.asynchronousWorkers).append(", neverCacheHTTP10ResponsesWithQuery=").append(this.neverCacheHTTP10ResponsesWithQuery).append("]");
        return builder.toString();
    }

    public static class Builder {
        private long maxObjectSize = 8192L;
        private int maxCacheEntries = 1000;
        private int maxUpdateRetries = 1;
        private boolean allow303Caching = false;
        private boolean weakETagOnPutDeleteAllowed = false;
        private boolean heuristicCachingEnabled = false;
        private float heuristicCoefficient = 0.1f;
        private TimeValue heuristicDefaultLifetime = DEFAULT_HEURISTIC_LIFETIME;
        private boolean sharedCache = true;
        private boolean freshnessCheckEnabled = true;
        private int asynchronousWorkers = 1;
        private boolean neverCacheHTTP10ResponsesWithQuery;

        Builder() {
        }

        public Builder setMaxObjectSize(long maxObjectSize) {
            this.maxObjectSize = maxObjectSize;
            return this;
        }

        public Builder setMaxCacheEntries(int maxCacheEntries) {
            this.maxCacheEntries = maxCacheEntries;
            return this;
        }

        public Builder setMaxUpdateRetries(int maxUpdateRetries) {
            this.maxUpdateRetries = maxUpdateRetries;
            return this;
        }

        public Builder setAllow303Caching(boolean allow303Caching) {
            this.allow303Caching = allow303Caching;
            return this;
        }

        public Builder setWeakETagOnPutDeleteAllowed(boolean weakETagOnPutDeleteAllowed) {
            this.weakETagOnPutDeleteAllowed = weakETagOnPutDeleteAllowed;
            return this;
        }

        public Builder setHeuristicCachingEnabled(boolean heuristicCachingEnabled) {
            this.heuristicCachingEnabled = heuristicCachingEnabled;
            return this;
        }

        public Builder setHeuristicCoefficient(float heuristicCoefficient) {
            this.heuristicCoefficient = heuristicCoefficient;
            return this;
        }

        public Builder setHeuristicDefaultLifetime(TimeValue heuristicDefaultLifetime) {
            this.heuristicDefaultLifetime = heuristicDefaultLifetime;
            return this;
        }

        public Builder setSharedCache(boolean sharedCache) {
            this.sharedCache = sharedCache;
            return this;
        }

        public Builder setAsynchronousWorkers(int asynchronousWorkers) {
            this.asynchronousWorkers = asynchronousWorkers;
            return this;
        }

        public Builder setNeverCacheHTTP10ResponsesWithQueryString(boolean neverCacheHTTP10ResponsesWithQuery) {
            this.neverCacheHTTP10ResponsesWithQuery = neverCacheHTTP10ResponsesWithQuery;
            return this;
        }

        public Builder setFreshnessCheckEnabled(boolean freshnessCheckEnabled) {
            this.freshnessCheckEnabled = freshnessCheckEnabled;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(this.maxObjectSize, this.maxCacheEntries, this.maxUpdateRetries, this.allow303Caching, this.weakETagOnPutDeleteAllowed, this.heuristicCachingEnabled, this.heuristicCoefficient, this.heuristicDefaultLifetime, this.sharedCache, this.freshnessCheckEnabled, this.asynchronousWorkers, this.neverCacheHTTP10ResponsesWithQuery);
        }
    }
}

