/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.conversion.converter;

import com.viaversion.viaversion.libs.opennbt.conversion.TagConverter;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.DoubleTag;

public class DoubleTagConverter
implements TagConverter<DoubleTag, Double> {
    @Override
    public Double convert(DoubleTag tag) {
        return tag.getValue();
    }

    @Override
    public DoubleTag convert(Double value) {
        return new DoubleTag(value);
    }
}

