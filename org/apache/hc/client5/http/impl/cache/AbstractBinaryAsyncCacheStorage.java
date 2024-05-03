/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.impl.cache.AbstractSerializingAsyncCacheStorage;
import org.apache.hc.client5.http.impl.cache.ByteArrayCacheEntrySerializer;

public abstract class AbstractBinaryAsyncCacheStorage<CAS>
extends AbstractSerializingAsyncCacheStorage<byte[], CAS> {
    public AbstractBinaryAsyncCacheStorage(int maxUpdateRetries, HttpCacheEntrySerializer<byte[]> serializer) {
        super(maxUpdateRetries, serializer);
    }

    public AbstractBinaryAsyncCacheStorage(int maxUpdateRetries) {
        super(maxUpdateRetries, ByteArrayCacheEntrySerializer.INSTANCE);
    }
}

