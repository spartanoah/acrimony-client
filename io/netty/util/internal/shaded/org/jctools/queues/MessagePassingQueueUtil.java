/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;

public final class MessagePassingQueueUtil {
    public static <E> int drain(MessagePassingQueue<E> queue, MessagePassingQueue.Consumer<E> c, int limit) {
        E e;
        int i;
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative: " + limit);
        }
        if (limit == 0) {
            return 0;
        }
        for (i = 0; i < limit && (e = queue.relaxedPoll()) != null; ++i) {
            c.accept(e);
        }
        return i;
    }

    public static <E> int drain(MessagePassingQueue<E> queue, MessagePassingQueue.Consumer<E> c) {
        E e;
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        int i = 0;
        while ((e = queue.relaxedPoll()) != null) {
            ++i;
            c.accept(e);
        }
        return i;
    }

    public static <E> void drain(MessagePassingQueue<E> queue, MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        if (null == wait) {
            throw new IllegalArgumentException("wait is null");
        }
        if (null == exit) {
            throw new IllegalArgumentException("exit condition is null");
        }
        int idleCounter = 0;
        while (exit.keepRunning()) {
            E e = queue.relaxedPoll();
            if (e == null) {
                idleCounter = wait.idle(idleCounter);
                continue;
            }
            idleCounter = 0;
            c.accept(e);
        }
    }

    public static <E> void fill(MessagePassingQueue<E> q, MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        if (null == wait) {
            throw new IllegalArgumentException("waiter is null");
        }
        if (null == exit) {
            throw new IllegalArgumentException("exit condition is null");
        }
        int idleCounter = 0;
        while (exit.keepRunning()) {
            if (q.fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0) {
                idleCounter = wait.idle(idleCounter);
                continue;
            }
            idleCounter = 0;
        }
    }

    public static <E> int fillBounded(MessagePassingQueue<E> q, MessagePassingQueue.Supplier<E> s) {
        return MessagePassingQueueUtil.fillInBatchesToLimit(q, s, PortableJvmInfo.RECOMENDED_OFFER_BATCH, q.capacity());
    }

    public static <E> int fillInBatchesToLimit(MessagePassingQueue<E> q, MessagePassingQueue.Supplier<E> s, int batch, int limit) {
        int filled;
        long result = 0L;
        do {
            if ((filled = q.fill(s, batch)) != 0) continue;
            return (int)result;
        } while ((result += (long)filled) <= (long)limit);
        return (int)result;
    }

    public static <E> int fillUnbounded(MessagePassingQueue<E> q, MessagePassingQueue.Supplier<E> s) {
        return MessagePassingQueueUtil.fillInBatchesToLimit(q, s, PortableJvmInfo.RECOMENDED_OFFER_BATCH, 4096);
    }
}

