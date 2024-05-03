/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_12;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_12To1_11_1
extends EntityRewriter<ClientboundPackets1_9_3, Protocol1_12To1_11_1> {
    public MetadataRewriter1_12To1_11_1(Protocol1_12To1_11_1 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.filter().handler((event, meta) -> {
            if (meta.getValue() instanceof Item) {
                meta.setValue(((Protocol1_12To1_11_1)this.protocol).getItemRewriter().handleItemToClient((Item)meta.value()));
            }
        });
        this.filter().type(EntityTypes1_12.EntityType.EVOCATION_ILLAGER).index(12).toIndex(13);
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_12.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_12.getTypeFromId(type, true);
    }
}

