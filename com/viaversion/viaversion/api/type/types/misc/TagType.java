/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.NamedCompoundTagType;
import com.viaversion.viaversion.libs.opennbt.tag.TagRegistry;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;

public class TagType
extends Type<Tag> {
    public TagType() {
        super(Tag.class);
    }

    @Override
    public Tag read(ByteBuf buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == 0) {
            return null;
        }
        TagLimiter tagLimiter = TagLimiter.create(0x200000, 512);
        return TagRegistry.read(id, new ByteBufInputStream(buffer), tagLimiter, 0);
    }

    @Override
    public void write(ByteBuf buffer, Tag tag) throws IOException {
        NamedCompoundTagType.write(buffer, tag, null);
    }

    public static final class OptionalTagType
    extends OptionalType<Tag> {
        public OptionalTagType() {
            super(Type.TAG);
        }
    }
}

