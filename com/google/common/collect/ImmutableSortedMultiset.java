/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.DescendingImmutableSortedMultiset;
import com.google.common.collect.EmptyImmutableSortedMultiset;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMultisetFauxverideShim;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.RegularImmutableSortedMultiset;
import com.google.common.collect.RegularImmutableSortedSet;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

@Beta
@GwtIncompatible(value="hasn't been tested yet")
public abstract class ImmutableSortedMultiset<E>
extends ImmutableSortedMultisetFauxverideShim<E>
implements SortedMultiset<E> {
    private static final Comparator<Comparable> NATURAL_ORDER = Ordering.natural();
    private static final ImmutableSortedMultiset<Comparable> NATURAL_EMPTY_MULTISET = new EmptyImmutableSortedMultiset<Comparable>(NATURAL_ORDER);
    transient ImmutableSortedMultiset<E> descendingMultiset;

    public static <E> ImmutableSortedMultiset<E> of() {
        return NATURAL_EMPTY_MULTISET;
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E element) {
        RegularImmutableSortedSet elementSet = (RegularImmutableSortedSet)ImmutableSortedSet.of(element);
        int[] counts = new int[]{1};
        long[] cumulativeCounts = new long[]{0L, 1L};
        return new RegularImmutableSortedMultiset(elementSet, counts, cumulativeCounts, 0, 1);
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2) {
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), Arrays.asList(e1, e2));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3) {
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), Arrays.asList(e1, e2, e3));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4) {
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), Arrays.asList(e1, e2, e3, e4));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5) {
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), Arrays.asList(e1, e2, e3, e4, e5));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E ... remaining) {
        int size = remaining.length + 6;
        ArrayList all = Lists.newArrayListWithCapacity(size);
        Collections.addAll(all, e1, e2, e3, e4, e5, e6);
        Collections.addAll(all, remaining);
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), all);
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> copyOf(E[] elements) {
        return ImmutableSortedMultiset.copyOf(Ordering.natural(), Arrays.asList(elements));
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Iterable<? extends E> elements) {
        Ordering naturalOrder = Ordering.natural();
        return ImmutableSortedMultiset.copyOf(naturalOrder, elements);
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Iterator<? extends E> elements) {
        Ordering naturalOrder = Ordering.natural();
        return ImmutableSortedMultiset.copyOf(naturalOrder, elements);
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Comparator<? super E> comparator, Iterator<? extends E> elements) {
        Preconditions.checkNotNull(comparator);
        return ((Builder)new Builder<E>(comparator).addAll((Iterator)elements)).build();
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Comparator<? super E> comparator, Iterable<? extends E> elements) {
        ImmutableSortedMultiset multiset;
        if (elements instanceof ImmutableSortedMultiset && comparator.equals((multiset = (ImmutableSortedMultiset)elements).comparator())) {
            if (multiset.isPartialView()) {
                return ImmutableSortedMultiset.copyOfSortedEntries(comparator, ((ImmutableCollection)((Object)multiset.entrySet())).asList());
            }
            return multiset;
        }
        elements = Lists.newArrayList(elements);
        TreeMultiset<E> sortedCopy = TreeMultiset.create(Preconditions.checkNotNull(comparator));
        Iterables.addAll(sortedCopy, elements);
        return ImmutableSortedMultiset.copyOfSortedEntries(comparator, sortedCopy.entrySet());
    }

    public static <E> ImmutableSortedMultiset<E> copyOfSorted(SortedMultiset<E> sortedMultiset) {
        return ImmutableSortedMultiset.copyOfSortedEntries(sortedMultiset.comparator(), Lists.newArrayList(sortedMultiset.entrySet()));
    }

    private static <E> ImmutableSortedMultiset<E> copyOfSortedEntries(Comparator<? super E> comparator, Collection<Multiset.Entry<E>> entries) {
        if (entries.isEmpty()) {
            return ImmutableSortedMultiset.emptyMultiset(comparator);
        }
        ImmutableList.Builder elementsBuilder = new ImmutableList.Builder(entries.size());
        int[] counts = new int[entries.size()];
        long[] cumulativeCounts = new long[entries.size() + 1];
        int i = 0;
        for (Multiset.Entry<E> entry : entries) {
            elementsBuilder.add((Object)entry.getElement());
            counts[i] = entry.getCount();
            cumulativeCounts[i + 1] = cumulativeCounts[i] + (long)counts[i];
            ++i;
        }
        return new RegularImmutableSortedMultiset<E>(new RegularImmutableSortedSet<E>(elementsBuilder.build(), comparator), counts, cumulativeCounts, 0, entries.size());
    }

    static <E> ImmutableSortedMultiset<E> emptyMultiset(Comparator<? super E> comparator) {
        if (NATURAL_ORDER.equals(comparator)) {
            return NATURAL_EMPTY_MULTISET;
        }
        return new EmptyImmutableSortedMultiset<E>(comparator);
    }

    ImmutableSortedMultiset() {
    }

    @Override
    public final Comparator<? super E> comparator() {
        return ((ImmutableSortedSet)this.elementSet()).comparator();
    }

    @Override
    public abstract ImmutableSortedSet<E> elementSet();

    @Override
    public ImmutableSortedMultiset<E> descendingMultiset() {
        ImmutableSortedMultiset<E> result = this.descendingMultiset;
        if (result == null) {
            this.descendingMultiset = new DescendingImmutableSortedMultiset(this);
            return this.descendingMultiset;
        }
        return result;
    }

    @Override
    @Deprecated
    public final Multiset.Entry<E> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final Multiset.Entry<E> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract ImmutableSortedMultiset<E> headMultiset(E var1, BoundType var2);

    @Override
    public ImmutableSortedMultiset<E> subMultiset(E lowerBound, BoundType lowerBoundType, E upperBound, BoundType upperBoundType) {
        Preconditions.checkArgument(this.comparator().compare(lowerBound, upperBound) <= 0, "Expected lowerBound <= upperBound but %s > %s", lowerBound, upperBound);
        return ((ImmutableSortedMultiset)this.tailMultiset((Object)lowerBound, lowerBoundType)).headMultiset((Object)upperBound, upperBoundType);
    }

    @Override
    public abstract ImmutableSortedMultiset<E> tailMultiset(E var1, BoundType var2);

    public static <E> Builder<E> orderedBy(Comparator<E> comparator) {
        return new Builder<E>(comparator);
    }

    public static <E extends Comparable<E>> Builder<E> reverseOrder() {
        return new Builder(Ordering.natural().reverse());
    }

    public static <E extends Comparable<E>> Builder<E> naturalOrder() {
        return new Builder(Ordering.natural());
    }

    @Override
    Object writeReplace() {
        return new SerializedForm(this);
    }

    private static final class SerializedForm<E>
    implements Serializable {
        Comparator<? super E> comparator;
        E[] elements;
        int[] counts;

        SerializedForm(SortedMultiset<E> multiset) {
            this.comparator = multiset.comparator();
            int n = multiset.entrySet().size();
            this.elements = new Object[n];
            this.counts = new int[n];
            int i = 0;
            for (Multiset.Entry<E> entry : multiset.entrySet()) {
                this.elements[i] = entry.getElement();
                this.counts[i] = entry.getCount();
                ++i;
            }
        }

        Object readResolve() {
            int n = this.elements.length;
            Builder<E> builder = new Builder<E>(this.comparator);
            for (int i = 0; i < n; ++i) {
                builder.addCopies((Object)this.elements[i], this.counts[i]);
            }
            return builder.build();
        }
    }

    public static class Builder<E>
    extends ImmutableMultiset.Builder<E> {
        public Builder(Comparator<? super E> comparator) {
            super(TreeMultiset.create(Preconditions.checkNotNull(comparator)));
        }

        @Override
        public Builder<E> add(E element) {
            super.add((Object)element);
            return this;
        }

        @Override
        public Builder<E> addCopies(E element, int occurrences) {
            super.addCopies(element, occurrences);
            return this;
        }

        @Override
        public Builder<E> setCount(E element, int count) {
            super.setCount(element, count);
            return this;
        }

        @Override
        public Builder<E> add(E ... elements) {
            super.add((Object[])elements);
            return this;
        }

        @Override
        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll((Iterable)elements);
            return this;
        }

        @Override
        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll((Iterator)elements);
            return this;
        }

        @Override
        public ImmutableSortedMultiset<E> build() {
            return ImmutableSortedMultiset.copyOfSorted((SortedMultiset)this.contents);
        }
    }
}

