/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueuePad1;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class BaseMpscLinkedArrayQueueProducerFields<E>
extends BaseMpscLinkedArrayQueuePad1<E> {
    private static final long P_INDEX_OFFSET = UnsafeAccess.fieldOffset(BaseMpscLinkedArrayQueueProducerFields.class, "producerIndex");
    private volatile long producerIndex;

    BaseMpscLinkedArrayQueueProducerFields() {
    }

    @Override
    public final long lvProducerIndex() {
        return this.producerIndex;
    }

    final void soProducerIndex(long newValue) {
        UnsafeAccess.UNSAFE.putOrderedLong(this, P_INDEX_OFFSET, newValue);
    }

    final boolean casProducerIndex(long expect, long newValue) {
        return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
    }
}

