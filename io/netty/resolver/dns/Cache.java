/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class Cache<E> {
    private static final AtomicReferenceFieldUpdater<Entries, ScheduledFuture> FUTURE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(Entries.class, ScheduledFuture.class, "expirationFuture");
    private static final ScheduledFuture<?> CANCELLED = new ScheduledFuture<Object>(){

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return Long.MIN_VALUE;
        }

        @Override
        public int compareTo(Delayed o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    };
    static final int MAX_SUPPORTED_TTL_SECS = (int)TimeUnit.DAYS.toSeconds(730L);
    private final ConcurrentMap<String, Entries> resolveCache = PlatformDependent.newConcurrentHashMap();

    Cache() {
    }

    final void clear() {
        while (!this.resolveCache.isEmpty()) {
            Iterator i = this.resolveCache.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = i.next();
                i.remove();
                ((Entries)e.getValue()).clearAndCancel();
            }
        }
    }

    final boolean clear(String hostname) {
        Entries entries = (Entries)this.resolveCache.remove(hostname);
        return entries != null && entries.clearAndCancel();
    }

    final List<? extends E> get(String hostname) {
        Entries entries = (Entries)this.resolveCache.get(hostname);
        return entries == null ? null : (List)entries.get();
    }

    final void cache(String hostname, E value, int ttl, EventLoop loop) {
        Entries oldEntries;
        Entries entries = (Entries)this.resolveCache.get(hostname);
        if (entries == null && (oldEntries = this.resolveCache.putIfAbsent(hostname, entries = new Entries(hostname))) != null) {
            entries = oldEntries;
        }
        entries.add(value, ttl, loop);
    }

    final int size() {
        return this.resolveCache.size();
    }

    protected abstract boolean shouldReplaceAll(E var1);

    protected void sortEntries(String hostname, List<E> entries) {
    }

    protected abstract boolean equals(E var1, E var2);

    private final class Entries
    extends AtomicReference<List<E>>
    implements Runnable {
        private final String hostname;
        volatile ScheduledFuture<?> expirationFuture;

        Entries(String hostname) {
            super(Collections.emptyList());
            this.hostname = hostname;
        }

        void add(E e, int ttl, EventLoop loop) {
            if (!Cache.this.shouldReplaceAll(e)) {
                while (true) {
                    List entries;
                    if (!(entries = (List)this.get()).isEmpty()) {
                        Object firstEntry = entries.get(0);
                        if (Cache.this.shouldReplaceAll(firstEntry)) {
                            assert (entries.size() == 1);
                            if (!this.compareAndSet(entries, Collections.singletonList(e))) continue;
                            this.scheduleCacheExpirationIfNeeded(ttl, loop);
                            return;
                        }
                        ArrayList newEntries = new ArrayList(entries.size() + 1);
                        int i = 0;
                        Object replacedEntry = null;
                        do {
                            Object entry;
                            if (!Cache.this.equals(e, entry = entries.get(i))) {
                                newEntries.add(entry);
                                continue;
                            }
                            replacedEntry = entry;
                            newEntries.add(e);
                            ++i;
                            while (i < entries.size()) {
                                newEntries.add(entries.get(i));
                                ++i;
                            }
                            break;
                        } while (++i < entries.size());
                        if (replacedEntry == null) {
                            newEntries.add(e);
                        }
                        Cache.this.sortEntries(this.hostname, newEntries);
                        if (!this.compareAndSet(entries, Collections.unmodifiableList(newEntries))) continue;
                        this.scheduleCacheExpirationIfNeeded(ttl, loop);
                        return;
                    }
                    if (this.compareAndSet(entries, Collections.singletonList(e))) break;
                }
                this.scheduleCacheExpirationIfNeeded(ttl, loop);
                return;
            }
            this.set(Collections.singletonList(e));
            this.scheduleCacheExpirationIfNeeded(ttl, loop);
        }

        private void scheduleCacheExpirationIfNeeded(int ttl, EventLoop loop) {
            ScheduledFuture oldFuture;
            while ((oldFuture = (ScheduledFuture)FUTURE_UPDATER.get(this)) == null || oldFuture.getDelay(TimeUnit.SECONDS) > (long)ttl) {
                io.netty.util.concurrent.ScheduledFuture<?> newFuture = loop.schedule(this, (long)ttl, TimeUnit.SECONDS);
                if (FUTURE_UPDATER.compareAndSet(this, oldFuture, newFuture)) {
                    if (oldFuture == null) break;
                    oldFuture.cancel(true);
                    break;
                }
                newFuture.cancel(true);
            }
        }

        boolean clearAndCancel() {
            List entries = this.getAndSet(Collections.emptyList());
            if (entries.isEmpty()) {
                return false;
            }
            ScheduledFuture expirationFuture = FUTURE_UPDATER.getAndSet(this, CANCELLED);
            if (expirationFuture != null) {
                expirationFuture.cancel(false);
            }
            return true;
        }

        @Override
        public void run() {
            Cache.this.resolveCache.remove(this.hostname, this);
            this.clearAndCancel();
        }
    }
}

