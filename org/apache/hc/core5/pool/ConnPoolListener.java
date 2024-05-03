/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.pool.ConnPoolStats;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ConnPoolListener<T> {
    public void onLease(T var1, ConnPoolStats<T> var2);

    public void onRelease(T var1, ConnPoolStats<T> var2);
}

