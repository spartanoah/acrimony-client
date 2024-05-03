/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.ConnPoolListener;
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
public class StrictConnPool<T, C extends ModalCloseable>
implements ManagedConnPool<T, C> {
    private final TimeValue timeToLive;
    private final PoolReusePolicy policy;
    private final DisposalCallback<C> disposalCallback;
    private final ConnPoolListener<T> connPoolListener;
    private final Map<T, PerRoutePool<T, C>> routeToPool;
    private final LinkedList<LeaseRequest<T, C>> leasingRequests;
    private final Set<PoolEntry<T, C>> leased;
    private final LinkedList<PoolEntry<T, C>> available;
    private final ConcurrentLinkedQueue<LeaseRequest<T, C>> completedRequests;
    private final Map<T, Integer> maxPerRoute;
    private final Lock lock;
    private final AtomicBoolean isShutDown;
    private volatile int defaultMaxPerRoute;
    private volatile int maxTotal;

    public StrictConnPool(int defaultMaxPerRoute, int maxTotal, TimeValue timeToLive, PoolReusePolicy policy, DisposalCallback<C> disposalCallback, ConnPoolListener<T> connPoolListener) {
        Args.positive(defaultMaxPerRoute, "Max per route value");
        Args.positive(maxTotal, "Max total value");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.policy = policy != null ? policy : PoolReusePolicy.LIFO;
        this.disposalCallback = disposalCallback;
        this.connPoolListener = connPoolListener;
        this.routeToPool = new HashMap<T, PerRoutePool<T, C>>();
        this.leasingRequests = new LinkedList();
        this.leased = new HashSet<PoolEntry<T, C>>();
        this.available = new LinkedList();
        this.completedRequests = new ConcurrentLinkedQueue();
        this.maxPerRoute = new HashMap<T, Integer>();
        this.lock = new ReentrantLock();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.maxTotal = maxTotal;
    }

    public StrictConnPool(int defaultMaxPerRoute, int maxTotal, TimeValue timeToLive, PoolReusePolicy policy, ConnPoolListener<T> connPoolListener) {
        this(defaultMaxPerRoute, maxTotal, timeToLive, policy, null, connPoolListener);
    }

    public StrictConnPool(int defaultMaxPerRoute, int maxTotal) {
        this(defaultMaxPerRoute, maxTotal, TimeValue.NEG_ONE_MILLISECOND, PoolReusePolicy.LIFO, null);
    }

    public boolean isShutdown() {
        return this.isShutDown.get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close(CloseMode closeMode) {
        if (this.isShutDown.compareAndSet(false, true)) {
            this.fireCallbacks();
            this.lock.lock();
            try {
                for (PerRoutePool<T, C> pool : this.routeToPool.values()) {
                    pool.shutdown(closeMode);
                }
                this.routeToPool.clear();
                this.leased.clear();
                this.available.clear();
                this.leasingRequests.clear();
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    private PerRoutePool<T, C> getPool(T route) {
        PerRoutePool<T, C> pool = this.routeToPool.get(route);
        if (pool == null) {
            pool = new PerRoutePool<T, C>(route, this.disposalCallback);
            this.routeToPool.put(route, pool);
        }
        return pool;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Future<PoolEntry<T, C>> lease(T route, Object state, Timeout requestTimeout, FutureCallback<PoolEntry<T, C>> callback) {
        boolean acquiredLock;
        Args.notNull(route, "Route");
        Args.notNull(requestTimeout, "Request timeout");
        Asserts.check(!this.isShutDown.get(), "Connection pool shut down");
        Deadline deadline = Deadline.calculate(requestTimeout);
        BasicFuture<PoolEntry<T, C>> future = new BasicFuture<PoolEntry<T, C>>(callback);
        try {
            acquiredLock = this.lock.tryLock(requestTimeout.getDuration(), requestTimeout.getTimeUnit());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            future.cancel();
            return future;
        }
        if (acquiredLock) {
            try {
                LeaseRequest<T, C> request = new LeaseRequest<T, C>(route, state, requestTimeout, future);
                boolean completed = this.processPendingRequest(request);
                if (!request.isDone() && !completed) {
                    this.leasingRequests.add(request);
                }
                if (request.isDone()) {
                    this.completedRequests.add(request);
                }
            } finally {
                this.lock.unlock();
            }
            this.fireCallbacks();
        } else {
            future.failed(DeadlineTimeoutException.from(deadline));
        }
        return future;
    }

    public Future<PoolEntry<T, C>> lease(T route, Object state) {
        return this.lease(route, state, Timeout.DISABLED, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void release(PoolEntry<T, C> entry, boolean reusable) {
        block11: {
            if (entry == null) {
                return;
            }
            if (this.isShutDown.get()) {
                return;
            }
            if (!reusable) {
                entry.discardConnection(CloseMode.GRACEFUL);
            }
            this.lock.lock();
            try {
                block12: {
                    block14: {
                        block13: {
                            if (!this.leased.remove(entry)) break block12;
                            if (this.connPoolListener != null) {
                                this.connPoolListener.onRelease(entry.getRoute(), this);
                            }
                            PerRoutePool<T, C> pool = this.getPool(entry.getRoute());
                            boolean keepAlive = entry.hasConnection() && reusable;
                            pool.free(entry, keepAlive);
                            if (!keepAlive) break block13;
                            switch (this.policy) {
                                case LIFO: {
                                    this.available.addFirst(entry);
                                    break block14;
                                }
                                case FIFO: {
                                    this.available.addLast(entry);
                                    break block14;
                                }
                                default: {
                                    throw new IllegalStateException("Unexpected ConnPoolPolicy value: " + (Object)((Object)this.policy));
                                }
                            }
                        }
                        entry.discardConnection(CloseMode.GRACEFUL);
                    }
                    this.processNextPendingRequest();
                    break block11;
                }
                throw new IllegalStateException("Pool entry is not present in the set of leased entries");
            } finally {
                this.lock.unlock();
            }
        }
        this.fireCallbacks();
    }

    private void processPendingRequests() {
        ListIterator it = this.leasingRequests.listIterator();
        while (it.hasNext()) {
            LeaseRequest request = (LeaseRequest)it.next();
            BasicFuture future = request.getFuture();
            if (future.isCancelled()) {
                it.remove();
                continue;
            }
            boolean completed = this.processPendingRequest(request);
            if (request.isDone() || completed) {
                it.remove();
            }
            if (!request.isDone()) continue;
            this.completedRequests.add(request);
        }
    }

    private void processNextPendingRequest() {
        ListIterator it = this.leasingRequests.listIterator();
        while (it.hasNext()) {
            LeaseRequest request = (LeaseRequest)it.next();
            BasicFuture future = request.getFuture();
            if (future.isCancelled()) {
                it.remove();
                continue;
            }
            boolean completed = this.processPendingRequest(request);
            if (request.isDone() || completed) {
                it.remove();
            }
            if (request.isDone()) {
                this.completedRequests.add(request);
            }
            if (!completed) continue;
            return;
        }
    }

    private boolean processPendingRequest(LeaseRequest<T, C> request) {
        PoolEntry<T, C> entry;
        T route = request.getRoute();
        Object state = request.getState();
        Deadline deadline = request.getDeadline();
        if (deadline.isExpired()) {
            request.failed(DeadlineTimeoutException.from(deadline));
            return false;
        }
        PerRoutePool<T, C> pool = this.getPool(route);
        while ((entry = pool.getFree(state)) != null && entry.getExpiryDeadline().isExpired()) {
            entry.discardConnection(CloseMode.GRACEFUL);
            this.available.remove(entry);
            pool.free(entry, false);
        }
        if (entry != null) {
            this.available.remove(entry);
            this.leased.add(entry);
            request.completed(entry);
            if (this.connPoolListener != null) {
                this.connPoolListener.onLease(entry.getRoute(), this);
            }
            return true;
        }
        int maxPerRoute = this.getMax(route);
        int excess = Math.max(0, pool.getAllocatedCount() + 1 - maxPerRoute);
        if (excess > 0) {
            PoolEntry<T, C> lastUsed;
            for (int i = 0; i < excess && (lastUsed = pool.getLastUsed()) != null; ++i) {
                lastUsed.discardConnection(CloseMode.GRACEFUL);
                this.available.remove(lastUsed);
                pool.remove(lastUsed);
            }
        }
        if (pool.getAllocatedCount() < maxPerRoute) {
            int freeCapacity = Math.max(this.maxTotal - this.leased.size(), 0);
            if (freeCapacity == 0) {
                return false;
            }
            int totalAvailable = this.available.size();
            if (totalAvailable > freeCapacity - 1 && !this.available.isEmpty()) {
                PoolEntry<T, C> lastUsed = this.available.removeLast();
                lastUsed.discardConnection(CloseMode.GRACEFUL);
                PerRoutePool<T, C> otherpool = this.getPool(lastUsed.getRoute());
                otherpool.remove(lastUsed);
            }
            entry = pool.createEntry(this.timeToLive);
            this.leased.add(entry);
            request.completed(entry);
            if (this.connPoolListener != null) {
                this.connPoolListener.onLease(entry.getRoute(), this);
            }
            return true;
        }
        return false;
    }

    private void fireCallbacks() {
        LeaseRequest<T, C> request;
        while ((request = this.completedRequests.poll()) != null) {
            BasicFuture<PoolEntry<PoolEntry<T, C>, C>> future = request.getFuture();
            Exception ex = request.getException();
            PoolEntry<T, C> result = request.getResult();
            boolean successfullyCompleted = false;
            if (ex != null) {
                future.failed(ex);
            } else if (result != null) {
                if (future.completed(result)) {
                    successfullyCompleted = true;
                }
            } else {
                future.cancel();
            }
            if (successfullyCompleted) continue;
            this.release(result, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void validatePendingRequests() {
        this.lock.lock();
        try {
            long now = System.currentTimeMillis();
            ListIterator it = this.leasingRequests.listIterator();
            while (it.hasNext()) {
                LeaseRequest request = (LeaseRequest)it.next();
                BasicFuture future = request.getFuture();
                if (future.isCancelled() && !request.isDone()) {
                    it.remove();
                    continue;
                }
                Deadline deadline = request.getDeadline();
                if (deadline.isBefore(now)) {
                    request.failed(DeadlineTimeoutException.from(deadline));
                }
                if (!request.isDone()) continue;
                it.remove();
                this.completedRequests.add(request);
            }
        } finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    private int getMax(T route) {
        Integer v = this.maxPerRoute.get(route);
        if (v != null) {
            return v;
        }
        return this.defaultMaxPerRoute;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaxTotal(int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.maxTotal = max;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxTotal() {
        this.lock.lock();
        try {
            int n = this.maxTotal;
            return n;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDefaultMaxPerRoute(int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.defaultMaxPerRoute = max;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getDefaultMaxPerRoute() {
        this.lock.lock();
        try {
            int n = this.defaultMaxPerRoute;
            return n;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaxPerRoute(T route, int max) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            if (max > -1) {
                this.maxPerRoute.put(route, max);
            } else {
                this.maxPerRoute.remove(route);
            }
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxPerRoute(T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            int n = this.getMax(route);
            return n;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PoolStats getTotalStats() {
        this.lock.lock();
        try {
            PoolStats poolStats = new PoolStats(this.leased.size(), this.leasingRequests.size(), this.available.size(), this.maxTotal);
            return poolStats;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PoolStats getStats(T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            PerRoutePool<T, C> pool = this.getPool(route);
            int pendingCount = 0;
            for (LeaseRequest leaseRequest : this.leasingRequests) {
                if (!LangUtils.equals(route, leaseRequest.getRoute())) continue;
                ++pendingCount;
            }
            PoolStats poolStats = new PoolStats(pool.getLeasedCount(), pendingCount, pool.getAvailableCount(), this.getMax(route));
            return poolStats;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Set<T> getRoutes() {
        this.lock.lock();
        try {
            HashSet<T> hashSet = new HashSet<T>(this.routeToPool.keySet());
            return hashSet;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void enumAvailable(Callback<PoolEntry<T, C>> callback) {
        this.lock.lock();
        try {
            Iterator it = this.available.iterator();
            while (it.hasNext()) {
                PoolEntry entry = (PoolEntry)it.next();
                callback.execute(entry);
                if (entry.hasConnection()) continue;
                PerRoutePool pool = this.getPool(entry.getRoute());
                pool.remove(entry);
                it.remove();
            }
            this.processPendingRequests();
            this.purgePoolMap();
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void enumLeased(Callback<PoolEntry<T, C>> callback) {
        this.lock.lock();
        try {
            for (PoolEntry<T, C> entry : this.leased) {
                callback.execute(entry);
            }
            this.processPendingRequests();
        } finally {
            this.lock.unlock();
        }
    }

    private void purgePoolMap() {
        Iterator<Map.Entry<T, PerRoutePool<T, C>>> it = this.routeToPool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, PerRoutePool<T, C>> entry = it.next();
            PerRoutePool<T, C> pool = entry.getValue();
            if (pool.getAllocatedCount() != 0) continue;
            it.remove();
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
        StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased.size());
        buffer.append("][available: ");
        buffer.append(this.available.size());
        buffer.append("][pending: ");
        buffer.append(this.leasingRequests.size());
        buffer.append("]");
        return buffer.toString();
    }

    static class PerRoutePool<T, C extends ModalCloseable> {
        private final T route;
        private final Set<PoolEntry<T, C>> leased;
        private final LinkedList<PoolEntry<T, C>> available;
        private final DisposalCallback<C> disposalCallback;

        PerRoutePool(T route, DisposalCallback<C> disposalCallback) {
            this.route = route;
            this.disposalCallback = disposalCallback;
            this.leased = new HashSet<PoolEntry<T, C>>();
            this.available = new LinkedList();
        }

        public final T getRoute() {
            return this.route;
        }

        public int getLeasedCount() {
            return this.leased.size();
        }

        public int getAvailableCount() {
            return this.available.size();
        }

        public int getAllocatedCount() {
            return this.available.size() + this.leased.size();
        }

        public PoolEntry<T, C> getFree(Object state) {
            if (!this.available.isEmpty()) {
                PoolEntry entry;
                Iterator it;
                if (state != null) {
                    it = this.available.iterator();
                    while (it.hasNext()) {
                        entry = (PoolEntry)it.next();
                        if (!state.equals(entry.getState())) continue;
                        it.remove();
                        this.leased.add(entry);
                        return entry;
                    }
                }
                it = this.available.iterator();
                while (it.hasNext()) {
                    entry = (PoolEntry)it.next();
                    if (entry.getState() != null) continue;
                    it.remove();
                    this.leased.add(entry);
                    return entry;
                }
            }
            return null;
        }

        public PoolEntry<T, C> getLastUsed() {
            return this.available.peekLast();
        }

        public boolean remove(PoolEntry<T, C> entry) {
            return this.available.remove(entry) || this.leased.remove(entry);
        }

        public void free(PoolEntry<T, C> entry, boolean reusable) {
            boolean found = this.leased.remove(entry);
            Asserts.check(found, "Entry %s has not been leased from this pool", entry);
            if (reusable) {
                this.available.addFirst(entry);
            }
        }

        public PoolEntry<T, C> createEntry(TimeValue timeToLive) {
            PoolEntry<T, C> entry = new PoolEntry<T, C>(this.route, timeToLive, this.disposalCallback);
            this.leased.add(entry);
            return entry;
        }

        public void shutdown(CloseMode closeMode) {
            PoolEntry<T, C> availableEntry;
            while ((availableEntry = this.available.poll()) != null) {
                availableEntry.discardConnection(closeMode);
            }
            for (PoolEntry<T, C> entry : this.leased) {
                entry.discardConnection(closeMode);
            }
            this.leased.clear();
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[route: ");
            buffer.append(this.route);
            buffer.append("][leased: ");
            buffer.append(this.leased.size());
            buffer.append("][available: ");
            buffer.append(this.available.size());
            buffer.append("]");
            return buffer.toString();
        }
    }

    static class LeaseRequest<T, C extends ModalCloseable> {
        private final T route;
        private final Object state;
        private final Deadline deadline;
        private final BasicFuture<PoolEntry<T, C>> future;
        private final AtomicBoolean completed;
        private volatile PoolEntry<T, C> result;
        private volatile Exception ex;

        public LeaseRequest(T route, Object state, Timeout requestTimeout, BasicFuture<PoolEntry<T, C>> future) {
            this.route = route;
            this.state = state;
            this.deadline = Deadline.calculate(requestTimeout);
            this.future = future;
            this.completed = new AtomicBoolean(false);
        }

        public T getRoute() {
            return this.route;
        }

        public Object getState() {
            return this.state;
        }

        public Deadline getDeadline() {
            return this.deadline;
        }

        public boolean isDone() {
            return this.completed.get();
        }

        public void failed(Exception ex) {
            if (this.completed.compareAndSet(false, true)) {
                this.ex = ex;
            }
        }

        public void completed(PoolEntry<T, C> result) {
            if (this.completed.compareAndSet(false, true)) {
                this.result = result;
            }
        }

        public BasicFuture<PoolEntry<T, C>> getFuture() {
            return this.future;
        }

        public PoolEntry<T, C> getResult() {
            return this.result;
        }

        public Exception getException() {
            return this.ex;
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[");
            buffer.append(this.route);
            buffer.append("][");
            buffer.append(this.state);
            buffer.append("]");
            return buffer.toString();
        }
    }
}

