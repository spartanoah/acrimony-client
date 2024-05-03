/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ThreadLocalBufferManager;
import java.lang.ref.SoftReference;

public class BufferRecyclers {
    public static final String SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS = "com.fasterxml.jackson.core.util.BufferRecyclers.trackReusableBuffers";
    private static final ThreadLocalBufferManager _bufferRecyclerTracker;
    protected static final ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef;

    public static BufferRecycler getBufferRecycler() {
        BufferRecycler br;
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler bufferRecycler = br = ref == null ? null : ref.get();
        if (br == null) {
            br = new BufferRecycler();
            ref = _bufferRecyclerTracker != null ? _bufferRecyclerTracker.wrapAndTrack(br) : new SoftReference<BufferRecycler>(br);
            _recyclerRef.set(ref);
        }
        return br;
    }

    public static int releaseBuffers() {
        if (_bufferRecyclerTracker != null) {
            return _bufferRecyclerTracker.releaseBuffers();
        }
        return -1;
    }

    static {
        boolean trackReusableBuffers = false;
        try {
            trackReusableBuffers = "true".equals(System.getProperty(SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS));
        } catch (SecurityException securityException) {
            // empty catch block
        }
        _bufferRecyclerTracker = trackReusableBuffers ? ThreadLocalBufferManager.instance() : null;
        _recyclerRef = new ThreadLocal();
    }
}

