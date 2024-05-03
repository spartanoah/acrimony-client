/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_15to1_14_4.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_14_4to1_14_3.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_15To1_14_4
extends EntityRewriter<ClientboundPackets1_14_4, Protocol1_15To1_14_4> {
    public MetadataRewriter1_15To1_14_4(Protocol1_15To1_14_4 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.registerMetaTypeHandler(Types1_14.META_TYPES.itemType, Types1_14.META_TYPES.blockStateType, null, Types1_14.META_TYPES.particleType);
        this.filter().type(EntityTypes1_15.MINECART_ABSTRACT).index(10).handler((metadatas, meta) -> {
            int data = (Integer)meta.value();
            meta.setValue(((Protocol1_15To1_14_4)this.protocol).getMappingData().getNewBlockStateId(data));
        });
        this.filter().type(EntityTypes1_15.LIVINGENTITY).addIndex(12);
        this.filter().type(EntityTypes1_15.WOLF).removeIndex(18);
    }

    @Override
    public int newEntityId(int id) {
        return EntityPackets.getNewEntityId(id);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_15.getTypeFromId(type);
    }
}

