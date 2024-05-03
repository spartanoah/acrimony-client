/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.HashMap;
import java.util.Map;
import tv.twitch.broadcast.StreamAPI;

public class FrameBuffer {
    private static Map<Long, FrameBuffer> s_OutstandingBuffers = new HashMap<Long, FrameBuffer>();
    protected long m_NativeAddress = 0L;
    protected int m_Size = 0;
    protected StreamAPI m_API = null;

    public static FrameBuffer lookupBuffer(long l) {
        return s_OutstandingBuffers.get(l);
    }

    protected static void registerBuffer(FrameBuffer frameBuffer) {
        if (frameBuffer.getAddress() != 0L) {
            s_OutstandingBuffers.put(frameBuffer.getAddress(), frameBuffer);
        }
    }

    protected static void unregisterBuffer(FrameBuffer frameBuffer) {
        s_OutstandingBuffers.remove(frameBuffer.getAddress());
    }

    FrameBuffer(StreamAPI streamAPI, int n) {
        this.m_NativeAddress = streamAPI.allocateFrameBuffer(n);
        if (this.m_NativeAddress == 0L) {
            return;
        }
        this.m_API = streamAPI;
        this.m_Size = n;
        FrameBuffer.registerBuffer(this);
    }

    public boolean getIsValid() {
        return this.m_NativeAddress != 0L;
    }

    public int getSize() {
        return this.m_Size;
    }

    public long getAddress() {
        return this.m_NativeAddress;
    }

    public void free() {
        if (this.m_NativeAddress != 0L) {
            FrameBuffer.unregisterBuffer(this);
            this.m_API.freeFrameBuffer(this.m_NativeAddress);
            this.m_NativeAddress = 0L;
        }
    }

    protected void finalize() {
        this.free();
    }
}

