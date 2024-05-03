/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.DisposalCallback;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Deadline;
import org.apache.hc.core5.util.TimeValue;

public final class PoolEntry<T, C extends ModalCloseable> {
    private final T route;
    private final TimeValue timeToLive;
    private final AtomicReference<C> connRef;
    private final DisposalCallback<C> disposalCallback;
    private final Supplier<Long> currentTimeSupplier;
    private volatile Object state;
    private volatile long created;
    private volatile long updated;
    private volatile Deadline expiryDeadline = Deadline.MIN_VALUE;
    private volatile Deadline validityDeadline = Deadline.MIN_VALUE;

    PoolEntry(T route, TimeValue timeToLive, DisposalCallback<C> disposalCallback, Supplier<Long> currentTimeSupplier) {
        this.route = Args.notNull(route, "Route");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.connRef = new AtomicReference<Object>(null);
        this.disposalCallback = disposalCallback;
        this.currentTimeSupplier = currentTimeSupplier;
    }

    PoolEntry(T route, TimeValue timeToLive, Supplier<Long> currentTimeSupplier) {
        this(route, timeToLive, null, currentTimeSupplier);
    }

    public PoolEntry(T route, TimeValue timeToLive, DisposalCallback<C> disposalCallback) {
        this(route, timeToLive, disposalCallback, null);
    }

    public PoolEntry(T route, TimeValue timeToLive) {
        this(route, timeToLive, null, null);
    }

    public PoolEntry(T route) {
        this(route, null);
    }

    long getCurrentTime() {
        return this.currentTimeSupplier != null ? this.currentTimeSupplier.get() : System.currentTimeMillis();
    }

    public T getRoute() {
        return this.route;
    }

    public C getConnection() {
        return (C)((ModalCloseable)this.connRef.get());
    }

    public Deadline getValidityDeadline() {
        return this.validityDeadline;
    }

    public Object getState() {
        return this.state;
    }

    public long getUpdated() {
        return this.updated;
    }

    public Deadline getExpiryDeadline() {
        return this.expiryDeadline;
    }

    public boolean hasConnection() {
        return this.connRef.get() != null;
    }

    public void assignConnection(C conn) {
        Args.notNull(conn, "connection");
        if (!this.connRef.compareAndSet(null, conn)) {
            throw new IllegalStateException("Connection already assigned");
        }
        this.updated = this.created = this.getCurrentTime();
        this.expiryDeadline = this.validityDeadline = Deadline.calculate(this.created, this.timeToLive);
        this.state = null;
    }

    public void discardConnection(CloseMode closeMode) {
        ModalCloseable connection = this.connRef.getAndSet(null);
        if (connection != null) {
            this.state = null;
            this.created = 0L;
            this.updated = 0L;
            this.expiryDeadline = Deadline.MIN_VALUE;
            this.validityDeadline = Deadline.MIN_VALUE;
            if (this.disposalCallback != null) {
                this.disposalCallback.execute(connection, closeMode);
            } else {
                connection.close(closeMode);
            }
        }
    }

    public void updateExpiry(TimeValue expiryTime) {
        Args.notNull(expiryTime, "Expiry time");
        long currentTime = this.getCurrentTime();
        Deadline newExpiry = Deadline.calculate(currentTime, expiryTime);
        this.expiryDeadline = newExpiry.min(this.validityDeadline);
        this.updated = currentTime;
    }

    public void updateState(Object state) {
        this.state = state;
        this.updated = this.getCurrentTime();
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[route:");
        buffer.append(this.route);
        buffer.append("][state:");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }
}

