/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.metadata;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_14To1_13_2
extends EntityRewriter<ClientboundPackets1_13, Protocol1_14To1_13_2> {
    public MetadataRewriter1_14To1_13_2(Protocol1_14To1_13_2 protocol) {
        super(protocol);
        this.mapTypes(EntityTypes1_13.EntityType.values(), EntityTypes1_14.class);
        this.mapEntityType(EntityTypes1_13.EntityType.OCELOT, EntityTypes1_14.CAT);
    }

    @Override
    protected void registerRewrites() {
        this.filter().mapMetaType(Types1_14.META_TYPES::byId);
        this.registerMetaTypeHandler(Types1_14.META_TYPES.itemType, Types1_14.META_TYPES.blockStateType, null, Types1_14.META_TYPES.particleType);
        this.filter().type(EntityTypes1_14.ENTITY).addIndex(6);
        this.filter().type(EntityTypes1_14.LIVINGENTITY).addIndex(12);
        this.filter().type(EntityTypes1_14.LIVINGENTITY).index(8).handler((event, meta) -> {
            float value = ((Number)meta.getValue()).floatValue();
            if (Float.isNaN(value) && Via.getConfig().is1_14HealthNaNFix()) {
                meta.setValue(Float.valueOf(1.0f));
            }
        });
        this.filter().type(EntityTypes1_14.ABSTRACT_INSENTIENT).index(13).handler((event, meta) -> {
            EntityTracker1_14 tracker = (EntityTracker1_14)this.tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte)(((Number)meta.getValue()).byteValue() & 0xFFFFFFFB | tracker.getInsentientData(entityId) & 4));
            meta.setValue(tracker.getInsentientData(entityId));
        });
        this.filter().type(EntityTypes1_14.PLAYER).handler((event, meta) -> {
            EntityTracker1_14 tracker = (EntityTracker1_14)this.tracker(event.user());
            int entityId = event.entityId();
            if (entityId != tracker.clientEntityId()) {
                if (meta.id() == 0) {
                    byte flags = ((Number)meta.getValue()).byteValue();
                    tracker.setEntityFlags(entityId, flags);
                } else if (meta.id() == 7) {
                    tracker.setRiptide(entityId, (((Number)meta.getValue()).byteValue() & 4) != 0);
                }
                if (meta.id() == 0 || meta.id() == 7) {
                    event.createExtraMeta(new Metadata(6, Types1_14.META_TYPES.poseType, MetadataRewriter1_14To1_13_2.recalculatePlayerPose(entityId, tracker)));
                }
            }
        });
        this.filter().type(EntityTypes1_14.ZOMBIE).handler((event, meta) -> {
            if (meta.id() == 16) {
                EntityTracker1_14 tracker = (EntityTracker1_14)this.tracker(event.user());
                int entityId = event.entityId();
                tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | ((Boolean)meta.getValue() != false ? 4 : 0)));
                event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
                event.cancel();
            } else if (meta.id() > 16) {
                meta.setId(meta.id() - 1);
            }
        });
        this.filter().type(EntityTypes1_14.MINECART_ABSTRACT).index(10).handler((event, meta) -> {
            int data = (Integer)meta.value();
            meta.setValue(((Protocol1_14To1_13_2)this.protocol).getMappingData().getNewBlockStateId(data));
        });
        this.filter().type(EntityTypes1_14.HORSE).index(18).handler((event, meta) -> {
            event.cancel();
            int armorType = (Integer)meta.value();
            DataItem armorItem = null;
            if (armorType == 1) {
                armorItem = new DataItem(((Protocol1_14To1_13_2)this.protocol).getMappingData().getNewItemId(727), 1, 0, null);
            } else if (armorType == 2) {
                armorItem = new DataItem(((Protocol1_14To1_13_2)this.protocol).getMappingData().getNewItemId(728), 1, 0, null);
            } else if (armorType == 3) {
                armorItem = new DataItem(((Protocol1_14To1_13_2)this.protocol).getMappingData().getNewItemId(729), 1, 0, null);
            }
            PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_14.ENTITY_EQUIPMENT, null, event.user());
            equipmentPacket.write(Type.VAR_INT, event.entityId());
            equipmentPacket.write(Type.VAR_INT, 4);
            equipmentPacket.write(Type.ITEM1_13_2, armorItem);
            try {
                equipmentPacket.scheduleSend(Protocol1_14To1_13_2.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.filter().type(EntityTypes1_14.VILLAGER).index(15).handler((event, meta) -> meta.setTypeAndValue(Types1_14.META_TYPES.villagerDatatType, new VillagerData(2, MetadataRewriter1_14To1_13_2.getNewProfessionId((Integer)meta.value()), 0)));
        this.filter().type(EntityTypes1_14.ZOMBIE_VILLAGER).index(18).handler((event, meta) -> meta.setTypeAndValue(Types1_14.META_TYPES.villagerDatatType, new VillagerData(2, MetadataRewriter1_14To1_13_2.getNewProfessionId((Integer)meta.value()), 0)));
        this.filter().type(EntityTypes1_14.ABSTRACT_ARROW).addIndex(9);
        this.filter().type(EntityTypes1_14.FIREWORK_ROCKET).index(8).handler((event, meta) -> {
            meta.setMetaType(Types1_14.META_TYPES.optionalVarIntType);
            if (meta.getValue().equals(0)) {
                meta.setValue(null);
            }
        });
        this.filter().type(EntityTypes1_14.ABSTRACT_SKELETON).index(14).handler((event, meta) -> {
            EntityTracker1_14 tracker = (EntityTracker1_14)this.tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | ((Boolean)meta.getValue() != false ? 4 : 0)));
            event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();
        });
        this.filter().type(EntityTypes1_14.ABSTRACT_ILLAGER_BASE).index(14).handler((event, meta) -> {
            EntityTracker1_14 tracker = (EntityTracker1_14)this.tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | (((Number)meta.getValue()).byteValue() != 0 ? 4 : 0)));
            event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();
        });
        this.filter().handler((event, meta) -> {
            EntityType type = event.entityType();
            if ((type.is(EntityTypes1_14.WITCH) || type.is(EntityTypes1_14.RAVAGER) || type.isOrHasParent(EntityTypes1_14.ABSTRACT_ILLAGER_BASE)) && meta.id() >= 14) {
                meta.setId(meta.id() + 1);
            }
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_14.getTypeFromId(type);
    }

    private static boolean isSneaking(byte flags) {
        return (flags & 2) != 0;
    }

    private static boolean isSwimming(byte flags) {
        return (flags & 0x10) != 0;
    }

    private static int getNewProfessionId(int old) {
        switch (old) {
            case 0: {
                return 5;
            }
            case 1: {
                return 9;
            }
            case 2: {
                return 4;
            }
            case 3: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 5: {
                return 11;
            }
        }
        return 0;
    }

    private static boolean isFallFlying(int entityFlags) {
        return (entityFlags & 0x80) != 0;
    }

    public static int recalculatePlayerPose(int entityId, EntityTracker1_14 tracker) {
        byte flags = tracker.getEntityFlags(entityId);
        int pose = 0;
        if (MetadataRewriter1_14To1_13_2.isFallFlying(flags)) {
            pose = 1;
        } else if (tracker.isSleeping(entityId)) {
            pose = 2;
        } else if (MetadataRewriter1_14To1_13_2.isSwimming(flags)) {
            pose = 3;
        } else if (tracker.isRiptide(entityId)) {
            pose = 4;
        } else if (MetadataRewriter1_14To1_13_2.isSneaking(flags)) {
            pose = 5;
        }
        return pose;
    }
}

