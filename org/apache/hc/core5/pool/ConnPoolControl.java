/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import java.util.Set;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.util.TimeValue;

public interface ConnPoolControl<T>
extends ConnPoolStats<T> {
    public void setMaxTotal(int var1);

    public int getMaxTotal();

    public void setDefaultMaxPerRoute(int var1);

    public int getDefaultMaxPerRoute();

    public void setMaxPerRoute(T var1, int var2);

    public int getMaxPerRoute(T var1);

    public void closeIdle(TimeValue var1);

    public void closeExpired();

    public Set<T> getRoutes();
}

