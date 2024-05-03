/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface HttpCacheEntrySerializer<T> {
    public T serialize(HttpCacheStorageEntry var1) throws ResourceIOException;

    public HttpCacheStorageEntry deserialize(T var1) throws ResourceIOException;
}

