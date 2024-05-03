/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.queues;

public final class IndexedQueueSizeUtil {
    public static int size(IndexedQueue iq) {
        long currentProducerIndex;
        long before;
        long after = iq.lvConsumerIndex();
        do {
            before = after;
            currentProducerIndex = iq.lvProducerIndex();
        } while (before != (after = iq.lvConsumerIndex()));
        long size = currentProducerIndex - after;
        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (size < 0L) {
            return 0;
        }
        if (iq.capacity() != -1 && size > (long)iq.capacity()) {
            return iq.capacity();
        }
        return (int)size;
    }

    public static boolean isEmpty(IndexedQueue iq) {
        return iq.lvConsumerIndex() >= iq.lvProducerIndex();
    }

    public static interface IndexedQueue {
        public long lvConsumerIndex();

        public long lvProducerIndex();

        public int capacity();
    }
}

