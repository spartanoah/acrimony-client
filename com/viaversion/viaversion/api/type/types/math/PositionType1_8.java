/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class PositionType1_8
extends Type<Position> {
    public PositionType1_8() {
        super(Position.class);
    }

    @Override
    public Position read(ByteBuf buffer) {
        long val2 = buffer.readLong();
        long x = val2 >> 38;
        long y = val2 << 26 >> 52;
        long z = val2 << 38 >> 38;
        return new Position((int)x, (short)y, (int)z);
    }

    @Override
    public void write(ByteBuf buffer, Position object) {
        buffer.writeLong(((long)object.x() & 0x3FFFFFFL) << 38 | ((long)object.y() & 0xFFFL) << 26 | (long)object.z() & 0x3FFFFFFL);
    }

    public static final class OptionalPositionType
    extends OptionalType<Position> {
        public OptionalPositionType() {
            super(Type.POSITION1_8);
        }
    }
}

