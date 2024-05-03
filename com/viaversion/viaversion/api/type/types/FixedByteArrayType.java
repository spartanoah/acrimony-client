/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class FixedByteArrayType
extends Type<byte[]> {
    private final int arrayLength;

    public FixedByteArrayType(int arrayLength) {
        super(byte[].class);
        this.arrayLength = arrayLength;
    }

    @Override
    public byte[] read(ByteBuf byteBuf) throws Exception {
        if (byteBuf.readableBytes() < this.arrayLength) {
            throw new RuntimeException("Readable bytes does not match expected!");
        }
        byte[] byteArray = new byte[this.arrayLength];
        byteBuf.readBytes(byteArray);
        return byteArray;
    }

    @Override
    public void write(ByteBuf byteBuf, byte[] bytes) throws Exception {
        byteBuf.writeBytes(bytes);
    }
}

