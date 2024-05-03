/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueuePad0;
import io.netty.util.internal.shaded.org.jctools.queues.LinkedQueueNode;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class BaseLinkedQueueProducerNodeRef<E>
extends BaseLinkedQueuePad0<E> {
    static final long P_NODE_OFFSET = UnsafeAccess.fieldOffset(BaseLinkedQueueProducerNodeRef.class, "producerNode");
    private volatile LinkedQueueNode<E> producerNode;

    BaseLinkedQueueProducerNodeRef() {
    }

    final void spProducerNode(LinkedQueueNode<E> newValue) {
        UnsafeAccess.UNSAFE.putObject((Object)this, P_NODE_OFFSET, newValue);
    }

    final void soProducerNode(LinkedQueueNode<E> newValue) {
        UnsafeAccess.UNSAFE.putOrderedObject(this, P_NODE_OFFSET, newValue);
    }

    final LinkedQueueNode<E> lvProducerNode() {
        return this.producerNode;
    }

    final boolean casProducerNode(LinkedQueueNode<E> expect, LinkedQueueNode<E> newValue) {
        return UnsafeAccess.UNSAFE.compareAndSwapObject(this, P_NODE_OFFSET, expect, newValue);
    }

    final LinkedQueueNode<E> lpProducerNode() {
        return this.producerNode;
    }
}

