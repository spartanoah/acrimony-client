/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

final class MaxCapacityQueue<E>
implements Queue<E> {
    private final Queue<E> queue;
    private final int maxCapacity;

    MaxCapacityQueue(Queue<E> queue, int maxCapacity) {
        this.queue = queue;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean add(E element) {
        if (this.offer(element)) {
            return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean offer(E element) {
        if (this.maxCapacity <= this.queue.size()) {
            return false;
        }
        return this.queue.offer(element);
    }

    @Override
    public E remove() {
        return this.queue.remove();
    }

    @Override
    public E poll() {
        return this.queue.poll();
    }

    @Override
    public E element() {
        return this.queue.element();
    }

    @Override
    public E peek() {
        return this.queue.peek();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.queue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return this.queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (this.maxCapacity >= this.size() + c.size()) {
            return this.queue.addAll(c);
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.queue.retainAll(c);
    }

    @Override
    public void clear() {
        this.queue.clear();
    }
}

