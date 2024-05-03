/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class LongArrayType
extends Type<long[]> {
    public LongArrayType() {
        super(long[].class);
    }

    @Override
    public long[] read(ByteBuf buffer) throws Exception {
        int length = Type.VAR_INT.readPrimitive(buffer);
        long[] array = new long[length];
        for (int i = 0; i < array.length; ++i) {
            array[i] = buffer.readLong();
        }
        return array;
    }

    @Override
    public void write(ByteBuf buffer, long[] object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.length);
        for (long l : object) {
            buffer.writeLong(l);
        }
    }
}

