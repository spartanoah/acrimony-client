/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueL2Pad;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

abstract class MpscAtomicArrayQueueConsumerIndexField<E>
extends MpscAtomicArrayQueueL2Pad<E> {
    private static final AtomicLongFieldUpdater<MpscAtomicArrayQueueConsumerIndexField> C_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(MpscAtomicArrayQueueConsumerIndexField.class, "consumerIndex");
    private volatile long consumerIndex;

    MpscAtomicArrayQueueConsumerIndexField(int capacity) {
        super(capacity);
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

