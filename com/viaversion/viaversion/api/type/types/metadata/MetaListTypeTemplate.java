/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.metadata;

import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import java.util.List;

public abstract class MetaListTypeTemplate
extends Type<List<Metadata>> {
    protected MetaListTypeTemplate() {
        super("MetaData List", List.class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return MetaListTypeTemplate.class;
    }
}

