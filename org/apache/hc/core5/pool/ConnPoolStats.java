/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import org.apache.hc.core5.pool.PoolStats;

public interface ConnPoolStats<T> {
    public PoolStats getTotalStats();

    public PoolStats getStats(T var1);
}

