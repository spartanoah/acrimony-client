/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NamedCompoundTagType
extends Type<CompoundTag> {
    public static final int MAX_NBT_BYTES = 0x200000;
    public static final int MAX_NESTING_LEVEL = 512;

    public NamedCompoundTagType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(ByteBuf buffer) throws IOException {
        return NamedCompoundTagType.read(buffer, true);
    }

    @Override
    public void write(ByteBuf buffer, CompoundTag object) throws IOException {
        NamedCompoundTagType.write(buffer, object, "");
    }

    public static CompoundTag read(ByteBuf buffer, boolean readName) throws IOException {
        byte id = buffer.readByte();
        if (id == 0) {
            return null;
        }
        if (id != 10) {
            throw new IOException(String.format("Expected root tag to be a CompoundTag, was %s", id));
        }
        if (readName) {
            buffer.skipBytes(buffer.readUnsignedShort());
        }
        TagLimiter tagLimiter = TagLimiter.create(0x200000, 512);
        return CompoundTag.read(new ByteBufInputStream(buffer), tagLimiter, 0);
    }

    public static void write(ByteBuf buffer, Tag tag, @Nullable String name) throws IOException {
        if (tag == null) {
            buffer.writeByte(0);
            return;
        }
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        out.writeByte(tag.getTagId());
        if (name != null) {
            out.writeUTF(name);
        }
        tag.write(out);
    }
}

