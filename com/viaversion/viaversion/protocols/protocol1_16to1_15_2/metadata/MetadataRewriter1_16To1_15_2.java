/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_16To1_15_2
extends EntityRewriter<ClientboundPackets1_15, Protocol1_16To1_15_2> {
    public MetadataRewriter1_16To1_15_2(Protocol1_16To1_15_2 protocol) {
        super(protocol);
        this.mapEntityType(EntityTypes1_15.ZOMBIE_PIGMAN, EntityTypes1_16.ZOMBIFIED_PIGLIN);
        this.mapTypes(EntityTypes1_15.values(), EntityTypes1_16.class);
    }

    @Override
    protected void registerRewrites() {
        this.filter().mapMetaType(Types1_16.META_TYPES::byId);
        this.registerMetaTypeHandler(Types1_16.META_TYPES.itemType, Types1_16.META_TYPES.blockStateType, null, Types1_16.META_TYPES.particleType);
        this.filter().type(EntityTypes1_16.MINECART_ABSTRACT).index(10).handler((metadatas, meta) -> {
            int data = (Integer)meta.value();
            meta.setValue(((Protocol1_16To1_15_2)this.protocol).getMappingData().getNewBlockStateId(data));
        });
        this.filter().type(EntityTypes1_16.ABSTRACT_ARROW).removeIndex(8);
        this.filter().type(EntityTypes1_16.WOLF).index(16).handler((event, meta) -> {
            byte mask = (Byte)meta.value();
            int angerTime = (mask & 2) != 0 ? Integer.MAX_VALUE : 0;
            event.createExtraMeta(new Metadata(20, Types1_16.META_TYPES.varIntType, angerTime));
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16.getTypeFromId(type);
    }
}

