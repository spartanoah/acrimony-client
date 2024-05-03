/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13_1to1_13.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_13_1To1_13
extends EntityRewriter<ClientboundPackets1_13, Protocol1_13_1To1_13> {
    public MetadataRewriter1_13_1To1_13(Protocol1_13_1To1_13 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.registerMetaTypeHandler(Types1_13.META_TYPES.itemType, Types1_13.META_TYPES.blockStateType, null, Types1_13.META_TYPES.particleType);
        this.filter().type(EntityTypes1_13.EntityType.MINECART_ABSTRACT).index(9).handler((event, meta) -> {
            int data = (Integer)meta.value();
            meta.setValue(((Protocol1_13_1To1_13)this.protocol).getMappingData().getNewBlockStateId(data));
        });
        this.filter().type(EntityTypes1_13.EntityType.ABSTRACT_ARROW).addIndex(7);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, true);
    }
}

