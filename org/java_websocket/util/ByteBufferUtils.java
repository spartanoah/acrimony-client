/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.util;

import java.nio.ByteBuffer;

public class ByteBufferUtils {
    private ByteBufferUtils() {
    }

    public static int transferByteBuffer(ByteBuffer source, ByteBuffer dest) {
        int toremain;
        if (source == null || dest == null) {
            throw new IllegalArgumentException();
        }
        int fremain = source.remaining();
        if (fremain > (toremain = dest.remaining())) {
            int limit = Math.min(fremain, toremain);
            source.limit(limit);
            dest.put(source);
            return limit;
        }
        dest.put(source);
        return fremain;
    }

    public static ByteBuffer getEmptyByteBuffer() {
        return ByteBuffer.allocate(0);
    }
}

