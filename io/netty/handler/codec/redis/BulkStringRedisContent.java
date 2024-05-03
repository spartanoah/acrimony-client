/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.redis.RedisMessage;

public interface BulkStringRedisContent
extends RedisMessage,
ByteBufHolder {
    @Override
    public BulkStringRedisContent copy();

    @Override
    public BulkStringRedisContent duplicate();

    public BulkStringRedisContent retainedDuplicate();

    public BulkStringRedisContent replace(ByteBuf var1);

    @Override
    public BulkStringRedisContent retain();

    @Override
    public BulkStringRedisContent retain(int var1);

    public BulkStringRedisContent touch();

    public BulkStringRedisContent touch(Object var1);
}

