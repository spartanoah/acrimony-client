/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.LifecycleAware
 *  com.lmax.disruptor.Sequence
 *  com.lmax.disruptor.SequenceReportingEventHandler
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;

public class RingBufferLogEventHandler
implements SequenceReportingEventHandler<RingBufferLogEvent>,
LifecycleAware {
    private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
    private Sequence sequenceCallback;
    private int counter;
    private long threadId = -1L;

    public void setSequenceCallback(Sequence sequenceCallback) {
        this.sequenceCallback = sequenceCallback;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onEvent(RingBufferLogEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            if (event.isPopulated()) {
                event.execute(endOfBatch);
            }
        } finally {
            event.clear();
            this.notifyCallback(sequence);
        }
    }

    private void notifyCallback(long sequence) {
        if (++this.counter > 50) {
            this.sequenceCallback.set(sequence);
            this.counter = 0;
        }
    }

    public long getThreadId() {
        return this.threadId;
    }

    public void onStart() {
        this.threadId = Thread.currentThread().getId();
    }

    public void onShutdown() {
    }
}

