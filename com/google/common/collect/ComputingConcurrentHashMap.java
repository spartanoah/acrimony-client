/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.MapMakerInternalMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

class ComputingConcurrentHashMap<K, V>
extends MapMakerInternalMap<K, V> {
    final Function<? super K, ? extends V> computingFunction;
    private static final long serialVersionUID = 4L;

    ComputingConcurrentHashMap(MapMaker builder, Function<? super K, ? extends V> computingFunction) {
        super(builder);
        this.computingFunction = Preconditions.checkNotNull(computingFunction);
    }

    @Override
    MapMakerInternalMap.Segment<K, V> createSegment(int initialCapacity, int maxSegmentSize) {
        return new ComputingSegment(this, initialCapacity, maxSegmentSize);
    }

    @Override
    ComputingSegment<K, V> segmentFor(int hash) {
        return (ComputingSegment)super.segmentFor(hash);
    }

    V getOrCompute(K key) throws ExecutionException {
        int hash = this.hash(Preconditions.checkNotNull(key));
        return ((ComputingSegment)this.segmentFor(hash)).getOrCompute(key, hash, this.computingFunction);
    }

    @Override
    Object writeReplace() {
        return new ComputingSerializationProxy<K, V>(this.keyStrength, this.valueStrength, this.keyEquivalence, this.valueEquivalence, this.expireAfterWriteNanos, this.expireAfterAccessNanos, this.maximumSize, this.concurrencyLevel, this.removalListener, this, this.computingFunction);
    }

    static final class ComputingSerializationProxy<K, V>
    extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
        final Function<? super K, ? extends V> computingFunction;
        private static final long serialVersionUID = 4L;

        ComputingSerializationProxy(MapMakerInternalMap.Strength keyStrength, MapMakerInternalMap.Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, long expireAfterWriteNanos, long expireAfterAccessNanos, int maximumSize, int concurrencyLevel, MapMaker.RemovalListener<? super K, ? super V> removalListener, ConcurrentMap<K, V> delegate, Function<? super K, ? extends V> computingFunction) {
            super(keyStrength, valueStrength, keyEquivalence, valueEquivalence, expireAfterWriteNanos, expireAfterAccessNanos, maximumSize, concurrencyLevel, removalListener, delegate);
            this.computingFunction = computingFunction;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            this.writeMapTo(out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            MapMaker mapMaker = this.readMapMaker(in);
            this.delegate = mapMaker.makeComputingMap(this.computingFunction);
            this.readEntries(in);
        }

        Object readResolve() {
            return this.delegate;
        }
    }

    private static final class ComputingValueReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V> {
        final Function<? super K, ? extends V> computingFunction;
        @GuardedBy(value="ComputingValueReference.this")
        volatile MapMakerInternalMap.ValueReference<K, V> computedReference = MapMakerInternalMap.unset();

        public ComputingValueReference(Function<? super K, ? extends V> computingFunction) {
            this.computingFunction = computingFunction;
        }

        @Override
        public V get() {
            return null;
        }

        @Override
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, @Nullable V value, MapMakerInternalMap.ReferenceEntry<K, V> entry) {
            return this;
        }

        @Override
        public boolean isComputingReference() {
            return true;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public V waitForValue() throws ExecutionException {
            if (this.computedReference == MapMakerInternalMap.UNSET) {
                boolean interrupted = false;
                try {
                    ComputingValueReference computingValueReference = this;
                    synchronized (computingValueReference) {
                        while (this.computedReference == MapMakerInternalMap.UNSET) {
                            try {
                                this.wait();
                            } catch (InterruptedException ie) {
                                interrupted = true;
                            }
                        }
                    }
                } finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return this.computedReference.waitForValue();
        }

        @Override
        public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {
            this.setValueReference(newValue);
        }

        V compute(K key, int hash) throws ExecutionException {
            V value;
            try {
                value = this.computingFunction.apply(key);
            } catch (Throwable t) {
                this.setValueReference(new ComputationExceptionReference(t));
                throw new ExecutionException(t);
            }
            this.setValueReference(new ComputedReference(value));
            return value;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setValueReference(MapMakerInternalMap.ValueReference<K, V> valueReference) {
            ComputingValueReference computingValueReference = this;
            synchronized (computingValueReference) {
                if (this.computedReference == MapMakerInternalMap.UNSET) {
                    this.computedReference = valueReference;
                    this.notifyAll();
                }
            }
        }
    }

    private static final class ComputedReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V> {
        final V value;

        ComputedReference(@Nullable V value) {
            this.value = value;
        }

        @Override
        public V get() {
            return this.value;
        }

        @Override
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, V value, MapMakerInternalMap.ReferenceEntry<K, V> entry) {
            return this;
        }

        @Override
        public boolean isComputingReference() {
            return false;
        }

        @Override
        public V waitForValue() {
            return this.get();
        }

        @Override
        public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {
        }
    }

    private static final class ComputationExceptionReference<K, V>
    implements MapMakerInternalMap.ValueReference<K, V> {
        final Throwable t;

        ComputationExceptionReference(Throwable t) {
            this.t = t;
        }

        @Override
        public V get() {
            return null;
        }

        @Override
        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        @Override
        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> queue, V value, MapMakerInternalMap.ReferenceEntry<K, V> entry) {
            return this;
        }

        @Override
        public boolean isComputingReference() {
            return false;
        }

        @Override
        public V waitForValue() throws ExecutionException {
            throw new ExecutionException(this.t);
        }

        @Override
        public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {
        }
    }

    static final class ComputingSegment<K, V>
    extends MapMakerInternalMap.Segment<K, V> {
        ComputingSegment(MapMakerInternalMap<K, V> map, int initialCapacity, int maxSegmentSize) {
            super(map, initialCapacity, maxSegmentSize);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        V getOrCompute(K key, int hash, Function<? super K, ? extends V> computingFunction) throws ExecutionException {
            try {
                Object v;
                MapMakerInternalMap.ReferenceEntry<Object, Object> e;
                Object value;
                do {
                    if ((e = this.getEntry(key, hash)) != null && (value = this.getLiveValue(e)) != null) {
                        this.recordRead(e);
                        v = value;
                        return v;
                    }
                    if (e == null || !e.getValueReference().isComputingReference()) {
                        ComputingValueReference<? super K, ? extends V> computingValueReference;
                        boolean createNewEntry;
                        block22: {
                            createNewEntry = true;
                            computingValueReference = null;
                            this.lock();
                            try {
                                MapMakerInternalMap.ReferenceEntry<Object, Object> first;
                                this.preWriteCleanup();
                                int newCount = this.count - 1;
                                AtomicReferenceArray table = this.table;
                                int index = hash & table.length() - 1;
                                for (e = first = (MapMakerInternalMap.ReferenceEntry<Object, Object>)table.get(index); e != null; e = e.getNext()) {
                                    Object entryKey = e.getKey();
                                    if (e.getHash() != hash || entryKey == null || !this.map.keyEquivalence.equivalent(key, entryKey)) continue;
                                    MapMakerInternalMap.ValueReference valueReference = e.getValueReference();
                                    if (valueReference.isComputingReference()) {
                                        createNewEntry = false;
                                        break;
                                    }
                                    Object value2 = e.getValueReference().get();
                                    if (value2 == null) {
                                        this.enqueueNotification(entryKey, hash, value2, MapMaker.RemovalCause.COLLECTED);
                                    } else if (this.map.expires() && this.map.isExpired(e)) {
                                        this.enqueueNotification(entryKey, hash, value2, MapMaker.RemovalCause.EXPIRED);
                                    } else {
                                        this.recordLockedRead(e);
                                        Object v2 = value2;
                                        return v2;
                                    }
                                    this.evictionQueue.remove(e);
                                    this.expirationQueue.remove(e);
                                    this.count = newCount;
                                    break;
                                }
                                if (!createNewEntry) break block22;
                                computingValueReference = new ComputingValueReference<K, V>(computingFunction);
                                if (e == null) {
                                    e = this.newEntry(key, hash, first);
                                    e.setValueReference(computingValueReference);
                                    table.set(index, e);
                                } else {
                                    e.setValueReference(computingValueReference);
                                }
                            } finally {
                                this.unlock();
                                this.postWriteCleanup();
                            }
                        }
                        if (createNewEntry) {
                            V v3 = this.compute(key, hash, e, computingValueReference);
                            return v3;
                        }
                    }
                    Preconditions.checkState(!Thread.holdsLock(e), "Recursive computation");
                } while ((value = e.getValueReference().waitForValue()) == null);
                this.recordRead(e);
                v = value;
                return v;
            } finally {
                this.postReadCleanup();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        V compute(K key, int hash, MapMakerInternalMap.ReferenceEntry<K, V> e, ComputingValueReference<K, V> computingValueReference) throws ExecutionException {
            Object value = null;
            long start = System.nanoTime();
            long end = 0L;
            try {
                Object oldValue;
                MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry = e;
                synchronized (referenceEntry) {
                    value = computingValueReference.compute(key, hash);
                    end = System.nanoTime();
                }
                if (value != null && (oldValue = this.put(key, hash, value, true)) != null) {
                    this.enqueueNotification(key, hash, value, MapMaker.RemovalCause.REPLACED);
                }
                referenceEntry = value;
                return (V)referenceEntry;
            } finally {
                if (end == 0L) {
                    end = System.nanoTime();
                }
                if (value == null) {
                    this.clearValue(key, hash, computingValueReference);
                }
            }
        }
    }
}

