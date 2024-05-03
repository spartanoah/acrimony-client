/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields;
import io.netty.util.internal.shaded.org.jctools.queues.LinkedArrayQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import io.netty.util.internal.shaded.org.jctools.util.Pow2;
import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class BaseMpscLinkedArrayQueue<E>
extends BaseMpscLinkedArrayQueueColdProducerFields<E>
implements MessagePassingQueue<E>,
QueueProgressIndicators {
    private static final Object JUMP = new Object();
    private static final Object BUFFER_CONSUMED = new Object();
    private static final int CONTINUE_TO_P_INDEX_CAS = 0;
    private static final int RETRY = 1;
    private static final int QUEUE_FULL = 2;
    private static final int QUEUE_RESIZE = 3;

    public BaseMpscLinkedArrayQueue(int initialCapacity) {
        RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
        int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
        long mask = p2capacity - 1 << 1;
        E[] buffer = UnsafeRefArrayAccess.allocateRefArray(p2capacity + 1);
        this.producerBuffer = buffer;
        this.producerMask = mask;
        this.consumerBuffer = buffer;
        this.consumerMask = mask;
        this.soProducerLimit(mask);
    }

    @Override
    public int size() {
        long currentProducerIndex;
        long before;
        long after = this.lvConsumerIndex();
        do {
            before = after;
            currentProducerIndex = this.lvProducerIndex();
        } while (before != (after = this.lvConsumerIndex()));
        long size = currentProducerIndex - after >> 1;
        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)size;
    }

    @Override
    public boolean isEmpty() {
        return this.lvConsumerIndex() == this.lvProducerIndex();
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public boolean offer(E e) {
        Object[] buffer;
        long mask;
        long pIndex;
        if (null == e) {
            throw new NullPointerException();
        }
        block6: while (true) {
            long producerLimit = this.lvProducerLimit();
            pIndex = this.lvProducerIndex();
            if ((pIndex & 1L) == 1L) continue;
            mask = this.producerMask;
            buffer = this.producerBuffer;
            if (producerLimit <= pIndex) {
                int result = this.offerSlowPath(mask, pIndex, producerLimit);
                switch (result) {
                    case 0: {
                        break;
                    }
                    case 1: {
                        continue block6;
                    }
                    case 2: {
                        return false;
                    }
                    case 3: {
                        this.resize(mask, buffer, pIndex, e, null);
                        return true;
                    }
                }
            }
            if (this.casProducerIndex(pIndex, pIndex + 2L)) break;
        }
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(pIndex, mask);
        UnsafeRefArrayAccess.soRefElement(buffer, offset, e);
        return true;
    }

    @Override
    public E poll() {
        long mask;
        Object[] buffer = this.consumerBuffer;
        long index = this.lpConsumerIndex();
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, mask = this.consumerMask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (e == null) {
            if (index != this.lvProducerIndex()) {
                while ((e = UnsafeRefArrayAccess.lvRefElement(buffer, offset)) == null) {
                }
            } else {
                return null;
            }
        }
        if (e == JUMP) {
            Object[] nextBuffer = this.nextBuffer(buffer, mask);
            return (E)this.newBufferPoll(nextBuffer, index);
        }
        UnsafeRefArrayAccess.soRefElement(buffer, offset, null);
        this.soConsumerIndex(index + 2L);
        return (E)e;
    }

    @Override
    public E peek() {
        long mask;
        Object[] buffer = this.consumerBuffer;
        long index = this.lpConsumerIndex();
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, mask = this.consumerMask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (e == null && index != this.lvProducerIndex()) {
            while ((e = UnsafeRefArrayAccess.lvRefElement(buffer, offset)) == null) {
            }
        }
        if (e == JUMP) {
            return (E)this.newBufferPeek(this.nextBuffer(buffer, mask), index);
        }
        return (E)e;
    }

    private int offerSlowPath(long mask, long pIndex, long producerLimit) {
        long bufferCapacity;
        long cIndex = this.lvConsumerIndex();
        if (cIndex + (bufferCapacity = this.getCurrentBufferCapacity(mask)) > pIndex) {
            if (!this.casProducerLimit(producerLimit, cIndex + bufferCapacity)) {
                return 1;
            }
            return 0;
        }
        if (this.availableInQueue(pIndex, cIndex) <= 0L) {
            return 2;
        }
        if (this.casProducerIndex(pIndex, pIndex + 1L)) {
            return 3;
        }
        return 1;
    }

    protected abstract long availableInQueue(long var1, long var3);

    private E[] nextBuffer(E[] buffer, long mask) {
        long offset = BaseMpscLinkedArrayQueue.nextArrayOffset(mask);
        Object[] nextBuffer = (Object[])UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        this.consumerBuffer = nextBuffer;
        this.consumerMask = LinkedArrayQueueUtil.length(nextBuffer) - 2 << 1;
        UnsafeRefArrayAccess.soRefElement(buffer, offset, BUFFER_CONSUMED);
        return nextBuffer;
    }

    private static long nextArrayOffset(long mask) {
        return LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(mask + 2L, Long.MAX_VALUE);
    }

    private E newBufferPoll(E[] nextBuffer, long index) {
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, this.consumerMask);
        E n = UnsafeRefArrayAccess.lvRefElement(nextBuffer, offset);
        if (n == null) {
            throw new IllegalStateException("new buffer must have at least one element");
        }
        UnsafeRefArrayAccess.soRefElement(nextBuffer, offset, null);
        this.soConsumerIndex(index + 2L);
        return n;
    }

    private E newBufferPeek(E[] nextBuffer, long index) {
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, this.consumerMask);
        E n = UnsafeRefArrayAccess.lvRefElement(nextBuffer, offset);
        if (null == n) {
            throw new IllegalStateException("new buffer must have at least one element");
        }
        return n;
    }

    @Override
    public long currentProducerIndex() {
        return this.lvProducerIndex() / 2L;
    }

    @Override
    public long currentConsumerIndex() {
        return this.lvConsumerIndex() / 2L;
    }

    @Override
    public abstract int capacity();

    @Override
    public boolean relaxedOffer(E e) {
        return this.offer(e);
    }

    @Override
    public E relaxedPoll() {
        long mask;
        Object[] buffer = this.consumerBuffer;
        long index = this.lpConsumerIndex();
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, mask = this.consumerMask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (e == null) {
            return null;
        }
        if (e == JUMP) {
            Object[] nextBuffer = this.nextBuffer(buffer, mask);
            return (E)this.newBufferPoll(nextBuffer, index);
        }
        UnsafeRefArrayAccess.soRefElement(buffer, offset, null);
        this.soConsumerIndex(index + 2L);
        return (E)e;
    }

    @Override
    public E relaxedPeek() {
        long mask;
        Object[] buffer = this.consumerBuffer;
        long index = this.lpConsumerIndex();
        long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(index, mask = this.consumerMask);
        Object e = UnsafeRefArrayAccess.lvRefElement(buffer, offset);
        if (e == JUMP) {
            return (E)this.newBufferPeek(this.nextBuffer(buffer, mask), index);
        }
        return (E)e;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s) {
        int filled;
        long result = 0L;
        int capacity = this.capacity();
        do {
            if ((filled = this.fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH)) != 0) continue;
            return (int)result;
        } while ((result += (long)filled) <= (long)capacity);
        return (int)result;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
        long batchIndex;
        Object[] buffer;
        long mask;
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
        block5: while (true) {
            long producerLimit = this.lvProducerLimit();
            pIndex = this.lvProducerIndex();
            if ((pIndex & 1L) == 1L) continue;
            mask = this.producerMask;
            buffer = this.producerBuffer;
            batchIndex = Math.min(producerLimit, pIndex + 2L * (long)limit);
            if (pIndex >= producerLimit) {
                int result = this.offerSlowPath(mask, pIndex, producerLimit);
                switch (result) {
                    case 0: 
                    case 1: {
                        continue block5;
                    }
                    case 2: {
                        return 0;
                    }
                    case 3: {
                        this.resize(mask, buffer, pIndex, null, s);
                        return 1;
                    }
                }
            }
            if (this.casProducerIndex(pIndex, batchIndex)) break;
        }
        int claimedSlots = (int)((batchIndex - pIndex) / 2L);
        for (int i = 0; i < claimedSlots; ++i) {
            long offset = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(pIndex + 2L * (long)i, mask);
            UnsafeRefArrayAccess.soRefElement(buffer, offset, s.get());
        }
        return claimedSlots;
    }

    @Override
    public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c) {
        return this.drain(c, this.capacity());
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
        return MessagePassingQueueUtil.drain(this, c, limit);
    }

    @Override
    public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, wait, exit);
    }

    @Override
    public Iterator<E> iterator() {
        return new WeakIterator<Object>(this.consumerBuffer, this.lvConsumerIndex(), this.lvProducerIndex());
    }

    private void resize(long oldMask, E[] oldBuffer, long pIndex, E e, MessagePassingQueue.Supplier<E> s) {
        E[] newBuffer;
        assert (e != null && s == null || e == null || s != null);
        int newBufferLength = this.getNextBufferSize(oldBuffer);
        try {
            newBuffer = UnsafeRefArrayAccess.allocateRefArray(newBufferLength);
        } catch (OutOfMemoryError oom) {
            assert (this.lvProducerIndex() == pIndex + 1L);
            this.soProducerIndex(pIndex);
            throw oom;
        }
        this.producerBuffer = newBuffer;
        int newMask = newBufferLength - 2 << 1;
        this.producerMask = newMask;
        long offsetInOld = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(pIndex, oldMask);
        long offsetInNew = LinkedArrayQueueUtil.modifiedCalcCircularRefElementOffset(pIndex, newMask);
        UnsafeRefArrayAccess.soRefElement(newBuffer, offsetInNew, e == null ? s.get() : e);
        UnsafeRefArrayAccess.soRefElement(oldBuffer, BaseMpscLinkedArrayQueue.nextArrayOffset(oldMask), newBuffer);
        long cIndex = this.lvConsumerIndex();
        long availableInQueue = this.availableInQueue(pIndex, cIndex);
        RangeUtil.checkPositive(availableInQueue, "availableInQueue");
        this.soProducerLimit(pIndex + Math.min((long)newMask, availableInQueue));
        this.soProducerIndex(pIndex + 2L);
        UnsafeRefArrayAccess.soRefElement(oldBuffer, offsetInOld, JUMP);
    }

    protected abstract int getNextBufferSize(E[] var1);

    protected abstract long getCurrentBufferCapacity(long var1);

    private static class WeakIterator<E>
    implements Iterator<E> {
        private final long pIndex;
        private long nextIndex;
        private E nextElement;
        private E[] currentBuffer;
        private int mask;

        WeakIterator(E[] currentBuffer, long cIndex, long pIndex) {
            this.pIndex = pIndex >> 1;
            this.nextIndex = cIndex >> 1;
            this.setBuffer(currentBuffer);
            this.nextElement = this.getNext();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean hasNext() {
            return this.nextElement != null;
        }

        @Override
        public E next() {
            E e = this.nextElement;
            if (e == null) {
                throw new NoSuchElementException();
            }
            this.nextElement = this.getNext();
            return e;
        }

        private void setBuffer(E[] buffer) {
            this.currentBuffer = buffer;
            this.mask = LinkedArrayQueueUtil.length(buffer) - 2;
        }

        private E getNext() {
            while (this.nextIndex < this.pIndex) {
                long index;
                ++this.nextIndex;
                E e = UnsafeRefArrayAccess.lvRefElement(this.currentBuffer, UnsafeRefArrayAccess.calcCircularRefElementOffset(index, this.mask));
                if (e == null) continue;
                if (e != JUMP) {
                    return e;
                }
                int nextBufferIndex = this.mask + 1;
                E nextBuffer = UnsafeRefArrayAccess.lvRefElement(this.currentBuffer, UnsafeRefArrayAccess.calcRefElementOffset(nextBufferIndex));
                if (nextBuffer == BUFFER_CONSUMED || nextBuffer == null) {
                    return null;
                }
                this.setBuffer((Object[])nextBuffer);
                e = UnsafeRefArrayAccess.lvRefElement(this.currentBuffer, UnsafeRefArrayAccess.calcCircularRefElementOffset(index, this.mask));
                if (e == null) continue;
                return e;
            }
            return null;
        }
    }
}

