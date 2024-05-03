/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueuePad3;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;

abstract class BaseMpscLinkedAtomicArrayQueueColdProducerFields<E>
extends BaseMpscLinkedAtomicArrayQueuePad3<E> {
    private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueColdProducerFields> P_LIMIT_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueColdProducerFields.class, "producerLimit");
    private volatile long producerLimit;
    protected long producerMask;
    protected AtomicReferenceArray<E> producerBuffer;

    BaseMpscLinkedAtomicArrayQueueColdProducerFields() {
    }

    final long lvProducerLimit() {
        return this.producerLimit;
    }

    final boolean casProducerLimit(long expect, long newValue) {
        return P_LIMIT_UPDATER.compareAndSet(this, expect, newValue);
    }

    final void soProducerLimit(long newValue) {
        P_LIMIT_UPDATER.lazySet(this, newValue);
    }
}

