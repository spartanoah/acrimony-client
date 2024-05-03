/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.util.BufferRecycler;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ThreadLocalBufferManager {
    private final Object RELEASE_LOCK = new Object();
    private final Map<SoftReference<BufferRecycler>, Boolean> _trackedRecyclers = new ConcurrentHashMap<SoftReference<BufferRecycler>, Boolean>();
    private final ReferenceQueue<BufferRecycler> _refQueue = new ReferenceQueue();

    ThreadLocalBufferManager() {
    }

    public static ThreadLocalBufferManager instance() {
        return ThreadLocalBufferManagerHolder.manager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int releaseBuffers() {
        Object object = this.RELEASE_LOCK;
        synchronized (object) {
            int count = 0;
            this.removeSoftRefsClearedByGc();
            for (SoftReference<BufferRecycler> ref : this._trackedRecyclers.keySet()) {
                ref.clear();
                ++count;
            }
            this._trackedRecyclers.clear();
            return count;
        }
    }

    public SoftReference<BufferRecycler> wrapAndTrack(BufferRecycler br) {
        SoftReference<BufferRecycler> newRef = new SoftReference<BufferRecycler>(br, this._refQueue);
        this._trackedRecyclers.put(newRef, true);
        this.removeSoftRefsClearedByGc();
        return newRef;
    }

    private void removeSoftRefsClearedByGc() {
        SoftReference clearedSoftRef;
        while ((clearedSoftRef = (SoftReference)this._refQueue.poll()) != null) {
            this._trackedRecyclers.remove(clearedSoftRef);
        }
    }

    private static final class ThreadLocalBufferManagerHolder {
        static final ThreadLocalBufferManager manager = new ThreadLocalBufferManager();

        private ThreadLocalBufferManagerHolder() {
        }
    }
}

