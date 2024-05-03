/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.AbstractMapEntry;
import com.google.common.collect.Cut;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;

@Beta
@GwtIncompatible(value="NavigableMap")
public final class TreeRangeMap<K extends Comparable, V>
implements RangeMap<K, V> {
    private final NavigableMap<Cut<K>, RangeMapEntry<K, V>> entriesByLowerBound = Maps.newTreeMap();
    private static final RangeMap EMPTY_SUB_RANGE_MAP = new RangeMap(){

        @Nullable
        public Object get(Comparable key) {
            return null;
        }

        @Nullable
        public Map.Entry<Range, Object> getEntry(Comparable key) {
            return null;
        }

        public Range span() {
            throw new NoSuchElementException();
        }

        public void put(Range range, Object value) {
            Preconditions.checkNotNull(range);
            throw new IllegalArgumentException("Cannot insert range " + range + " into an empty subRangeMap");
        }

        public void putAll(RangeMap rangeMap) {
            if (!rangeMap.asMapOfRanges().isEmpty()) {
                throw new IllegalArgumentException("Cannot putAll(nonEmptyRangeMap) into an empty subRangeMap");
            }
        }

        @Override
        public void clear() {
        }

        public void remove(Range range) {
            Preconditions.checkNotNull(range);
        }

        public Map<Range, Object> asMapOfRanges() {
            return Collections.emptyMap();
        }

        public RangeMap subRangeMap(Range range) {
            Preconditions.checkNotNull(range);
            return this;
        }
    };

    public static <K extends Comparable, V> TreeRangeMap<K, V> create() {
        return new TreeRangeMap<K, V>();
    }

    private TreeRangeMap() {
    }

    @Override
    @Nullable
    public V get(K key) {
        Map.Entry<Range<K>, V> entry = this.getEntry(key);
        return entry == null ? null : (V)entry.getValue();
    }

    @Override
    @Nullable
    public Map.Entry<Range<K>, V> getEntry(K key) {
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> mapEntry = this.entriesByLowerBound.floorEntry(Cut.belowValue(key));
        if (mapEntry != null && mapEntry.getValue().contains(key)) {
            return mapEntry.getValue();
        }
        return null;
    }

    @Override
    public void put(Range<K> range, V value) {
        if (!range.isEmpty()) {
            Preconditions.checkNotNull(value);
            this.remove(range);
            this.entriesByLowerBound.put(range.lowerBound, new RangeMapEntry<K, V>(range, value));
        }
    }

    @Override
    public void putAll(RangeMap<K, V> rangeMap) {
        for (Map.Entry<Range<K>, V> entry : rangeMap.asMapOfRanges().entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.entriesByLowerBound.clear();
    }

    @Override
    public Range<K> span() {
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> firstEntry = this.entriesByLowerBound.firstEntry();
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> lastEntry = this.entriesByLowerBound.lastEntry();
        if (firstEntry == null) {
            throw new NoSuchElementException();
        }
        return Range.create(((Range)firstEntry.getValue().getKey()).lowerBound, ((Range)lastEntry.getValue().getKey()).upperBound);
    }

    private void putRangeMapEntry(Cut<K> lowerBound, Cut<K> upperBound, V value) {
        this.entriesByLowerBound.put(lowerBound, new RangeMapEntry<K, V>(lowerBound, upperBound, value));
    }

    @Override
    public void remove(Range<K> rangeToRemove) {
        RangeMapEntry<K, V> rangeMapEntry;
        Map.Entry mapEntryAboveToTruncate;
        RangeMapEntry<K, V> rangeMapEntry2;
        if (rangeToRemove.isEmpty()) {
            return;
        }
        Map.Entry mapEntryBelowToTruncate = this.entriesByLowerBound.lowerEntry(rangeToRemove.lowerBound);
        if (mapEntryBelowToTruncate != null && (rangeMapEntry2 = mapEntryBelowToTruncate.getValue()).getUpperBound().compareTo(rangeToRemove.lowerBound) > 0) {
            if (rangeMapEntry2.getUpperBound().compareTo(rangeToRemove.upperBound) > 0) {
                this.putRangeMapEntry(rangeToRemove.upperBound, rangeMapEntry2.getUpperBound(), mapEntryBelowToTruncate.getValue().getValue());
            }
            this.putRangeMapEntry(rangeMapEntry2.getLowerBound(), rangeToRemove.lowerBound, mapEntryBelowToTruncate.getValue().getValue());
        }
        if ((mapEntryAboveToTruncate = this.entriesByLowerBound.lowerEntry(rangeToRemove.upperBound)) != null && (rangeMapEntry = mapEntryAboveToTruncate.getValue()).getUpperBound().compareTo(rangeToRemove.upperBound) > 0) {
            this.putRangeMapEntry(rangeToRemove.upperBound, rangeMapEntry.getUpperBound(), mapEntryAboveToTruncate.getValue().getValue());
            this.entriesByLowerBound.remove(rangeToRemove.lowerBound);
        }
        this.entriesByLowerBound.subMap(rangeToRemove.lowerBound, rangeToRemove.upperBound).clear();
    }

    @Override
    public Map<Range<K>, V> asMapOfRanges() {
        return new AsMapOfRanges();
    }

    @Override
    public RangeMap<K, V> subRangeMap(Range<K> subRange) {
        if (subRange.equals(Range.all())) {
            return this;
        }
        return new SubRangeMap(subRange);
    }

    private RangeMap<K, V> emptySubRangeMap() {
        return EMPTY_SUB_RANGE_MAP;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof RangeMap) {
            RangeMap rangeMap = (RangeMap)o;
            return this.asMapOfRanges().equals(rangeMap.asMapOfRanges());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.asMapOfRanges().hashCode();
    }

    @Override
    public String toString() {
        return this.entriesByLowerBound.values().toString();
    }

    private class SubRangeMap
    implements RangeMap<K, V> {
        private final Range<K> subRange;

        SubRangeMap(Range<K> subRange) {
            this.subRange = subRange;
        }

        @Override
        @Nullable
        public V get(K key) {
            return this.subRange.contains(key) ? (Object)TreeRangeMap.this.get(key) : null;
        }

        @Override
        @Nullable
        public Map.Entry<Range<K>, V> getEntry(K key) {
            Map.Entry entry;
            if (this.subRange.contains(key) && (entry = TreeRangeMap.this.getEntry(key)) != null) {
                return Maps.immutableEntry(entry.getKey().intersection(this.subRange), entry.getValue());
            }
            return null;
        }

        @Override
        public Range<K> span() {
            Cut lowerBound;
            Map.Entry lowerEntry = TreeRangeMap.this.entriesByLowerBound.floorEntry(this.subRange.lowerBound);
            if (lowerEntry != null && ((RangeMapEntry)lowerEntry.getValue()).getUpperBound().compareTo(this.subRange.lowerBound) > 0) {
                lowerBound = this.subRange.lowerBound;
            } else {
                lowerBound = TreeRangeMap.this.entriesByLowerBound.ceilingKey(this.subRange.lowerBound);
                if (lowerBound == null || lowerBound.compareTo(this.subRange.upperBound) >= 0) {
                    throw new NoSuchElementException();
                }
            }
            Map.Entry upperEntry = TreeRangeMap.this.entriesByLowerBound.lowerEntry(this.subRange.upperBound);
            if (upperEntry == null) {
                throw new NoSuchElementException();
            }
            Cut<Object> upperBound = ((RangeMapEntry)upperEntry.getValue()).getUpperBound().compareTo(this.subRange.upperBound) >= 0 ? this.subRange.upperBound : ((RangeMapEntry)upperEntry.getValue()).getUpperBound();
            return Range.create(lowerBound, upperBound);
        }

        @Override
        public void put(Range<K> range, V value) {
            Preconditions.checkArgument(this.subRange.encloses(range), "Cannot put range %s into a subRangeMap(%s)", range, this.subRange);
            TreeRangeMap.this.put(range, value);
        }

        @Override
        public void putAll(RangeMap<K, V> rangeMap) {
            if (rangeMap.asMapOfRanges().isEmpty()) {
                return;
            }
            Range span = rangeMap.span();
            Preconditions.checkArgument(this.subRange.encloses(span), "Cannot putAll rangeMap with span %s into a subRangeMap(%s)", span, this.subRange);
            TreeRangeMap.this.putAll(rangeMap);
        }

        @Override
        public void clear() {
            TreeRangeMap.this.remove(this.subRange);
        }

        @Override
        public void remove(Range<K> range) {
            if (range.isConnected(this.subRange)) {
                TreeRangeMap.this.remove(range.intersection(this.subRange));
            }
        }

        @Override
        public RangeMap<K, V> subRangeMap(Range<K> range) {
            if (!range.isConnected(this.subRange)) {
                return TreeRangeMap.this.emptySubRangeMap();
            }
            return TreeRangeMap.this.subRangeMap(range.intersection(this.subRange));
        }

        @Override
        public Map<Range<K>, V> asMapOfRanges() {
            return new SubRangeMapAsMap();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof RangeMap) {
                RangeMap rangeMap = (RangeMap)o;
                return this.asMapOfRanges().equals(rangeMap.asMapOfRanges());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.asMapOfRanges().hashCode();
        }

        @Override
        public String toString() {
            return this.asMapOfRanges().toString();
        }

        class SubRangeMapAsMap
        extends AbstractMap<Range<K>, V> {
            SubRangeMapAsMap() {
            }

            @Override
            public boolean containsKey(Object key) {
                return this.get(key) != null;
            }

            @Override
            public V get(Object key) {
                try {
                    if (key instanceof Range) {
                        Range r = (Range)key;
                        if (!SubRangeMap.this.subRange.encloses(r) || r.isEmpty()) {
                            return null;
                        }
                        RangeMapEntry candidate = null;
                        if (r.lowerBound.compareTo(((SubRangeMap)SubRangeMap.this).subRange.lowerBound) == 0) {
                            Map.Entry entry = TreeRangeMap.this.entriesByLowerBound.floorEntry(r.lowerBound);
                            if (entry != null) {
                                candidate = (RangeMapEntry)entry.getValue();
                            }
                        } else {
                            candidate = (RangeMapEntry)TreeRangeMap.this.entriesByLowerBound.get(r.lowerBound);
                        }
                        if (candidate != null && ((Range)candidate.getKey()).isConnected(SubRangeMap.this.subRange) && ((Range)candidate.getKey()).intersection(SubRangeMap.this.subRange).equals(r)) {
                            return candidate.getValue();
                        }
                    }
                } catch (ClassCastException e) {
                    return null;
                }
                return null;
            }

            @Override
            public V remove(Object key) {
                Object value = this.get(key);
                if (value != null) {
                    Range range = (Range)key;
                    TreeRangeMap.this.remove(range);
                    return value;
                }
                return null;
            }

            @Override
            public void clear() {
                SubRangeMap.this.clear();
            }

            private boolean removeEntryIf(Predicate<? super Map.Entry<Range<K>, V>> predicate) {
                ArrayList toRemove = Lists.newArrayList();
                for (Map.Entry entry : this.entrySet()) {
                    if (!predicate.apply(entry)) continue;
                    toRemove.add(entry.getKey());
                }
                for (Range range : toRemove) {
                    TreeRangeMap.this.remove(range);
                }
                return !toRemove.isEmpty();
            }

            @Override
            public Set<Range<K>> keySet() {
                return new Maps.KeySet<Range<K>, V>(this){

                    @Override
                    public boolean remove(@Nullable Object o) {
                        return SubRangeMapAsMap.this.remove(o) != null;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return SubRangeMapAsMap.this.removeEntryIf(Predicates.compose(Predicates.not(Predicates.in(c)), Maps.keyFunction()));
                    }
                };
            }

            @Override
            public Set<Map.Entry<Range<K>, V>> entrySet() {
                return new Maps.EntrySet<Range<K>, V>(){

                    @Override
                    Map<Range<K>, V> map() {
                        return SubRangeMapAsMap.this;
                    }

                    @Override
                    public Iterator<Map.Entry<Range<K>, V>> iterator() {
                        if (SubRangeMap.this.subRange.isEmpty()) {
                            return Iterators.emptyIterator();
                        }
                        Cut cutToStart = Objects.firstNonNull(TreeRangeMap.this.entriesByLowerBound.floorKey(((SubRangeMap)SubRangeMap.this).subRange.lowerBound), ((SubRangeMap)SubRangeMap.this).subRange.lowerBound);
                        final Iterator backingItr = TreeRangeMap.this.entriesByLowerBound.tailMap(cutToStart, true).values().iterator();
                        return new AbstractIterator<Map.Entry<Range<K>, V>>(){

                            @Override
                            protected Map.Entry<Range<K>, V> computeNext() {
                                RangeMapEntry entry;
                                while (backingItr.hasNext() && (entry = (RangeMapEntry)backingItr.next()).getLowerBound().compareTo(((SubRangeMap)SubRangeMap.this).subRange.upperBound) < 0) {
                                    if (entry.getUpperBound().compareTo(((SubRangeMap)SubRangeMap.this).subRange.lowerBound) <= 0) continue;
                                    return Maps.immutableEntry(((Range)entry.getKey()).intersection(SubRangeMap.this.subRange), entry.getValue());
                                }
                                return (Map.Entry)this.endOfData();
                            }
                        };
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return SubRangeMapAsMap.this.removeEntryIf(Predicates.not(Predicates.in(c)));
                    }

                    @Override
                    public int size() {
                        return Iterators.size(this.iterator());
                    }

                    @Override
                    public boolean isEmpty() {
                        return !this.iterator().hasNext();
                    }
                };
            }

            @Override
            public Collection<V> values() {
                return new Maps.Values<Range<K>, V>(this){

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return SubRangeMapAsMap.this.removeEntryIf(Predicates.compose(Predicates.in(c), Maps.valueFunction()));
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return SubRangeMapAsMap.this.removeEntryIf(Predicates.compose(Predicates.not(Predicates.in(c)), Maps.valueFunction()));
                    }
                };
            }
        }
    }

    private final class AsMapOfRanges
    extends AbstractMap<Range<K>, V> {
        private AsMapOfRanges() {
        }

        @Override
        public boolean containsKey(@Nullable Object key) {
            return this.get(key) != null;
        }

        @Override
        public V get(@Nullable Object key) {
            if (key instanceof Range) {
                Range range = (Range)key;
                RangeMapEntry rangeMapEntry = (RangeMapEntry)TreeRangeMap.this.entriesByLowerBound.get(range.lowerBound);
                if (rangeMapEntry != null && ((Range)rangeMapEntry.getKey()).equals(range)) {
                    return rangeMapEntry.getValue();
                }
            }
            return null;
        }

        @Override
        public Set<Map.Entry<Range<K>, V>> entrySet() {
            return new AbstractSet<Map.Entry<Range<K>, V>>(){

                @Override
                public Iterator<Map.Entry<Range<K>, V>> iterator() {
                    return TreeRangeMap.this.entriesByLowerBound.values().iterator();
                }

                @Override
                public int size() {
                    return TreeRangeMap.this.entriesByLowerBound.size();
                }
            };
        }
    }

    private static final class RangeMapEntry<K extends Comparable, V>
    extends AbstractMapEntry<Range<K>, V> {
        private final Range<K> range;
        private final V value;

        RangeMapEntry(Cut<K> lowerBound, Cut<K> upperBound, V value) {
            this(Range.create(lowerBound, upperBound), value);
        }

        RangeMapEntry(Range<K> range, V value) {
            this.range = range;
            this.value = value;
        }

        @Override
        public Range<K> getKey() {
            return this.range;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        public boolean contains(K value) {
            return this.range.contains(value);
        }

        Cut<K> getLowerBound() {
            return this.range.lowerBound;
        }

        Cut<K> getUpperBound() {
            return this.range.upperBound;
        }
    }
}

