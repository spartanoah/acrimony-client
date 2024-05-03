/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.SingleCoreIOReactor;

final class IOWorkers {
    IOWorkers() {
    }

    static Selector newSelector(SingleCoreIOReactor[] dispatchers) {
        return IOWorkers.isPowerOfTwo(dispatchers.length) ? new PowerOfTwoSelector(dispatchers) : new GenericSelector(dispatchers);
    }

    private static boolean isPowerOfTwo(int val2) {
        return (val2 & -val2) == val2;
    }

    private static void validate(SingleCoreIOReactor dispatcher) {
        if (dispatcher.getStatus() == IOReactorStatus.SHUT_DOWN) {
            throw new IOReactorShutdownException("I/O reactor has been shut down");
        }
    }

    private static final class GenericSelector
    implements Selector {
        private final AtomicInteger idx = new AtomicInteger(0);
        private final SingleCoreIOReactor[] dispatchers;

        GenericSelector(SingleCoreIOReactor[] dispatchers) {
            this.dispatchers = dispatchers;
        }

        @Override
        public SingleCoreIOReactor next() {
            SingleCoreIOReactor dispatcher = this.dispatchers[(this.idx.getAndIncrement() & Integer.MAX_VALUE) % this.dispatchers.length];
            IOWorkers.validate(dispatcher);
            return dispatcher;
        }
    }

    private static final class PowerOfTwoSelector
    implements Selector {
        private final AtomicInteger idx = new AtomicInteger(0);
        private final SingleCoreIOReactor[] dispatchers;

        PowerOfTwoSelector(SingleCoreIOReactor[] dispatchers) {
            this.dispatchers = dispatchers;
        }

        @Override
        public SingleCoreIOReactor next() {
            SingleCoreIOReactor dispatcher = this.dispatchers[this.idx.getAndIncrement() & this.dispatchers.length - 1];
            IOWorkers.validate(dispatcher);
            return dispatcher;
        }
    }

    static interface Selector {
        public SingleCoreIOReactor next();
    }
}

