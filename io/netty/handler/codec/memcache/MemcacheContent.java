/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.memcache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.memcache.MemcacheObject;

public interface MemcacheContent
extends MemcacheObject,
ByteBufHolder {
    @Override
    public MemcacheContent copy();

    @Override
    public MemcacheContent duplicate();

    public MemcacheContent retainedDuplicate();

    public MemcacheContent replace(ByteBuf var1);

    @Override
    public MemcacheContent retain();

    @Override
    public MemcacheContent retain(int var1);

    public MemcacheContent touch();

    public MemcacheContent touch(Object var1);
}

