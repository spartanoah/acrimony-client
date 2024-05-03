/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http2.Http2Frame;

public interface Http2GoAwayFrame
extends Http2Frame,
ByteBufHolder {
    public long errorCode();

    public int extraStreamIds();

    public Http2GoAwayFrame setExtraStreamIds(int var1);

    public int lastStreamId();

    @Override
    public ByteBuf content();

    @Override
    public Http2GoAwayFrame copy();

    @Override
    public Http2GoAwayFrame duplicate();

    public Http2GoAwayFrame retainedDuplicate();

    public Http2GoAwayFrame replace(ByteBuf var1);

    @Override
    public Http2GoAwayFrame retain();

    @Override
    public Http2GoAwayFrame retain(int var1);

    public Http2GoAwayFrame touch();

    public Http2GoAwayFrame touch(Object var1);
}

