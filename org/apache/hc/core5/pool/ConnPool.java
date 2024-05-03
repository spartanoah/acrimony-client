/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.util.Timeout;

public interface ConnPool<T, C extends ModalCloseable> {
    public Future<PoolEntry<T, C>> lease(T var1, Object var2, Timeout var3, FutureCallback<PoolEntry<T, C>> var4);

    public void release(PoolEntry<T, C> var1, boolean var2);
}

