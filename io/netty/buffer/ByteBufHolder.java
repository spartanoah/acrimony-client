/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public interface ByteBufHolder
extends ReferenceCounted {
    public ByteBuf content();

    public ByteBufHolder copy();

    public ByteBufHolder duplicate();

    @Override
    public ByteBufHolder retain();

    @Override
    public ByteBufHolder retain(int var1);
}

