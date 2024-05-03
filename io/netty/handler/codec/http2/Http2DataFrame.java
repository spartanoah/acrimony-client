/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http2.Http2StreamFrame;

public interface Http2DataFrame
extends Http2StreamFrame,
ByteBufHolder {
    public int padding();

    @Override
    public ByteBuf content();

    public int initialFlowControlledBytes();

    public boolean isEndStream();

    @Override
    public Http2DataFrame copy();

    @Override
    public Http2DataFrame duplicate();

    public Http2DataFrame retainedDuplicate();

    public Http2DataFrame replace(ByteBuf var1);

    @Override
    public Http2DataFrame retain();

    @Override
    public Http2DataFrame retain(int var1);

    public Http2DataFrame touch();

    public Http2DataFrame touch(Object var1);
}

