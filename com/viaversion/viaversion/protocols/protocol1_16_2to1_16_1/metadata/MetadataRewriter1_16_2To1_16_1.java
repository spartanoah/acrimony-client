/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_16_2To1_16_1
extends EntityRewriter<ClientboundPackets1_16, Protocol1_16_2To1_16_1> {
    public MetadataRewriter1_16_2To1_16_1(Protocol1_16_2To1_16_1 protocol) {
        super(protocol);
        this.mapTypes(EntityTypes1_16.values(), EntityTypes1_16_2.class);
    }

    @Override
    protected void registerRewrites() {
        this.registerMetaTypeHandler(Types1_16.META_TYPES.itemType, Types1_16.META_TYPES.blockStateType, null, Types1_16.META_TYPES.particleType);
        this.filter().type(EntityTypes1_16_2.MINECART_ABSTRACT).index(10).handler((metadatas, meta) -> {
            int data = (Integer)meta.value();
            meta.setValue(((Protocol1_16_2To1_16_1)this.protocol).getMappingData().getNewBlockStateId(data));
        });
        this.filter().type(EntityTypes1_16_2.ABSTRACT_PIGLIN).handler((metadatas, meta) -> {
            if (meta.id() == 15) {
                meta.setId(16);
            } else if (meta.id() == 16) {
                meta.setId(15);
            }
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16_2.getTypeFromId(type);
    }
}

