/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.io.Serializable;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class HttpCacheStorageEntry
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final HttpCacheEntry content;

    public HttpCacheStorageEntry(String key, HttpCacheEntry content) {
        this.key = key;
        this.content = Args.notNull(content, "Cache entry");
    }

    public String getKey() {
        return this.key;
    }

    public HttpCacheEntry getContent() {
        return this.content;
    }

    public String toString() {
        return "[key=" + this.key + "; content=" + this.content + "]";
    }
}

