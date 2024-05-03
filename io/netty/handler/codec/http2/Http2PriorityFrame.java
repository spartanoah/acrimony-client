/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2StreamFrame;

public interface Http2PriorityFrame
extends Http2StreamFrame {
    public int streamDependency();

    public short weight();

    public boolean exclusive();

    @Override
    public Http2PriorityFrame stream(Http2FrameStream var1);
}

