/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.AlertException
 *  com.lmax.disruptor.Sequence
 *  com.lmax.disruptor.SequenceBarrier
 *  com.lmax.disruptor.TimeoutException
 *  com.lmax.disruptor.WaitStrategy
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import java.util.concurrent.TimeUnit;

class TimeoutBlockingWaitStrategy
implements WaitStrategy {
    private final Object mutex = new Object();
    private final long timeoutInNanos;
    private static final int ONE_MILLISECOND_IN_NANOSECONDS = 1000000;

    public TimeoutBlockingWaitStrategy(long timeout, TimeUnit units) {
        this.timeoutInNanos = units.toNanos(timeout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence, SequenceBarrier barrier) throws AlertException, InterruptedException, TimeoutException {
        long availableSequence;
        long timeoutNanos = this.timeoutInNanos;
        if (cursorSequence.get() < sequence) {
            Object object = this.mutex;
            synchronized (object) {
                while (cursorSequence.get() < sequence) {
                    barrier.checkAlert();
                    if ((timeoutNanos = TimeoutBlockingWaitStrategy.awaitNanos(this.mutex, timeoutNanos)) > 0L) continue;
                    throw TimeoutException.INSTANCE;
                }
            }
        }
        while ((availableSequence = dependentSequence.get()) < sequence) {
            barrier.checkAlert();
        }
        return availableSequence;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void signalAllWhenBlocking() {
        Object object = this.mutex;
        synchronized (object) {
            this.mutex.notifyAll();
        }
    }

    public String toString() {
        return "TimeoutBlockingWaitStrategy{mutex=" + this.mutex + ", timeoutInNanos=" + this.timeoutInNanos + '}';
    }

    private static long awaitNanos(Object mutex, long timeoutNanos) throws InterruptedException {
        long millis = timeoutNanos / 1000000L;
        long nanos = timeoutNanos % 1000000L;
        long t0 = System.nanoTime();
        mutex.wait(millis, (int)nanos);
        long t1 = System.nanoTime();
        return timeoutNanos - (t1 - t0);
    }
}

