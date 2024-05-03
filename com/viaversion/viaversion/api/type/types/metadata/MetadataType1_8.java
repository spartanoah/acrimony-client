/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.metadata;

import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.type.types.metadata.OldMetaType;

public class MetadataType1_8
extends OldMetaType {
    @Override
    protected MetaType getType(int index) {
        return MetaType1_8.byId(index);
    }
}

