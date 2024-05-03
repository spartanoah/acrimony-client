/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.schedule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public final class ConcurrentCountMap<T> {
    private final ConcurrentMap<T, AtomicInteger> map = new ConcurrentHashMap<T, AtomicInteger>();

    public int getCount(T identifier) {
        Args.notNull(identifier, "Identifier");
        AtomicInteger count = (AtomicInteger)this.map.get(identifier);
        return count != null ? count.get() : 0;
    }

    public void resetCount(T identifier) {
        Args.notNull(identifier, "Identifier");
        this.map.remove(identifier);
    }

    public int increaseCount(T identifier) {
        Args.notNull(identifier, "Identifier");
        AtomicInteger count = this.get(identifier);
        return count.incrementAndGet();
    }

    private AtomicInteger get(T identifier) {
        AtomicInteger newEntry;
        AtomicInteger entry = (AtomicInteger)this.map.get(identifier);
        if (entry == null && (entry = this.map.putIfAbsent(identifier, newEntry = new AtomicInteger())) == null) {
            entry = newEntry;
        }
        return entry;
    }
}

