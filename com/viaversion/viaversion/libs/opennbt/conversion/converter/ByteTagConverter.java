/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.conversion.converter;

import com.viaversion.viaversion.libs.opennbt.conversion.TagConverter;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;

public class ByteTagConverter
implements TagConverter<ByteTag, Byte> {
    @Override
    public Byte convert(ByteTag tag) {
        return tag.getValue();
    }

    @Override
    public ByteTag convert(Byte value) {
        return new ByteTag(value);
    }
}

