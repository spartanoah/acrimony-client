/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class AbstractIOSessionPool<T>
implements ModalCloseable {
    private final ConcurrentMap<T, PoolEntry> sessionPool = new ConcurrentHashMap<T, PoolEntry>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    protected abstract Future<IOSession> connectSession(T var1, Timeout var2, FutureCallback<IOSession> var3);

    protected abstract void validateSession(IOSession var1, Callback<Boolean> var2);

    protected abstract void closeSession(IOSession var1, CloseMode var2);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void close(CloseMode closeMode) {
        if (this.closed.compareAndSet(false, true)) {
            Iterator i$ = this.sessionPool.values().iterator();
            while (i$.hasNext()) {
                PoolEntry poolEntry;
                PoolEntry poolEntry2 = poolEntry = (PoolEntry)i$.next();
                synchronized (poolEntry2) {
                    FutureCallback<IOSession> callback;
                    if (poolEntry.session != null) {
                        this.closeSession(poolEntry.session, closeMode);
                        poolEntry.session = null;
                    }
                    if (poolEntry.sessionFuture != null) {
                        poolEntry.sessionFuture.cancel(true);
                        poolEntry.sessionFuture = null;
                    }
                    while ((callback = poolEntry.requestQueue.poll()) != null) {
                        callback.cancelled();
                    }
                }
            }
            this.sessionPool.clear();
        }
    }

    @Override
    public final void close() {
        this.close(CloseMode.GRACEFUL);
    }

    PoolEntry getPoolEntry(T endpoint) {
        PoolEntry newPoolEntry;
        PoolEntry poolEntry = (PoolEntry)this.sessionPool.get(endpoint);
        if (poolEntry == null && (poolEntry = this.sessionPool.putIfAbsent(endpoint, newPoolEntry = new PoolEntry())) == null) {
            poolEntry = newPoolEntry;
        }
        return poolEntry;
    }

    public final Future<IOSession> getSession(final T endpoint, final Timeout connectTimeout, FutureCallback<IOSession> callback) {
        Args.notNull(endpoint, "Endpoint");
        Asserts.check(!this.closed.get(), "Connection pool shut down");
        final ComplexFuture<IOSession> future = new ComplexFuture<IOSession>(callback);
        final PoolEntry poolEntry = this.getPoolEntry(endpoint);
        this.getSessionInternal(poolEntry, false, endpoint, connectTimeout, new FutureCallback<IOSession>(){

            @Override
            public void completed(final IOSession ioSession) {
                AbstractIOSessionPool.this.validateSession(ioSession, new Callback<Boolean>(){

                    @Override
                    public void execute(Boolean result) {
                        if (result.booleanValue()) {
                            future.completed(ioSession);
                        } else {
                            AbstractIOSessionPool.this.getSessionInternal(poolEntry, true, endpoint, connectTimeout, new FutureCallback<IOSession>(){

                                @Override
                                public void completed(IOSession ioSession) {
                                    future.completed(ioSession);
                                }

                                @Override
                                public void failed(Exception ex) {
                                    future.failed(ex);
                                }

                                @Override
                                public void cancelled() {
                                    future.cancel();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void failed(Exception ex) {
                future.failed(ex);
            }

            @Override
            public void cancelled() {
                future.cancel();
            }
        });
        return future;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getSessionInternal(final PoolEntry poolEntry, boolean requestNew, T namedEndpoint, Timeout connectTimeout, FutureCallback<IOSession> callback) {
        PoolEntry poolEntry2 = poolEntry;
        synchronized (poolEntry2) {
            if (poolEntry.session != null && requestNew) {
                this.closeSession(poolEntry.session, CloseMode.GRACEFUL);
                poolEntry.session = null;
            }
            if (poolEntry.session != null && !poolEntry.session.isOpen()) {
                poolEntry.session = null;
            }
            if (poolEntry.session != null) {
                callback.completed(poolEntry.session);
            } else {
                poolEntry.requestQueue.add(callback);
                if (poolEntry.sessionFuture == null) {
                    poolEntry.sessionFuture = this.connectSession(namedEndpoint, connectTimeout, new FutureCallback<IOSession>(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void completed(IOSession result) {
                            PoolEntry poolEntry2 = poolEntry;
                            synchronized (poolEntry2) {
                                FutureCallback<IOSession> callback;
                                poolEntry.session = result;
                                poolEntry.sessionFuture = null;
                                while ((callback = poolEntry.requestQueue.poll()) != null) {
                                    callback.completed(result);
                                }
                            }
                        }

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void failed(Exception ex) {
                            PoolEntry poolEntry2 = poolEntry;
                            synchronized (poolEntry2) {
                                FutureCallback<IOSession> callback;
                                poolEntry.session = null;
                                poolEntry.sessionFuture = null;
                                while ((callback = poolEntry.requestQueue.poll()) != null) {
                                    callback.failed(ex);
                                }
                            }
                        }

                        @Override
                        public void cancelled() {
                            this.failed(new ConnectionClosedException("Connection request cancelled"));
                        }
                    });
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void enumAvailable(Callback<IOSession> callback) {
        for (PoolEntry poolEntry : this.sessionPool.values()) {
            if (poolEntry.session == null) continue;
            PoolEntry poolEntry2 = poolEntry;
            synchronized (poolEntry2) {
                if (poolEntry.session != null) {
                    callback.execute(poolEntry.session);
                    if (!poolEntry.session.isOpen()) {
                        poolEntry.session = null;
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void closeIdle(TimeValue idleTime) {
        long deadline = System.currentTimeMillis() - (TimeValue.isPositive(idleTime) ? idleTime.toMilliseconds() : 0L);
        for (PoolEntry poolEntry : this.sessionPool.values()) {
            if (poolEntry.session == null) continue;
            PoolEntry poolEntry2 = poolEntry;
            synchronized (poolEntry2) {
                if (poolEntry.session != null && poolEntry.session.getLastReadTime() <= deadline) {
                    this.closeSession(poolEntry.session, CloseMode.GRACEFUL);
                    poolEntry.session = null;
                }
            }
        }
    }

    public final Set<T> getRoutes() {
        return new HashSet(this.sessionPool.keySet());
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("I/O sessions: ");
        buffer.append(this.sessionPool.size());
        return buffer.toString();
    }

    static class PoolEntry {
        final Queue<FutureCallback<IOSession>> requestQueue = new ArrayDeque<FutureCallback<IOSession>>();
        volatile Future<IOSession> sessionFuture;
        volatile IOSession session;

        PoolEntry() {
        }
    }
}

