/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.util.Collection;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheUpdateException;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.SAFE)
public interface HttpCacheStorage {
    public void putEntry(String var1, HttpCacheEntry var2) throws ResourceIOException;

    public HttpCacheEntry getEntry(String var1) throws ResourceIOException;

    public void removeEntry(String var1) throws ResourceIOException;

    public void updateEntry(String var1, HttpCacheCASOperation var2) throws ResourceIOException, HttpCacheUpdateException;

    public Map<String, HttpCacheEntry> getEntries(Collection<String> var1) throws ResourceIOException;
}

