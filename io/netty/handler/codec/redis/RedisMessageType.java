/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.redis.RedisCodecException;

public enum RedisMessageType {
    INLINE_COMMAND(null, true),
    SIMPLE_STRING((byte)43, true),
    ERROR((byte)45, true),
    INTEGER((byte)58, true),
    BULK_STRING((byte)36, false),
    ARRAY_HEADER((byte)42, false);

    private final Byte value;
    private final boolean inline;

    private RedisMessageType(Byte value, boolean inline) {
        this.value = value;
        this.inline = inline;
    }

    public int length() {
        return this.value != null ? 1 : 0;
    }

    public boolean isInline() {
        return this.inline;
    }

    public static RedisMessageType readFrom(ByteBuf in, boolean decodeInlineCommands) {
        int initialIndex = in.readerIndex();
        RedisMessageType type = RedisMessageType.valueOf(in.readByte());
        if (type == INLINE_COMMAND) {
            if (!decodeInlineCommands) {
                throw new RedisCodecException("Decoding of inline commands is disabled");
            }
            in.readerIndex(initialIndex);
        }
        return type;
    }

    public void writeTo(ByteBuf out) {
        if (this.value == null) {
            return;
        }
        out.writeByte(this.value.byteValue());
    }

    private static RedisMessageType valueOf(byte value) {
        switch (value) {
            case 43: {
                return SIMPLE_STRING;
            }
            case 45: {
                return ERROR;
            }
            case 58: {
                return INTEGER;
            }
            case 36: {
                return BULK_STRING;
            }
            case 42: {
                return ARRAY_HEADER;
            }
        }
        return INLINE_COMMAND;
    }
}

