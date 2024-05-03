/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueL3Pad;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;

public class MpscArrayQueue<E>
extends MpscArrayQueueL3Pad<E> {
    public MpscArrayQueue(int capacity) {
        super(capacity);
    }

    public boolean offerIfBelowThreshold(E e, int threshold) {
        long pIndex;
        if (null == e) {
            throw new NullPointerException();
        }
        long mask = this.mask;
        long capacity = mask + 1L;
        long producerLimit = this.lvProducerLimit();
        do {
            long available;
            long size;
            if ((size = capacity - (available = producerLimit - (pIndex = this.lvProducerIndex()))) < (long)threshold) continue;
            long cIndex = this.lvConsumerIndex();
            size = pIndex - cIndex;
            if (size >= (long)threshold) {
                return false;
            }
            producerLimit = cIndex + capacity;
            this.soProducerLimit(producerLimit);
        } while (!this.casProducerIndex(pIndex, pIndex + 1L));
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(pIndex, mask);
        UnsafeRefArrayAccess.soRefElement(this.buffer, offset, e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        long pIndex;
        if (null == e) {
            throw new NullPointerException();
        }
        long mask = this.mask;
        long producerLimit = this.lvProducerLimit();
        do {
            if ((pIndex = this.lvProducerIndex()) < producerLimit) continue;
            long cIndex = this.lvConsumerIndex();
            producerLimit = cIndex + mask + 1L;
            if (pIndex >= producerLimit) {
                return false;
            }
            this.soProducerLimit(producerLimit);
        } while (!this.casProducerIndex(pIndex, pIndex + 1L));
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(pIndex, mask);
        UnsafeRefArrayAccess.soRefElement(this.buffer, offset, e);
        return true;
    }

    public final int failFastOffer(E e) {
        long producerLimit;
        if (null == e) {
            throw new NullPointerException();
        }
        long mask = this.mask;
        long capacity = mask + 1L;
        long pIndex = this.lvProducerIndex();
        if (pIndex >= (producerLimit = this.lvProducerLimit())) {
            long cIndex = this.lvConsumerIndex();
            producerLimit = cIndex + capacity;
            if (pIndex >= producerLimit) {
                return 1;
            }
            this.soProducerLimit(producerLimit);
        }
        if (!this.casProducerIndex(pIndex, pIndex + 1L)) {
            return -1;
        }
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(pIndex, mask);
        UnsafeRefArrayAccess.soRefElement(this.buffer, offset, e);
        return 0;
    }

    @Override
    public E poll() {
        Object[] buffer = this.buffer;
        long cIndex = this.lpConsumerIndex();
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(cIndex, this.mask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (null == e) {
            if (cIndex != this.lvProducerIndex()) {
                while ((e = UnsafeRefArrayAccess.lvRefElement(buffer, offset)) == null) {
                }
            } else {
                return null;
            }
        }
        UnsafeRefArrayAccess.spRefElement(buffer, offset, null);
        this.soConsumerIndex(cIndex + 1L);
        return (E)e;
    }

    @Override
    public E peek() {
        Object[] buffer = this.buffer;
        long cIndex = this.lpConsumerIndex();
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(cIndex, this.mask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (null == e) {
            if (cIndex != this.lvProducerIndex()) {
                while ((e = UnsafeRefArrayAccess.lvRefElement(buffer, offset)) == null) {
                }
            } else {
                return null;
            }
        }
        return (E)e;
    }

    @Override
    public boolean relaxedOffer(E e) {
        return this.offer(e);
    }

    @Override
    public E relaxedPoll() {
        Object[] buffer = this.buffer;
        long cIndex = this.lpConsumerIndex();
        long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(cIndex, this.mask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (null == e) {
            return null;
        }
        UnsafeRefArrayAccess.spRefElement(buffer, offset, null);
        this.soConsumerIndex(cIndex + 1L);
        return (E)e;
    }

    @Override
    public E relaxedPeek() {
        Object[] buffer = this.buffer;
        long mask = this.mask;
        long cIndex = this.lpConsumerIndex();
        return (E)UnsafeRefArrayAccess.lvRefElement(buffer, UnsafeRefArrayAccess.calcCircularRefElementOffset(cIndex, mask));
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative: " + limit);
        }
        if (limit == 0) {
            return 0;
        }
        Object[] buffer = this.buffer;
        long mask = this.mask;
        long cIndex = this.lpConsumerIndex();
        for (int i = 0; i < limit; ++i) {
            long index = cIndex + (long)i;
            long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(index, mask);
            Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
            if (null == e) {
                return i;
            }
            UnsafeRefArrayAccess.spRefElement(buffer, offset, null);
            this.soConsumerIndex(index + 1L);
            c.accept(e);
        }
        return limit;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
        long available;
        int actualLimit;
        long pIndex;
        if (null == s) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative:" + limit);
        }
        if (limit == 0) {
            return 0;
        }
        long mask = this.mask;
        long capacity = mask + 1L;
        long producerLimit = this.lvProducerLimit();
        do {
            if ((available = producerLimit - (pIndex = this.lvProducerIndex())) > 0L) continue;
            long cIndex = this.lvConsumerIndex();
            producerLimit = cIndex + capacity;
            available = producerLimit - pIndex;
            if (available <= 0L) {
                return 0;
            }
            this.soProducerLimit(producerLimit);
        } while (!this.casProducerIndex(pIndex, pIndex + (long)(actualLimit = Math.min((int)available, limit))));
        Object[] buffer = this.buffer;
        for (int i = 0; i < actualLimit; ++i) {
            long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset(pIndex + (long)i, mask);
            UnsafeRefArrayAccess.soRefElement(buffer, offset, s.get());
        }
        return actualLimit;
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c) {
        return this.drain(c, this.capacity());
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s) {
        return MessagePassingQueueUtil.fillBounded(this, s);
    }

    @Override
    public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, w, exit);
    }

    @Override
    public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }
}

