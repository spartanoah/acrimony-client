/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueuePad2;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;

abstract class BaseMpscLinkedAtomicArrayQueueConsumerFields<E>
extends BaseMpscLinkedAtomicArrayQueuePad2<E> {
    private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueConsumerFields> C_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueConsumerFields.class, "consumerIndex");
    private volatile long consumerIndex;
    protected long consumerMask;
    protected AtomicReferenceArray<E> consumerBuffer;

    BaseMpscLinkedAtomicArrayQueueConsumerFields() {
    }

    @Override
    public final long lvConsumerIndex() {
        return this.consumerIndex;
    }

    final long lpConsumerIndex() {
        return this.consumerIndex;
    }

    final void soConsumerIndex(long newValue) {
        C_INDEX_UPDATER.lazySet(this, newValue);
    }
}

