/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

final class LinkedQueueNode<E> {
    private static final long NEXT_OFFSET = UnsafeAccess.fieldOffset(LinkedQueueNode.class, "next");
    private E value;
    private volatile LinkedQueueNode<E> next;

    LinkedQueueNode() {
        this(null);
    }

    LinkedQueueNode(E val2) {
        this.spValue(val2);
    }

    public E getAndNullValue() {
        E temp = this.lpValue();
        this.spValue(null);
        return temp;
    }

    public E lpValue() {
        return this.value;
    }

    public void spValue(E newValue) {
        this.value = newValue;
    }

    public void soNext(LinkedQueueNode<E> n) {
        UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, n);
    }

    public void spNext(LinkedQueueNode<E> n) {
        UnsafeAccess.UNSAFE.putObject((Object)this, NEXT_OFFSET, n);
    }

    public LinkedQueueNode<E> lvNext() {
        return this.next;
    }
}

