/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.impl.nio.ExpandableBuffer;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
abstract class AbstractSharedBuffer
extends ExpandableBuffer {
    final ReentrantLock lock;
    final Condition condition;
    volatile boolean endStream;
    volatile boolean aborted;

    public AbstractSharedBuffer(ReentrantLock lock, int initialBufferSize) {
        super(initialBufferSize);
        this.lock = Args.notNull(lock, "Lock");
        this.condition = lock.newCondition();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hasData() {
        this.lock.lock();
        try {
            boolean bl = super.hasData();
            return bl;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int capacity() {
        this.lock.lock();
        try {
            int n = super.capacity();
            return n;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int length() {
        this.lock.lock();
        try {
            int n = super.length();
            return n;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void abort() {
        this.lock.lock();
        try {
            this.endStream = true;
            this.aborted = true;
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reset() {
        if (this.aborted) {
            return;
        }
        this.lock.lock();
        try {
            this.setInputMode();
            this.buffer().clear();
            this.endStream = false;
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isEndStream() {
        this.lock.lock();
        try {
            boolean bl = this.endStream && !super.hasData();
            return bl;
        } finally {
            this.lock.unlock();
        }
    }
}

