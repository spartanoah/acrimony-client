/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class PositionType1_14
extends Type<Position> {
    public PositionType1_14() {
        super(Position.class);
    }

    @Override
    public Position read(ByteBuf buffer) {
        long val2 = buffer.readLong();
        long x = val2 >> 38;
        long y = val2 << 52 >> 52;
        long z = val2 << 26 >> 38;
        return new Position((int)x, (int)y, (int)z);
    }

    @Override
    public void write(ByteBuf buffer, Position object) {
        buffer.writeLong(((long)object.x() & 0x3FFFFFFL) << 38 | (long)(object.y() & 0xFFF) | ((long)object.z() & 0x3FFFFFFL) << 12);
    }

    public static final class OptionalPosition1_14Type
    extends OptionalType<Position> {
        public OptionalPosition1_14Type() {
            super(Type.POSITION1_14);
        }
    }
}

