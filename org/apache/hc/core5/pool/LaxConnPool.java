/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.DisposalCallback;
import org.apache.hc.core5.pool.ManagedConnPool;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.Deadline;
import org.apache.hc.core5.util.DeadlineTimeoutException;
import org.apache.hc.core5.util.LangUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE)
@Experimental
public class LaxConnPool<T, C extends ModalCloseable>
implements ManagedConnPool<T, C> {
    private final TimeValue timeToLive;
    private final PoolReusePolicy policy;
    private final DisposalCallback<C> disposalCallback;
    private final ConnPoolListener<T> connPoolListener;
    private final ConcurrentMap<T, PerRoutePool<T, C>> routeToPool;
    private final AtomicBoolean isShutDown;
    private volatile int defaultMaxPerRoute;

    public LaxConnPool(int defaultMaxPerRoute, TimeValue timeToLive, PoolReusePolicy policy, DisposalCallback<C> disposalCallback, ConnPoolListener<T> connPoolListener) {
        Args.positive(defaultMaxPerRoute, "Max per route value");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.policy = policy != null ? policy : PoolReusePolicy.LIFO;
        this.disposalCallback = disposalCallback;
        this.connPoolListener = connPoolListener;
        this.routeToPool = new ConcurrentHashMap<T, PerRoutePool<T, C>>();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public LaxConnPool(int defaultMaxPerRoute, TimeValue timeToLive, PoolReusePolicy policy, ConnPoolListener<T> connPoolListener) {
        this(defaultMaxPerRoute, timeToLive, policy, null, connPoolListener);
    }

    public LaxConnPool(int defaultMaxPerRoute) {
        this(defaultMaxPerRoute, TimeValue.NEG_ONE_MILLISECOND, PoolReusePolicy.LIFO, null, null);
    }

    public boolean isShutdown() {
        return this.isShutDown.get();
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.isShutDown.compareAndSet(false, true)) {
            for (PerRoutePool routePool : this.routeToPool.values()) {
                routePool.shutdown(closeMode);
            }
            this.routeToPool.clear();
        }
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    private PerRoutePool<T, C> getPool(T route) {
        PerRoutePool<T, C> newRoutePool;
        PerRoutePool<T, C> routePool = (PerRoutePool<T, C>)this.routeToPool.get(route);
        if (routePool == null && (routePool = this.routeToPool.putIfAbsent(route, newRoutePool = new PerRoutePool<T, C>(route, this.defaultMaxPerRoute, this.timeToLive, this.policy, this, this.disposalCallback, this.connPoolListener))) == null) {
            routePool = newRoutePool;
        }
        return routePool;
    }

    @Override
    public Future<PoolEntry<T, C>> lease(T route, Object state, Timeout requestTimeout, FutureCallback<PoolEntry<T, C>> callback) {
        Args.notNull(route, "Route");
        Asserts.check(!this.isShutDown.get(), "Connection pool shut down");
        PerRoutePool<T, C> routePool = this.getPool(route);
        return routePool.lease(state, requestTimeout, callback);
    }

    public Future<PoolEntry<T, C>> lease(T route, Object state) {
        return this.lease(route, state, Timeout.DISABLED, null);
    }

    @Override
    public void release(PoolEntry<T, C> entry, boolean reusable) {
        if (entry == null) {
            return;
        }
        if (this.isShutDown.get()) {
            return;
        }
        PerRoutePool<T, C> routePool = this.getPool(entry.getRoute());
        routePool.release(entry, reusable);
    }

    public void validatePendingRequests() {
        for (PerRoutePool routePool : this.routeToPool.values()) {
            routePool.validatePendingRequests();
        }
    }

    @Override
    public void setMaxTotal(int max) {
    }

    @Override
    public int getMaxTotal() {
        return 0;
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        Args.positive(max, "Max value");
        this.defaultMaxPerRoute = max;
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return this.defaultMaxPerRoute;
    }

    @Override
    public void setMaxPerRoute(T route, int max) {
        Args.notNull(route, "Route");
        PerRoutePool<T, C> routePool = this.getPool(route);
        routePool.setMax(max > -1 ? max : this.defaultMaxPerRoute);
    }

    @Override
    public int getMaxPerRoute(T route) {
        Args.notNull(route, "Route");
        PerRoutePool<T, C> routePool = this.getPool(route);
        return routePool.getMax();
    }

    @Override
    public PoolStats getTotalStats() {
        int leasedTotal = 0;
        int pendingTotal = 0;
        int availableTotal = 0;
        int maxTotal = 0;
        for (PerRoutePool routePool : this.routeToPool.values()) {
            leasedTotal += routePool.getLeasedCount();
            pendingTotal += routePool.getPendingCount();
            availableTotal += routePool.getAvailableCount();
            maxTotal += routePool.getMax();
        }
        return new PoolStats(leasedTotal, pendingTotal, availableTotal, maxTotal);
    }

    @Override
    public PoolStats getStats(T route) {
        Args.notNull(route, "Route");
        PerRoutePool<T, C> routePool = this.getPool(route);
        return new PoolStats(routePool.getLeasedCount(), routePool.getPendingCount(), routePool.getAvailableCount(), routePool.getMax());
    }

    @Override
    public Set<T> getRoutes() {
        return new HashSet(this.routeToPool.keySet());
    }

    public void enumAvailable(Callback<PoolEntry<T, C>> callback) {
        for (PerRoutePool routePool : this.routeToPool.values()) {
            routePool.enumAvailable(callback);
        }
    }

    public void enumLeased(Callback<PoolEntry<T, C>> callback) {
        for (PerRoutePool routePool : this.routeToPool.values()) {
            routePool.enumLeased(callback);
        }
    }

    @Override
    public void closeIdle(TimeValue idleTime) {
        final long deadline = System.currentTimeMillis() - (TimeValue.isPositive(idleTime) ? idleTime.toMilliseconds() : 0L);
        this.enumAvailable(new Callback<PoolEntry<T, C>>(){

            @Override
            public void execute(PoolEntry<T, C> entry) {
                if (entry.getUpdated() <= deadline) {
                    entry.discardConnection(CloseMode.GRACEFUL);
                }
            }
        });
    }

    @Override
    public void closeExpired() {
        final long now = System.currentTimeMillis();
        this.enumAvailable(new Callback<PoolEntry<T, C>>(){

            @Override
            public void execute(PoolEntry<T, C> entry) {
                if (entry.getExpiryDeadline().isBefore(now)) {
                    entry.discardConnection(CloseMode.GRACEFUL);
                }
            }
        });
    }

    public String toString() {
        PoolStats totalStats = this.getTotalStats();
        StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(totalStats.getLeased());
        buffer.append("][available: ");
        buffer.append(totalStats.getAvailable());
        buffer.append("][pending: ");
        buffer.append(totalStats.getPending());
        buffer.append("]");
        return buffer.toString();
    }

    static class PerRoutePool<T, C extends ModalCloseable> {
        private final T route;
        private final TimeValue timeToLive;
        private final PoolReusePolicy policy;
        private final DisposalCallback<C> disposalCallback;
        private final ConnPoolListener<T> connPoolListener;
        private final ConnPoolStats<T> connPoolStats;
        private final ConcurrentMap<PoolEntry<T, C>, Boolean> leased;
        private final Deque<AtomicMarkableReference<PoolEntry<T, C>>> available;
        private final Deque<LeaseRequest<T, C>> pending;
        private final AtomicBoolean terminated;
        private final AtomicInteger allocated;
        private final AtomicLong releaseSeqNum;
        private volatile int max;

        PerRoutePool(T route, int max, TimeValue timeToLive, PoolReusePolicy policy, ConnPoolStats<T> connPoolStats, DisposalCallback<C> disposalCallback, ConnPoolListener<T> connPoolListener) {
            this.route = route;
            this.timeToLive = timeToLive;
            this.policy = policy;
            this.connPoolStats = connPoolStats;
            this.disposalCallback = disposalCallback;
            this.connPoolListener = connPoolListener;
            this.leased = new ConcurrentHashMap<PoolEntry<T, C>, Boolean>();
            this.available = new ConcurrentLinkedDeque<AtomicMarkableReference<PoolEntry<T, C>>>();
            this.pending = new ConcurrentLinkedDeque<LeaseRequest<T, C>>();
            this.terminated = new AtomicBoolean(false);
            this.allocated = new AtomicInteger(0);
            this.releaseSeqNum = new AtomicLong(0L);
            this.max = max;
        }

        public void shutdown(CloseMode closeMode) {
            if (this.terminated.compareAndSet(false, true)) {
                LeaseRequest<T, C> leaseRequest;
                AtomicMarkableReference<PoolEntry<T, C>> entryRef;
                while ((entryRef = this.available.poll()) != null) {
                    entryRef.getReference().discardConnection(closeMode);
                }
                for (PoolEntry entry : this.leased.keySet()) {
                    entry.discardConnection(closeMode);
                }
                this.leased.clear();
                while ((leaseRequest = this.pending.poll()) != null) {
                    leaseRequest.cancel();
                }
            }
        }

        private PoolEntry<T, C> createPoolEntry() {
            int next;
            int prev;
            int poolmax = this.max;
            while (!this.allocated.compareAndSet(prev, next = (prev = this.allocated.get()) < poolmax ? prev + 1 : prev)) {
            }
            return prev < next ? new PoolEntry<T, C>(this.route, this.timeToLive, this.disposalCallback) : null;
        }

        private void deallocatePoolEntry() {
            this.allocated.decrementAndGet();
        }

        private void addLeased(PoolEntry<T, C> entry) {
            if (this.leased.putIfAbsent(entry, Boolean.TRUE) != null) {
                throw new IllegalStateException("Pool entry already present in the set of leased entries");
            }
            if (this.connPoolListener != null) {
                this.connPoolListener.onLease(this.route, this.connPoolStats);
            }
        }

        private void removeLeased(PoolEntry<T, C> entry) {
            if (this.connPoolListener != null) {
                this.connPoolListener.onRelease(this.route, this.connPoolStats);
            }
            if (!this.leased.remove(entry, Boolean.TRUE)) {
                throw new IllegalStateException("Pool entry is not present in the set of leased entries");
            }
        }

        private PoolEntry<T, C> getAvailableEntry(Object state) {
            Iterator<AtomicMarkableReference<PoolEntry<T, C>>> it = this.available.iterator();
            while (it.hasNext()) {
                PoolEntry<T, C> entry;
                AtomicMarkableReference<PoolEntry<T, C>> ref = it.next();
                if (!ref.compareAndSet(entry = ref.getReference(), entry, false, true)) continue;
                it.remove();
                if (entry.getExpiryDeadline().isExpired()) {
                    entry.discardConnection(CloseMode.GRACEFUL);
                }
                if (!LangUtils.equals(entry.getState(), state)) {
                    entry.discardConnection(CloseMode.GRACEFUL);
                }
                return entry;
            }
            return null;
        }

        public Future<PoolEntry<T, C>> lease(Object state, Timeout requestTimeout, FutureCallback<PoolEntry<T, C>> callback) {
            Asserts.check(!this.terminated.get(), "Connection pool shut down");
            BasicFuture<PoolEntry<PoolEntry<T, C>, C>> future = new BasicFuture<PoolEntry<PoolEntry<T, C>, C>>(callback);
            long releaseState = this.releaseSeqNum.get();
            PoolEntry<T, C> entry = null;
            if (this.pending.isEmpty() && (entry = this.getAvailableEntry(state)) == null) {
                entry = this.createPoolEntry();
            }
            if (entry != null) {
                this.addLeased(entry);
                future.completed(entry);
            } else {
                this.pending.add(new LeaseRequest<T, C>(state, requestTimeout, future));
                if (releaseState != this.releaseSeqNum.get()) {
                    this.servicePendingRequest();
                }
            }
            return future;
        }

        public void release(PoolEntry<T, C> releasedEntry, boolean reusable) {
            block6: {
                block5: {
                    this.removeLeased(releasedEntry);
                    if (!reusable || releasedEntry.getExpiryDeadline().isExpired()) {
                        releasedEntry.discardConnection(CloseMode.GRACEFUL);
                    }
                    if (!releasedEntry.hasConnection()) break block5;
                    switch (this.policy) {
                        case LIFO: {
                            this.available.addFirst(new AtomicMarkableReference<PoolEntry<T, C>>(releasedEntry, false));
                            break block6;
                        }
                        case FIFO: {
                            this.available.addLast(new AtomicMarkableReference<PoolEntry<T, C>>(releasedEntry, false));
                            break block6;
                        }
                        default: {
                            throw new IllegalStateException("Unexpected ConnPoolPolicy value: " + (Object)((Object)this.policy));
                        }
                    }
                }
                this.deallocatePoolEntry();
            }
            this.releaseSeqNum.incrementAndGet();
            this.servicePendingRequest();
        }

        private void servicePendingRequest() {
            this.servicePendingRequests(RequestServiceStrategy.FIRST_SUCCESSFUL);
        }

        private void servicePendingRequests(RequestServiceStrategy serviceStrategy) {
            LeaseRequest<T, C> leaseRequest;
            while ((leaseRequest = this.pending.poll()) != null) {
                if (leaseRequest.isDone()) continue;
                Object state = leaseRequest.getState();
                Deadline deadline = leaseRequest.getDeadline();
                if (deadline.isExpired()) {
                    leaseRequest.failed(DeadlineTimeoutException.from(deadline));
                    continue;
                }
                long releaseState = this.releaseSeqNum.get();
                PoolEntry<T, C> entry = this.getAvailableEntry(state);
                if (entry == null) {
                    entry = this.createPoolEntry();
                }
                if (entry != null) {
                    this.addLeased(entry);
                    if (!leaseRequest.completed(entry)) {
                        this.release(entry, true);
                    }
                    if (serviceStrategy != RequestServiceStrategy.FIRST_SUCCESSFUL) continue;
                    break;
                }
                this.pending.addFirst(leaseRequest);
                if (releaseState != this.releaseSeqNum.get()) continue;
                break;
            }
        }

        public void validatePendingRequests() {
            Iterator<LeaseRequest<T, C>> it = this.pending.iterator();
            while (it.hasNext()) {
                LeaseRequest<T, C> request = it.next();
                BasicFuture<PoolEntry<T, C>> future = request.getFuture();
                if (future.isCancelled() && !request.isDone()) {
                    it.remove();
                    continue;
                }
                Deadline deadline = request.getDeadline();
                if (deadline.isExpired()) {
                    request.failed(DeadlineTimeoutException.from(deadline));
                }
                if (!request.isDone()) continue;
                it.remove();
            }
        }

        public final T getRoute() {
            return this.route;
        }

        public int getMax() {
            return this.max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getPendingCount() {
            return this.pending.size();
        }

        public int getLeasedCount() {
            return this.leased.size();
        }

        public int getAvailableCount() {
            return this.available.size();
        }

        public void enumAvailable(Callback<PoolEntry<T, C>> callback) {
            Iterator<AtomicMarkableReference<PoolEntry<T, C>>> it = this.available.iterator();
            while (it.hasNext()) {
                PoolEntry<T, C> entry;
                AtomicMarkableReference<PoolEntry<T, C>> ref = it.next();
                if (!ref.compareAndSet(entry = ref.getReference(), entry, false, true)) continue;
                callback.execute(entry);
                if (!entry.hasConnection()) {
                    this.deallocatePoolEntry();
                    it.remove();
                    continue;
                }
                ref.set(entry, false);
            }
            this.releaseSeqNum.incrementAndGet();
            this.servicePendingRequests(RequestServiceStrategy.ALL);
        }

        public void enumLeased(Callback<PoolEntry<T, C>> callback) {
            Iterator it = this.leased.keySet().iterator();
            while (it.hasNext()) {
                PoolEntry entry = (PoolEntry)it.next();
                callback.execute(entry);
                if (entry.hasConnection()) continue;
                this.deallocatePoolEntry();
                it.remove();
            }
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[route: ");
            buffer.append(this.route);
            buffer.append("][leased: ");
            buffer.append(this.leased.size());
            buffer.append("][available: ");
            buffer.append(this.available.size());
            buffer.append("][pending: ");
            buffer.append(this.pending.size());
            buffer.append("]");
            return buffer.toString();
        }

        private static enum RequestServiceStrategy {
            FIRST_SUCCESSFUL,
            ALL;

        }
    }

    static class LeaseRequest<T, C extends ModalCloseable>
    implements Cancellable {
        private final Object state;
        private final Deadline deadline;
        private final BasicFuture<PoolEntry<T, C>> future;

        LeaseRequest(Object state, Timeout requestTimeout, BasicFuture<PoolEntry<T, C>> future) {
            this.state = state;
            this.deadline = Deadline.calculate(requestTimeout);
            this.future = future;
        }

        BasicFuture<PoolEntry<T, C>> getFuture() {
            return this.future;
        }

        public Object getState() {
            return this.state;
        }

        public Deadline getDeadline() {
            return this.deadline;
        }

        public boolean isDone() {
            return this.future.isDone();
        }

        public boolean completed(PoolEntry<T, C> result) {
            return this.future.completed(result);
        }

        public boolean failed(Exception ex) {
            return this.future.failed(ex);
        }

        @Override
        public boolean cancel() {
            return this.future.cancel();
        }
    }
}

