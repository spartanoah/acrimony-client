/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseLinkedAtomicQueue;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode;

public class SpscLinkedAtomicQueue<E>
extends BaseLinkedAtomicQueue<E> {
    public SpscLinkedAtomicQueue() {
        LinkedQueueAtomicNode node = this.newNode();
        this.spProducerNode(node);
        this.spConsumerNode(node);
        node.soNext(null);
    }

    @Override
    public boolean offer(E e) {
        if (null == e) {
            throw new NullPointerException();
        }
        LinkedQueueAtomicNode<E> nextNode = this.newNode(e);
        LinkedQueueAtomicNode<E> oldNode = this.lpProducerNode();
        this.soProducerNode(nextNode);
        oldNode.soNext(nextNode);
        return true;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s) {
        return MessagePassingQueueUtil.fillUnbounded(this, s);
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
        LinkedQueueAtomicNode<E> tail;
        if (null == s) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative:" + limit);
        }
        if (limit == 0) {
            return 0;
        }
        LinkedQueueAtomicNode<E> head = tail = this.newNode(s.get());
        for (int i = 1; i < limit; ++i) {
            LinkedQueueAtomicNode<E> temp = this.newNode(s.get());
            tail.spNext(temp);
            tail = temp;
        }
        LinkedQueueAtomicNode<E> oldPNode = this.lpProducerNode();
        this.soProducerNode(tail);
        oldPNode.soNext(head);
        return limit;
    }

    @Override
    public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }
}

