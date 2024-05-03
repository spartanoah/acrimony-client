/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.entities.storage.EntityData;
import com.viaversion.viabackwards.api.entities.storage.EntityPositionHandler;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.ChunkLightStorage;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.DifficultyStorage;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.EntityPositionStorage1_14;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.rewriter.meta.MetaHandler;

public class EntityPackets1_14
extends LegacyEntityRewriter<ClientboundPackets1_14, Protocol1_13_2To1_14> {
    private EntityPositionHandler positionHandler;

    public EntityPackets1_14(Protocol1_13_2To1_14 protocol) {
        super(protocol, Types1_13_2.META_TYPES.optionalComponentType, Types1_13_2.META_TYPES.booleanType);
    }

    @Override
    protected void addTrackedEntity(PacketWrapper wrapper, int entityId, EntityType type) throws Exception {
        super.addTrackedEntity(wrapper, entityId, type);
        if (type == EntityTypes1_14.PAINTING) {
            Position position = wrapper.get(Type.POSITION1_8, 0);
            this.positionHandler.cacheEntityPosition(wrapper, position.x(), position.y(), position.z(), true, false);
        } else if (wrapper.getId() != ClientboundPackets1_14.JOIN_GAME.getId()) {
            this.positionHandler.cacheEntityPosition(wrapper, true, false);
        }
    }

    @Override
    protected void registerPackets() {
        this.positionHandler = new EntityPositionHandler(this, EntityPositionStorage1_14.class, EntityPositionStorage1_14::new);
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.ENTITY_STATUS, wrapper -> {
            int entityId = wrapper.passthrough(Type.INT);
            byte status = wrapper.passthrough(Type.BYTE);
            if (status != 3) {
                return;
            }
            Object tracker = this.tracker(wrapper.user());
            EntityType entityType = tracker.entityType(entityId);
            if (entityType != EntityTypes1_14.PLAYER) {
                return;
            }
            for (int i = 0; i <= 5; ++i) {
                PacketWrapper equipmentPacket = wrapper.create(ClientboundPackets1_13.ENTITY_EQUIPMENT);
                equipmentPacket.write(Type.VAR_INT, entityId);
                equipmentPacket.write(Type.VAR_INT, i);
                equipmentPacket.write(Type.ITEM1_13_2, null);
                equipmentPacket.send(Protocol1_13_2To1_14.class);
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.ENTITY_TELEPORT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.handler(wrapper -> EntityPackets1_14.this.positionHandler.cacheEntityPosition(wrapper, false, false));
            }
        });
        PacketHandlers relativeMoveHandler = new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(wrapper -> {
                    double x = (double)wrapper.get(Type.SHORT, 0).shortValue() / 4096.0;
                    double y = (double)wrapper.get(Type.SHORT, 1).shortValue() / 4096.0;
                    double z = (double)wrapper.get(Type.SHORT, 2).shortValue() / 4096.0;
                    EntityPackets1_14.this.positionHandler.cacheEntityPosition(wrapper, x, y, z, false, true);
                });
            }
        };
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.ENTITY_POSITION, relativeMoveHandler);
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.ENTITY_POSITION_AND_ROTATION, relativeMoveHandler);
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_ENTITY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.UUID);
                this.map((Type)Type.VAR_INT, Type.BYTE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.BYTE);
                this.map(Type.BYTE);
                this.map(Type.INT);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(EntityPackets1_14.this.getObjectTrackerHandler());
                this.handler(wrapper -> {
                    int data;
                    EntityTypes1_13.ObjectType objectType;
                    byte id = wrapper.get(Type.BYTE, 0);
                    int mappedId = EntityPackets1_14.this.newEntityId(id);
                    EntityTypes1_13.EntityType entityType = EntityTypes1_13.getTypeFromId(mappedId, false);
                    if (entityType.isOrHasParent(EntityTypes1_13.EntityType.MINECART_ABSTRACT)) {
                        objectType = EntityTypes1_13.ObjectType.MINECART;
                        data = 0;
                        switch (entityType) {
                            case CHEST_MINECART: {
                                data = 1;
                                break;
                            }
                            case FURNACE_MINECART: {
                                data = 2;
                                break;
                            }
                            case TNT_MINECART: {
                                data = 3;
                                break;
                            }
                            case SPAWNER_MINECART: {
                                data = 4;
                                break;
                            }
                            case HOPPER_MINECART: {
                                data = 5;
                                break;
                            }
                            case COMMAND_BLOCK_MINECART: {
                                data = 6;
                            }
                        }
                        if (data != 0) {
                            wrapper.set(Type.INT, 0, data);
                        }
                    } else {
                        objectType = EntityTypes1_13.ObjectType.fromEntityType(entityType).orElse(null);
                    }
                    if (objectType == null) {
                        return;
                    }
                    wrapper.set(Type.BYTE, 0, (byte)objectType.getId());
                    data = wrapper.get(Type.INT, 0);
                    if (objectType == EntityTypes1_13.ObjectType.FALLING_BLOCK) {
                        int blockState = wrapper.get(Type.INT, 0);
                        int combined = ((Protocol1_13_2To1_14)EntityPackets1_14.this.protocol).getMappingData().getNewBlockStateId(blockState);
                        wrapper.set(Type.INT, 0, combined);
                    } else if (entityType.isOrHasParent(EntityTypes1_13.EntityType.ABSTRACT_ARROW)) {
                        wrapper.set(Type.INT, 0, data + 1);
                    }
                });
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_MOB, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.UUID);
                this.map(Type.VAR_INT);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.BYTE);
                this.map(Type.BYTE);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.map(Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST);
                this.handler(wrapper -> {
                    int type = wrapper.get(Type.VAR_INT, 1);
                    EntityType entityType = EntityTypes1_14.getTypeFromId(type);
                    EntityPackets1_14.this.addTrackedEntity(wrapper, wrapper.get(Type.VAR_INT, 0), entityType);
                    int oldId = EntityPackets1_14.this.newEntityId(type);
                    if (oldId == -1) {
                        EntityData entityData = EntityPackets1_14.this.entityDataForType(entityType);
                        if (entityData == null) {
                            ViaBackwards.getPlatform().getLogger().warning("Could not find 1.13.2 entity type for 1.14 entity type " + type + "/" + entityType);
                            wrapper.cancel();
                        } else {
                            wrapper.set(Type.VAR_INT, 1, entityData.replacementId());
                        }
                    } else {
                        wrapper.set(Type.VAR_INT, 1, oldId);
                    }
                });
                this.handler(EntityPackets1_14.this.getMobSpawnRewriter(Types1_13_2.METADATA_LIST));
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_EXPERIENCE_ORB, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.handler(wrapper -> EntityPackets1_14.this.addTrackedEntity(wrapper, wrapper.get(Type.VAR_INT, 0), EntityTypes1_14.EXPERIENCE_ORB));
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_GLOBAL_ENTITY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.BYTE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.handler(wrapper -> EntityPackets1_14.this.addTrackedEntity(wrapper, wrapper.get(Type.VAR_INT, 0), EntityTypes1_14.LIGHTNING_BOLT));
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_PAINTING, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.UUID);
                this.map(Type.VAR_INT);
                this.map(Type.POSITION1_14, Type.POSITION1_8);
                this.map(Type.BYTE);
                this.handler(wrapper -> EntityPackets1_14.this.addTrackedEntity(wrapper, wrapper.get(Type.VAR_INT, 0), EntityTypes1_14.PAINTING));
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.SPAWN_PLAYER, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.UUID);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.BYTE);
                this.map(Type.BYTE);
                this.map(Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST);
                this.handler(EntityPackets1_14.this.getTrackerAndMetaHandler(Types1_13_2.METADATA_LIST, EntityTypes1_14.PLAYER));
                this.handler(wrapper -> EntityPackets1_14.this.positionHandler.cacheEntityPosition(wrapper, true, false));
            }
        });
        this.registerRemoveEntities(ClientboundPackets1_14.DESTROY_ENTITIES);
        this.registerMetadataRewriter(ClientboundPackets1_14.ENTITY_METADATA, Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST);
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.JOIN_GAME, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.INT);
                this.handler(EntityPackets1_14.this.getTrackerHandler(EntityTypes1_14.PLAYER, Type.INT));
                this.handler(EntityPackets1_14.this.getDimensionHandler(1));
                this.handler(wrapper -> {
                    short difficulty = wrapper.user().get(DifficultyStorage.class).getDifficulty();
                    wrapper.write(Type.UNSIGNED_BYTE, difficulty);
                    wrapper.passthrough(Type.UNSIGNED_BYTE);
                    wrapper.passthrough(Type.STRING);
                    wrapper.read(Type.VAR_INT);
                });
            }
        });
        ((Protocol1_13_2To1_14)this.protocol).registerClientbound(ClientboundPackets1_14.RESPAWN, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                    short difficulty = wrapper.user().get(DifficultyStorage.class).getDifficulty();
                    wrapper.write(Type.UNSIGNED_BYTE, difficulty);
                    wrapper.user().get(ChunkLightStorage.class).clear();
                });
            }
        });
    }

    @Override
    protected void registerRewrites() {
        this.mapTypes(EntityTypes1_14.values(), EntityTypes1_13.EntityType.class);
        this.mapEntityTypeWithData(EntityTypes1_14.CAT, EntityTypes1_14.OCELOT).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.TRADER_LLAMA, EntityTypes1_14.LLAMA).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.FOX, EntityTypes1_14.WOLF).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.PANDA, EntityTypes1_14.POLAR_BEAR).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.PILLAGER, EntityTypes1_14.VILLAGER).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.WANDERING_TRADER, EntityTypes1_14.VILLAGER).jsonName();
        this.mapEntityTypeWithData(EntityTypes1_14.RAVAGER, EntityTypes1_14.COW).jsonName();
        this.filter().handler((event, meta) -> {
            int typeId = meta.metaType().typeId();
            if (typeId <= 15) {
                meta.setMetaType(Types1_13_2.META_TYPES.byId(typeId));
            }
        });
        this.registerMetaTypeHandler(Types1_13_2.META_TYPES.itemType, Types1_13_2.META_TYPES.blockStateType, null, null, Types1_13_2.META_TYPES.componentType, Types1_13_2.META_TYPES.optionalComponentType);
        this.filter().type(EntityTypes1_14.PILLAGER).cancel(15);
        this.filter().type(EntityTypes1_14.FOX).cancel(15);
        this.filter().type(EntityTypes1_14.FOX).cancel(16);
        this.filter().type(EntityTypes1_14.FOX).cancel(17);
        this.filter().type(EntityTypes1_14.FOX).cancel(18);
        this.filter().type(EntityTypes1_14.PANDA).cancel(15);
        this.filter().type(EntityTypes1_14.PANDA).cancel(16);
        this.filter().type(EntityTypes1_14.PANDA).cancel(17);
        this.filter().type(EntityTypes1_14.PANDA).cancel(18);
        this.filter().type(EntityTypes1_14.PANDA).cancel(19);
        this.filter().type(EntityTypes1_14.PANDA).cancel(20);
        this.filter().type(EntityTypes1_14.CAT).cancel(18);
        this.filter().type(EntityTypes1_14.CAT).cancel(19);
        this.filter().type(EntityTypes1_14.CAT).cancel(20);
        this.filter().handler((event, meta) -> {
            EntityType type = event.entityType();
            if (type == null) {
                return;
            }
            if (type.isOrHasParent(EntityTypes1_14.ABSTRACT_ILLAGER_BASE) || type == EntityTypes1_14.RAVAGER || type == EntityTypes1_14.WITCH) {
                int index = event.index();
                if (index == 14) {
                    event.cancel();
                } else if (index > 14) {
                    event.setIndex(index - 1);
                }
            }
        });
        this.filter().type(EntityTypes1_14.AREA_EFFECT_CLOUD).index(10).handler((event, meta) -> this.rewriteParticle((Particle)meta.getValue()));
        this.filter().type(EntityTypes1_14.FIREWORK_ROCKET).index(8).handler((event, meta) -> {
            meta.setMetaType(Types1_13_2.META_TYPES.varIntType);
            Integer value = (Integer)meta.getValue();
            if (value == null) {
                meta.setValue(0);
            }
        });
        this.filter().type(EntityTypes1_14.ABSTRACT_ARROW).removeIndex(9);
        this.filter().type(EntityTypes1_14.VILLAGER).cancel(15);
        MetaHandler villagerDataHandler = (event, meta) -> {
            VillagerData villagerData = (VillagerData)meta.getValue();
            meta.setTypeAndValue(Types1_13_2.META_TYPES.varIntType, this.villagerDataToProfession(villagerData));
            if (meta.id() == 16) {
                event.setIndex(15);
            }
        };
        this.filter().type(EntityTypes1_14.ZOMBIE_VILLAGER).index(18).handler(villagerDataHandler);
        this.filter().type(EntityTypes1_14.VILLAGER).index(16).handler(villagerDataHandler);
        this.filter().type(EntityTypes1_14.ABSTRACT_SKELETON).index(13).handler((event, meta) -> {
            byte value = (Byte)meta.getValue();
            if ((value & 4) != 0) {
                event.createExtraMeta(new Metadata(14, Types1_13_2.META_TYPES.booleanType, true));
            }
        });
        this.filter().type(EntityTypes1_14.ZOMBIE).index(13).handler((event, meta) -> {
            byte value = (Byte)meta.getValue();
            if ((value & 4) != 0) {
                event.createExtraMeta(new Metadata(16, Types1_13_2.META_TYPES.booleanType, true));
            }
        });
        this.filter().type(EntityTypes1_14.ZOMBIE).addIndex(16);
        this.filter().type(EntityTypes1_14.LIVINGENTITY).handler((event, meta) -> {
            int index = event.index();
            if (index == 12) {
                Position position = (Position)meta.getValue();
                if (position != null) {
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_13.USE_BED, null, event.user());
                    wrapper.write(Type.VAR_INT, event.entityId());
                    wrapper.write(Type.POSITION1_8, position);
                    try {
                        wrapper.scheduleSend(Protocol1_13_2To1_14.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                event.cancel();
            } else if (index > 12) {
                event.setIndex(index - 1);
            }
        });
        this.filter().removeIndex(6);
        this.filter().type(EntityTypes1_14.OCELOT).index(13).handler((event, meta) -> {
            event.setIndex(15);
            meta.setTypeAndValue(Types1_13_2.META_TYPES.varIntType, 0);
        });
        this.filter().type(EntityTypes1_14.CAT).handler((event, meta) -> {
            if (event.index() == 15) {
                meta.setValue(1);
            } else if (event.index() == 13) {
                meta.setValue((byte)((Byte)meta.getValue() & 4));
            }
        });
        this.filter().handler((event, meta) -> {
            if (meta.metaType().typeId() > 15) {
                throw new IllegalArgumentException("Unhandled metadata: " + meta);
            }
        });
    }

    public int villagerDataToProfession(VillagerData data) {
        switch (data.profession()) {
            case 1: 
            case 10: 
            case 13: 
            case 14: {
                return 3;
            }
            case 2: 
            case 8: {
                return 4;
            }
            case 3: 
            case 9: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 5: 
            case 6: 
            case 7: 
            case 12: {
                return 0;
            }
        }
        return 5;
    }

    @Override
    public EntityType typeFromId(int typeId) {
        return EntityTypes1_14.getTypeFromId(typeId);
    }
}

