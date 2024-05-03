/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.pool;

import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReadOnlyIterator;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractChannelPoolMap<K, P extends ChannelPool>
implements ChannelPoolMap<K, P>,
Iterable<Map.Entry<K, P>>,
Closeable {
    private final ConcurrentMap<K, P> map = PlatformDependent.newConcurrentHashMap();

    @Override
    public final P get(K key) {
        ChannelPool old;
        ChannelPool pool = (ChannelPool)this.map.get(ObjectUtil.checkNotNull(key, "key"));
        if (pool == null && (old = this.map.putIfAbsent(key, pool = this.newPool(key))) != null) {
            AbstractChannelPoolMap.poolCloseAsyncIfSupported(pool);
            pool = old;
        }
        return (P)pool;
    }

    public final boolean remove(K key) {
        ChannelPool pool = (ChannelPool)this.map.remove(ObjectUtil.checkNotNull(key, "key"));
        if (pool != null) {
            AbstractChannelPoolMap.poolCloseAsyncIfSupported(pool);
            return true;
        }
        return false;
    }

    private Future<Boolean> removeAsyncIfSupported(K key) {
        ChannelPool pool = (ChannelPool)this.map.remove(ObjectUtil.checkNotNull(key, "key"));
        if (pool != null) {
            final Promise<Boolean> removePromise = GlobalEventExecutor.INSTANCE.newPromise();
            AbstractChannelPoolMap.poolCloseAsyncIfSupported(pool).addListener((GenericFutureListener<Future<Void>>)new GenericFutureListener<Future<? super Void>>(){

                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        removePromise.setSuccess(Boolean.TRUE);
                    } else {
                        removePromise.setFailure(future.cause());
                    }
                }
            });
            return removePromise;
        }
        return GlobalEventExecutor.INSTANCE.newSucceededFuture(Boolean.FALSE);
    }

    private static Future<Void> poolCloseAsyncIfSupported(ChannelPool pool) {
        if (pool instanceof SimpleChannelPool) {
            return ((SimpleChannelPool)pool).closeAsync();
        }
        try {
            pool.close();
            return GlobalEventExecutor.INSTANCE.newSucceededFuture(null);
        } catch (Exception e) {
            return GlobalEventExecutor.INSTANCE.newFailedFuture(e);
        }
    }

    @Override
    public final Iterator<Map.Entry<K, P>> iterator() {
        return new ReadOnlyIterator<Map.Entry<K, P>>(this.map.entrySet().iterator());
    }

    public final int size() {
        return this.map.size();
    }

    public final boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public final boolean contains(K key) {
        return this.map.containsKey(ObjectUtil.checkNotNull(key, "key"));
    }

    protected abstract P newPool(K var1);

    @Override
    public final void close() {
        for (Object key : this.map.keySet()) {
            this.removeAsyncIfSupported(key).syncUninterruptibly();
        }
    }
}

