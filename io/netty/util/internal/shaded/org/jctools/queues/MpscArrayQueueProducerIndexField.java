/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueL1Pad;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class MpscArrayQueueProducerIndexField<E>
extends MpscArrayQueueL1Pad<E> {
    private static final long P_INDEX_OFFSET = UnsafeAccess.fieldOffset(MpscArrayQueueProducerIndexField.class, "producerIndex");
    private volatile long producerIndex;

    MpscArrayQueueProducerIndexField(int capacity) {
        super(capacity);
    }

    @Override
    public final long lvProducerIndex() {
        return this.producerIndex;
    }

    final boolean casProducerIndex(long expect, long newValue) {
        return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
    }
}

