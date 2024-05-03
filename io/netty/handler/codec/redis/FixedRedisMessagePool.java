/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.redis.AbstractStringRedisMessage;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisCodecUtil;
import io.netty.handler.codec.redis.RedisMessagePool;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.util.CharsetUtil;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import java.util.HashMap;
import java.util.Map;

public final class FixedRedisMessagePool
implements RedisMessagePool {
    private static final long MIN_CACHED_INTEGER_NUMBER = -1L;
    private static final long MAX_CACHED_INTEGER_NUMBER = 128L;
    private static final int SIZE_CACHED_INTEGER_NUMBER = 129;
    public static final FixedRedisMessagePool INSTANCE = new FixedRedisMessagePool();
    private final Map<ByteBuf, SimpleStringRedisMessage> byteBufToSimpleStrings;
    private final Map<String, SimpleStringRedisMessage> stringToSimpleStrings;
    private final Map<RedisReplyKey, SimpleStringRedisMessage> keyToSimpleStrings = new HashMap<RedisReplyKey, SimpleStringRedisMessage>(RedisReplyKey.values().length, 1.0f);
    private final Map<ByteBuf, ErrorRedisMessage> byteBufToErrors;
    private final Map<String, ErrorRedisMessage> stringToErrors;
    private final Map<RedisErrorKey, ErrorRedisMessage> keyToErrors;
    private final Map<ByteBuf, IntegerRedisMessage> byteBufToIntegers;
    private final LongObjectMap<IntegerRedisMessage> longToIntegers;
    private final LongObjectMap<byte[]> longToByteBufs;

    private FixedRedisMessagePool() {
        AbstractStringRedisMessage message;
        ByteBuf key;
        this.stringToSimpleStrings = new HashMap<String, SimpleStringRedisMessage>(RedisReplyKey.values().length, 1.0f);
        this.byteBufToSimpleStrings = new HashMap<ByteBuf, SimpleStringRedisMessage>(RedisReplyKey.values().length, 1.0f);
        for (RedisReplyKey redisReplyKey : RedisReplyKey.values()) {
            key = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(redisReplyKey.name().getBytes(CharsetUtil.UTF_8))).asReadOnly();
            message = new SimpleStringRedisMessage(new String(Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(redisReplyKey.name().getBytes(CharsetUtil.UTF_8))).array()));
            this.stringToSimpleStrings.put(redisReplyKey.name(), (SimpleStringRedisMessage)message);
            this.keyToSimpleStrings.put(redisReplyKey, (SimpleStringRedisMessage)message);
            this.byteBufToSimpleStrings.put(key, (SimpleStringRedisMessage)message);
        }
        this.keyToErrors = new HashMap<RedisErrorKey, ErrorRedisMessage>(RedisErrorKey.values().length, 1.0f);
        this.stringToErrors = new HashMap<String, ErrorRedisMessage>(RedisErrorKey.values().length, 1.0f);
        this.byteBufToErrors = new HashMap<ByteBuf, ErrorRedisMessage>(RedisErrorKey.values().length, 1.0f);
        for (Enum enum_ : RedisErrorKey.values()) {
            key = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(((RedisErrorKey)enum_).toString().getBytes(CharsetUtil.UTF_8))).asReadOnly();
            message = new ErrorRedisMessage(new String(Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(((RedisErrorKey)enum_).toString().getBytes(CharsetUtil.UTF_8))).array()));
            this.stringToErrors.put(((RedisErrorKey)enum_).toString(), (ErrorRedisMessage)message);
            this.keyToErrors.put((RedisErrorKey)enum_, (ErrorRedisMessage)message);
            this.byteBufToErrors.put(key, (ErrorRedisMessage)message);
        }
        this.byteBufToIntegers = new HashMap<ByteBuf, IntegerRedisMessage>(129, 1.0f);
        this.longToIntegers = new LongObjectHashMap<IntegerRedisMessage>(129, 1.0f);
        this.longToByteBufs = new LongObjectHashMap<byte[]>(129, 1.0f);
        for (long value = -1L; value < 128L; ++value) {
            byte[] keyBytes = RedisCodecUtil.longToAsciiBytes(value);
            ByteBuf byteBuf = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(keyBytes)).asReadOnly();
            IntegerRedisMessage cached = new IntegerRedisMessage(value);
            this.byteBufToIntegers.put(byteBuf, cached);
            this.longToIntegers.put(value, cached);
            this.longToByteBufs.put(value, keyBytes);
        }
    }

    @Override
    public SimpleStringRedisMessage getSimpleString(String content) {
        return this.stringToSimpleStrings.get(content);
    }

    public SimpleStringRedisMessage getSimpleString(RedisReplyKey key) {
        return this.keyToSimpleStrings.get((Object)key);
    }

    @Override
    public SimpleStringRedisMessage getSimpleString(ByteBuf content) {
        return this.byteBufToSimpleStrings.get(content);
    }

    @Override
    public ErrorRedisMessage getError(String content) {
        return this.stringToErrors.get(content);
    }

    public ErrorRedisMessage getError(RedisErrorKey key) {
        return this.keyToErrors.get((Object)key);
    }

    @Override
    public ErrorRedisMessage getError(ByteBuf content) {
        return this.byteBufToErrors.get(content);
    }

    @Override
    public IntegerRedisMessage getInteger(long value) {
        return this.longToIntegers.get(value);
    }

    @Override
    public IntegerRedisMessage getInteger(ByteBuf content) {
        return this.byteBufToIntegers.get(content);
    }

    @Override
    public byte[] getByteBufOfInteger(long value) {
        return this.longToByteBufs.get(value);
    }

    public static enum RedisErrorKey {
        ERR("ERR"),
        ERR_IDX("ERR index out of range"),
        ERR_NOKEY("ERR no such key"),
        ERR_SAMEOBJ("ERR source and destination objects are the same"),
        ERR_SYNTAX("ERR syntax error"),
        BUSY("BUSY Redis is busy running a script. You can only call SCRIPT KILL or SHUTDOWN NOSAVE."),
        BUSYKEY("BUSYKEY Target key name already exists."),
        EXECABORT("EXECABORT Transaction discarded because of previous errors."),
        LOADING("LOADING Redis is loading the dataset in memory"),
        MASTERDOWN("MASTERDOWN Link with MASTER is down and slave-serve-stale-data is set to 'no'."),
        MISCONF("MISCONF Redis is configured to save RDB snapshots, but is currently not able to persist on disk. Commands that may modify the data set are disabled. Please check Redis logs for details about the error."),
        NOREPLICAS("NOREPLICAS Not enough good slaves to write."),
        NOSCRIPT("NOSCRIPT No matching script. Please use EVAL."),
        OOM("OOM command not allowed when used memory > 'maxmemory'."),
        READONLY("READONLY You can't write against a read only slave."),
        WRONGTYPE("WRONGTYPE Operation against a key holding the wrong kind of value"),
        NOT_AUTH("NOAUTH Authentication required.");

        private final String msg;

        private RedisErrorKey(String msg) {
            this.msg = msg;
        }

        public String toString() {
            return this.msg;
        }
    }

    public static enum RedisReplyKey {
        OK,
        PONG,
        QUEUED;

    }
}

