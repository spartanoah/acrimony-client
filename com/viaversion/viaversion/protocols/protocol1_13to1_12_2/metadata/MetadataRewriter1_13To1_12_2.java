/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.metadata;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.EntityTypeRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;

public class MetadataRewriter1_13To1_12_2
extends EntityRewriter<ClientboundPackets1_12_1, Protocol1_13To1_12_2> {
    public MetadataRewriter1_13To1_12_2(Protocol1_13To1_12_2 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.filter().mapMetaType(typeId -> Types1_13.META_TYPES.byId(typeId > 4 ? typeId + 1 : typeId));
        this.filter().metaType(Types1_13.META_TYPES.itemType).handler((event, meta) -> ((Protocol1_13To1_12_2)this.protocol).getItemRewriter().handleItemToClient((Item)meta.value()));
        this.filter().metaType(Types1_13.META_TYPES.blockStateType).handler((event, meta) -> meta.setValue(WorldPackets.toNewId((Integer)meta.value())));
        this.filter().index(0).handler((event, meta) -> meta.setValue((byte)((Byte)meta.getValue() & 0xFFFFFFEF)));
        this.filter().index(2).handler((event, meta) -> {
            if (meta.getValue() != null && !((String)meta.getValue()).isEmpty()) {
                meta.setTypeAndValue(Types1_13.META_TYPES.optionalComponentType, ComponentUtil.legacyToJson((String)meta.getValue()));
            } else {
                meta.setTypeAndValue(Types1_13.META_TYPES.optionalComponentType, null);
            }
        });
        this.filter().type(EntityTypes1_13.EntityType.ENDERMAN).index(12).handler((event, meta) -> {
            int stateId = (Integer)meta.value();
            int id = stateId & 0xFFF;
            int data = stateId >> 12 & 0xF;
            meta.setValue(id << 4 | data & 0xF);
        });
        this.filter().type(EntityTypes1_13.EntityType.WOLF).index(17).handler((event, meta) -> meta.setValue(15 - (Integer)meta.getValue()));
        this.filter().type(EntityTypes1_13.EntityType.ZOMBIE).addIndex(15);
        this.filter().type(EntityTypes1_13.EntityType.MINECART_ABSTRACT).index(9).handler((event, meta) -> {
            int oldId = (Integer)meta.value();
            int combined = (oldId & 0xFFF) << 4 | oldId >> 12 & 0xF;
            int newId = WorldPackets.toNewId(combined);
            meta.setValue(newId);
        });
        this.filter().type(EntityTypes1_13.EntityType.AREA_EFFECT_CLOUD).handler((event, meta) -> {
            if (meta.id() == 9) {
                int particleId = (Integer)meta.value();
                Metadata parameter1Meta = event.metaAtIndex(10);
                Metadata parameter2Meta = event.metaAtIndex(11);
                int parameter1 = parameter1Meta != null ? (Integer)parameter1Meta.value() : 0;
                int parameter2 = parameter2Meta != null ? (Integer)parameter2Meta.value() : 0;
                Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.getId() != -1) {
                    event.createExtraMeta(new Metadata(9, Types1_13.META_TYPES.particleType, particle));
                }
            }
            if (meta.id() >= 9) {
                event.cancel();
            }
        });
    }

    @Override
    public int newEntityId(int id) {
        return EntityTypeRewriter.getNewId(id);
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

