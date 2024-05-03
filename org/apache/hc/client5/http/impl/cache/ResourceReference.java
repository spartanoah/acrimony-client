/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.core5.util.Args;

class ResourceReference
extends PhantomReference<HttpCacheEntry> {
    private final Resource resource;

    public ResourceReference(HttpCacheEntry entry, ReferenceQueue<HttpCacheEntry> q) {
        super(entry, q);
        Args.notNull(entry.getResource(), "Resource");
        this.resource = entry.getResource();
    }

    public Resource getResource() {
        return this.resource;
    }

    public int hashCode() {
        return this.resource.hashCode();
    }

    public boolean equals(Object obj) {
        return this.resource.equals(obj);
    }
}

