/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.conversion.converter;

import com.viaversion.viaversion.libs.opennbt.conversion.TagConverter;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteArrayTag;

public class ByteArrayTagConverter
implements TagConverter<ByteArrayTag, byte[]> {
    @Override
    public byte[] convert(ByteArrayTag tag) {
        return tag.getValue();
    }

    @Override
    public ByteArrayTag convert(byte[] value) {
        return new ByteArrayTag(value);
    }
}

