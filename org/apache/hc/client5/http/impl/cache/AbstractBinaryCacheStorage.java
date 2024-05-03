/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.impl.cache.AbstractSerializingCacheStorage;
import org.apache.hc.client5.http.impl.cache.ByteArrayCacheEntrySerializer;

public abstract class AbstractBinaryCacheStorage<CAS>
extends AbstractSerializingCacheStorage<byte[], CAS> {
    public AbstractBinaryCacheStorage(int maxUpdateRetries, HttpCacheEntrySerializer<byte[]> serializer) {
        super(maxUpdateRetries, serializer);
    }

    public AbstractBinaryCacheStorage(int maxUpdateRetries) {
        super(maxUpdateRetries, ByteArrayCacheEntrySerializer.INSTANCE);
    }
}

