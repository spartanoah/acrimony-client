/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public class NoopCacheEntrySerializer
implements HttpCacheEntrySerializer<HttpCacheStorageEntry> {
    public static final NoopCacheEntrySerializer INSTANCE = new NoopCacheEntrySerializer();

    @Override
    public HttpCacheStorageEntry serialize(HttpCacheStorageEntry cacheEntry) throws ResourceIOException {
        return cacheEntry;
    }

    @Override
    public HttpCacheStorageEntry deserialize(HttpCacheStorageEntry cacheEntry) throws ResourceIOException {
        return cacheEntry;
    }
}

