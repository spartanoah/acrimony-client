/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_14_1to1_14.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.protocols.protocol1_14_1to1_14.Protocol1_14_1To1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_14_1To1_14
extends EntityRewriter<ClientboundPackets1_14, Protocol1_14_1To1_14> {
    public MetadataRewriter1_14_1To1_14(Protocol1_14_1To1_14 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.filter().type(EntityTypes1_14.VILLAGER).addIndex(15);
        this.filter().type(EntityTypes1_14.WANDERING_TRADER).addIndex(15);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_14.getTypeFromId(type);
    }
}

