/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

@Deprecated
public class VoidType
extends Type<Void>
implements TypeConverter<Void> {
    public VoidType() {
        super(Void.class);
    }

    @Override
    public Void read(ByteBuf buffer) {
        return null;
    }

    @Override
    public void write(ByteBuf buffer, Void object) {
    }

    @Override
    public Void from(Object o) {
        return null;
    }
}

