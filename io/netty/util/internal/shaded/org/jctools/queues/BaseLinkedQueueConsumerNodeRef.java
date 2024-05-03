/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueuePad1;
import io.netty.util.internal.shaded.org.jctools.queues.LinkedQueueNode;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class BaseLinkedQueueConsumerNodeRef<E>
extends BaseLinkedQueuePad1<E> {
    private static final long C_NODE_OFFSET = UnsafeAccess.fieldOffset(BaseLinkedQueueConsumerNodeRef.class, "consumerNode");
    private LinkedQueueNode<E> consumerNode;

    BaseLinkedQueueConsumerNodeRef() {
    }

    final void spConsumerNode(LinkedQueueNode<E> newValue) {
        this.consumerNode = newValue;
    }

    final LinkedQueueNode<E> lvConsumerNode() {
        return (LinkedQueueNode)UnsafeAccess.UNSAFE.getObjectVolatile(this, C_NODE_OFFSET);
    }

    final LinkedQueueNode<E> lpConsumerNode() {
        return this.consumerNode;
    }
}

