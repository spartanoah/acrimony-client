/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.concurrent;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class DefaultEventExecutorChooserFactory
implements EventExecutorChooserFactory {
    public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();

    private DefaultEventExecutorChooserFactory() {
    }

    @Override
    public EventExecutorChooserFactory.EventExecutorChooser newChooser(EventExecutor[] executors) {
        if (DefaultEventExecutorChooserFactory.isPowerOfTwo(executors.length)) {
            return new PowerOfTwoEventExecutorChooser(executors);
        }
        return new GenericEventExecutorChooser(executors);
    }

    private static boolean isPowerOfTwo(int val2) {
        return (val2 & -val2) == val2;
    }

    private static final class GenericEventExecutorChooser
    implements EventExecutorChooserFactory.EventExecutorChooser {
        private final AtomicLong idx = new AtomicLong();
        private final EventExecutor[] executors;

        GenericEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            return this.executors[(int)Math.abs(this.idx.getAndIncrement() % (long)this.executors.length)];
        }
    }

    private static final class PowerOfTwoEventExecutorChooser
    implements EventExecutorChooserFactory.EventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final EventExecutor[] executors;

        PowerOfTwoEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            return this.executors[this.idx.getAndIncrement() & this.executors.length - 1];
        }
    }
}

