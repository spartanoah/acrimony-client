/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.BossBarStorage;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerPackets {
    public static void register(final Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_9.BOSSBAR, null, (PacketHandler)new PacketHandlers(){

            @Override
            public void register() {
                this.handler(packetWrapper -> {
                    packetWrapper.cancel();
                    UUID uuid = packetWrapper.read(Type.UUID);
                    int action = packetWrapper.read(Type.VAR_INT);
                    BossBarStorage bossBarStorage = packetWrapper.user().get(BossBarStorage.class);
                    if (action == 0) {
                        bossBarStorage.add(uuid, ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)), packetWrapper.read(Type.FLOAT).floatValue());
                        packetWrapper.read(Type.VAR_INT);
                        packetWrapper.read(Type.VAR_INT);
                        packetWrapper.read(Type.UNSIGNED_BYTE);
                    } else if (action == 1) {
                        bossBarStorage.remove(uuid);
                    } else if (action == 2) {
                        bossBarStorage.updateHealth(uuid, packetWrapper.read(Type.FLOAT).floatValue());
                    } else if (action == 3) {
                        String title = ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT));
                        bossBarStorage.updateTitle(uuid, title);
                    }
                });
            }
        });
        protocol.cancelClientbound(ClientboundPackets1_9.COOLDOWN);
        protocol.registerClientbound(ClientboundPackets1_9.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(packetWrapper -> {
                    String channel = packetWrapper.get(Type.STRING, 0);
                    if (channel.equalsIgnoreCase("MC|TrList")) {
                        packetWrapper.passthrough(Type.INT);
                        int size = packetWrapper.isReadable(Type.BYTE, 0) ? packetWrapper.passthrough(Type.BYTE).byteValue() : packetWrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
                        ItemRewriter itemRewriter = protocol.getItemRewriter();
                        for (int i = 0; i < size; ++i) {
                            packetWrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(packetWrapper.read(Type.ITEM1_8)));
                            packetWrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(packetWrapper.read(Type.ITEM1_8)));
                            boolean has3Items = packetWrapper.passthrough(Type.BOOLEAN);
                            if (has3Items) {
                                packetWrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(packetWrapper.read(Type.ITEM1_8)));
                            }
                            packetWrapper.passthrough(Type.BOOLEAN);
                            packetWrapper.passthrough(Type.INT);
                            packetWrapper.passthrough(Type.INT);
                        }
                    } else if (channel.equalsIgnoreCase("MC|BOpen")) {
                        packetWrapper.read(Type.VAR_INT);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.GAME_EVENT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.FLOAT);
                this.handler(packetWrapper -> {
                    short reason = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (reason == 3) {
                        packetWrapper.user().get(EntityTracker.class).setPlayerGamemode(packetWrapper.get(Type.FLOAT, 0).intValue());
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.JOIN_GAME, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.BYTE);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.STRING);
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
                    tracker.setPlayerId(packetWrapper.get(Type.INT, 0));
                    tracker.setPlayerGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 0).shortValue());
                    tracker.getClientEntityTypes().put(tracker.getPlayerId(), EntityTypes1_10.EntityType.ENTITY_HUMAN);
                });
                this.handler(packetWrapper -> {
                    ClientWorld world = packetWrapper.user().get(ClientWorld.class);
                    world.setEnvironment(packetWrapper.get(Type.BYTE, 0).byteValue());
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.BYTE);
                this.handler(packetWrapper -> {
                    PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
                    int teleportId = packetWrapper.read(Type.VAR_INT);
                    pos.setConfirmId(teleportId);
                    byte flags = packetWrapper.get(Type.BYTE, 0);
                    double x = packetWrapper.get(Type.DOUBLE, 0);
                    double y = packetWrapper.get(Type.DOUBLE, 1);
                    double z = packetWrapper.get(Type.DOUBLE, 2);
                    float yaw = packetWrapper.get(Type.FLOAT, 0).floatValue();
                    float pitch = packetWrapper.get(Type.FLOAT, 1).floatValue();
                    packetWrapper.set(Type.BYTE, 0, (byte)0);
                    if (flags != 0) {
                        if ((flags & 1) != 0) {
                            packetWrapper.set(Type.DOUBLE, 0, x += pos.getPosX());
                        }
                        if ((flags & 2) != 0) {
                            packetWrapper.set(Type.DOUBLE, 1, y += pos.getPosY());
                        }
                        if ((flags & 4) != 0) {
                            packetWrapper.set(Type.DOUBLE, 2, z += pos.getPosZ());
                        }
                        if ((flags & 8) != 0) {
                            packetWrapper.set(Type.FLOAT, 0, Float.valueOf(yaw += pos.getYaw()));
                        }
                        if ((flags & 0x10) != 0) {
                            packetWrapper.set(Type.FLOAT, 1, Float.valueOf(pitch += pos.getPitch()));
                        }
                    }
                    pos.setPos(x, y, z);
                    pos.setYaw(yaw);
                    pos.setPitch(pitch);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.RESPAWN, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.STRING);
                this.handler(packetWrapper -> packetWrapper.user().get(EntityTracker.class).setPlayerGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 1).shortValue()));
                this.handler(packetWrapper -> {
                    packetWrapper.user().get(BossBarStorage.class).updateLocation();
                    packetWrapper.user().get(BossBarStorage.class).changeWorld();
                });
                this.handler(packetWrapper -> {
                    ClientWorld world = packetWrapper.user().get(ClientWorld.class);
                    world.setEnvironment(packetWrapper.get(Type.INT, 0));
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.CHAT_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(packetWrapper -> {
                    String msg = packetWrapper.get(Type.STRING, 0);
                    if (msg.toLowerCase().startsWith("/offhand")) {
                        packetWrapper.cancel();
                        PacketWrapper swapItems = PacketWrapper.create(19, null, packetWrapper.user());
                        swapItems.write(Type.VAR_INT, 6);
                        swapItems.write(Type.POSITION1_8, new Position(0, 0, 0));
                        swapItems.write(Type.BYTE, (byte)-1);
                        PacketUtil.sendToServer(swapItems, Protocol1_8To1_9.class, true, true);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.INTERACT_ENTITY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.handler(packetWrapper -> {
                    int type = packetWrapper.get(Type.VAR_INT, 1);
                    if (type == 2) {
                        packetWrapper.passthrough(Type.FLOAT);
                        packetWrapper.passthrough(Type.FLOAT);
                        packetWrapper.passthrough(Type.FLOAT);
                    }
                    if (type == 2 || type == 0) {
                        packetWrapper.write(Type.VAR_INT, 0);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_MOVEMENT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    int playerId;
                    PacketWrapper animation = null;
                    while ((animation = protocol2.animationsToSend.poll()) != null) {
                        PacketUtil.sendToServer(animation, Protocol1_8To1_9.class, true, true);
                    }
                    EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
                    if (tracker.isInsideVehicle(playerId = tracker.getPlayerId())) {
                        packetWrapper.cancel();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    PacketWrapper animation = null;
                    while ((animation = protocol2.animationsToSend.poll()) != null) {
                        PacketUtil.sendToServer(animation, Protocol1_8To1_9.class, true, true);
                    }
                    PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
                    if (pos.getConfirmId() != -1) {
                        return;
                    }
                    pos.setPos(packetWrapper.get(Type.DOUBLE, 0), packetWrapper.get(Type.DOUBLE, 1), packetWrapper.get(Type.DOUBLE, 2));
                    pos.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
                });
                this.handler(packetWrapper -> packetWrapper.user().get(BossBarStorage.class).updateLocation());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_ROTATION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    PacketWrapper animation = null;
                    while ((animation = protocol2.animationsToSend.poll()) != null) {
                        PacketUtil.sendToServer(animation, Protocol1_8To1_9.class, true, true);
                    }
                    PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
                    if (pos.getConfirmId() != -1) {
                        return;
                    }
                    pos.setYaw(packetWrapper.get(Type.FLOAT, 0).floatValue());
                    pos.setPitch(packetWrapper.get(Type.FLOAT, 1).floatValue());
                    pos.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
                });
                this.handler(packetWrapper -> packetWrapper.user().get(BossBarStorage.class).updateLocation());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION_AND_ROTATION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    PacketWrapper animation = null;
                    while ((animation = protocol2.animationsToSend.poll()) != null) {
                        PacketUtil.sendToServer(animation, Protocol1_8To1_9.class, true, true);
                    }
                    double x = packetWrapper.get(Type.DOUBLE, 0);
                    double y = packetWrapper.get(Type.DOUBLE, 1);
                    double z = packetWrapper.get(Type.DOUBLE, 2);
                    float yaw = packetWrapper.get(Type.FLOAT, 0).floatValue();
                    float pitch = packetWrapper.get(Type.FLOAT, 1).floatValue();
                    boolean onGround = packetWrapper.get(Type.BOOLEAN, 0);
                    PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
                    if (pos.getConfirmId() != -1) {
                        if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z && pos.getYaw() == yaw && pos.getPitch() == pitch) {
                            PacketWrapper confirmTeleport = packetWrapper.create(0);
                            confirmTeleport.write(Type.VAR_INT, pos.getConfirmId());
                            PacketUtil.sendToServer(confirmTeleport, Protocol1_8To1_9.class, true, true);
                            pos.setConfirmId(-1);
                        }
                    } else {
                        pos.setPos(x, y, z);
                        pos.setYaw(yaw);
                        pos.setPitch(pitch);
                        pos.setOnGround(onGround);
                        packetWrapper.user().get(BossBarStorage.class).updateLocation();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_DIGGING, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.POSITION1_8);
                this.handler(packetWrapper -> {
                    int state = packetWrapper.get(Type.VAR_INT, 0);
                    if (state == 0) {
                        packetWrapper.user().get(BlockPlaceDestroyTracker.class).setMining(true);
                    } else if (state == 2) {
                        BlockPlaceDestroyTracker tracker = packetWrapper.user().get(BlockPlaceDestroyTracker.class);
                        tracker.setMining(false);
                        tracker.setLastMining(System.currentTimeMillis() + 100L);
                        packetWrapper.user().get(Cooldown.class).setLastHit(0L);
                    } else if (state == 1) {
                        BlockPlaceDestroyTracker tracker = packetWrapper.user().get(BlockPlaceDestroyTracker.class);
                        tracker.setMining(false);
                        tracker.setLastMining(0L);
                        packetWrapper.user().get(Cooldown.class).hit();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLAYER_BLOCK_PLACEMENT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.POSITION1_8);
                this.map((Type)Type.BYTE, Type.VAR_INT);
                this.read(Type.ITEM1_8);
                this.create(Type.VAR_INT, 0);
                this.map((Type)Type.BYTE, Type.UNSIGNED_BYTE);
                this.map((Type)Type.BYTE, Type.UNSIGNED_BYTE);
                this.map((Type)Type.BYTE, Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    if (packetWrapper.get(Type.VAR_INT, 0) == -1) {
                        packetWrapper.cancel();
                        PacketWrapper useItemMainHand = PacketWrapper.create(29, null, packetWrapper.user());
                        useItemMainHand.write(Type.VAR_INT, 0);
                        PacketUtil.sendToServer(useItemMainHand, Protocol1_8To1_9.class, true, true);
                        if (ViaRewind.getPlatform().isSword()) {
                            PacketWrapper useItemOffHand = PacketWrapper.create(29, null, packetWrapper.user());
                            useItemOffHand.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItemOffHand, Protocol1_8To1_9.class, true, true);
                        }
                    }
                });
                this.handler(packetWrapper -> {
                    if (packetWrapper.get(Type.VAR_INT, 0) != -1) {
                        packetWrapper.user().get(BlockPlaceDestroyTracker.class).place();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.HELD_ITEM_CHANGE, new PacketHandlers(){

            @Override
            public void register() {
                this.handler(packetWrapper -> packetWrapper.user().get(Cooldown.class).hit());
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.ANIMATION, new PacketHandlers(){

            @Override
            public void register() {
                this.handler(packetWrapper -> {
                    packetWrapper.cancel();
                    PacketWrapper animationPacket = PacketWrapper.create(26, null, packetWrapper.user());
                    animationPacket.write(Type.VAR_INT, 0);
                    PacketUtil.sendToServer(animationPacket, Protocol1_8To1_9.class, true, true);
                });
                this.handler(packetWrapper -> {
                    packetWrapper.user().get(BlockPlaceDestroyTracker.class).updateMining();
                    packetWrapper.user().get(Cooldown.class).hit();
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.ENTITY_ACTION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.handler(packetWrapper -> {
                    PlayerPosition pos;
                    int action = packetWrapper.get(Type.VAR_INT, 1);
                    if (action == 6) {
                        packetWrapper.set(Type.VAR_INT, 1, 7);
                    } else if (action == 0 && !(pos = packetWrapper.user().get(PlayerPosition.class)).isOnGround()) {
                        PacketWrapper elytra = PacketWrapper.create(20, null, packetWrapper.user());
                        elytra.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
                        elytra.write(Type.VAR_INT, 8);
                        elytra.write(Type.VAR_INT, 0);
                        PacketUtil.sendToServer(elytra, Protocol1_8To1_9.class, true, false);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.STEER_VEHICLE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    int playerId;
                    EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
                    int vehicle = tracker.getVehicle(playerId = tracker.getPlayerId());
                    if (vehicle != -1 && tracker.getClientEntityTypes().get(vehicle) == EntityTypes1_10.EntityType.BOAT) {
                        PacketWrapper steerBoat = PacketWrapper.create(17, null, packetWrapper.user());
                        float left = packetWrapper.get(Type.FLOAT, 0).floatValue();
                        float forward = packetWrapper.get(Type.FLOAT, 1).floatValue();
                        steerBoat.write(Type.BOOLEAN, forward != 0.0f || left < 0.0f);
                        steerBoat.write(Type.BOOLEAN, forward != 0.0f || left > 0.0f);
                        PacketUtil.sendToServer(steerBoat, Protocol1_8To1_9.class);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.UPDATE_SIGN, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.POSITION1_8);
                this.handler(packetWrapper -> {
                    for (int i = 0; i < 4; ++i) {
                        packetWrapper.write(Type.STRING, ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)));
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.TAB_COMPLETE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(packetWrapper -> packetWrapper.write(Type.BOOLEAN, false));
                this.map(Type.OPTIONAL_POSITION1_8);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.CLIENT_SETTINGS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.map(Type.BYTE);
                this.map((Type)Type.BYTE, Type.VAR_INT);
                this.map(Type.BOOLEAN);
                this.map(Type.UNSIGNED_BYTE);
                this.create(Type.VAR_INT, 1);
                this.handler(packetWrapper -> {
                    short flags = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    PacketWrapper updateSkin = PacketWrapper.create(28, null, packetWrapper.user());
                    updateSkin.write(Type.VAR_INT, packetWrapper.user().get(EntityTracker.class).getPlayerId());
                    ArrayList<Metadata> metadata = new ArrayList<Metadata>();
                    metadata.add(new Metadata(10, MetaType1_8.Byte, (byte)flags));
                    updateSkin.write(Types1_8.METADATA_LIST, metadata);
                    PacketUtil.sendPacket(updateSkin, Protocol1_8To1_9.class);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(packetWrapper -> {
                    String channel = packetWrapper.get(Type.STRING, 0);
                    if (channel.equalsIgnoreCase("MC|BEdit") || channel.equalsIgnoreCase("MC|BSign")) {
                        Item book = packetWrapper.passthrough(Type.ITEM);
                        book.setIdentifier(386);
                        CompoundTag tag = book.tag();
                        if (tag.contains("pages")) {
                            ListTag pages = (ListTag)tag.get("pages");
                            if (pages.size() > ViaRewind.getConfig().getMaxBookPages()) {
                                packetWrapper.user().disconnect("Too many book pages");
                                return;
                            }
                            for (int i = 0; i < pages.size(); ++i) {
                                StringTag page = (StringTag)pages.get(i);
                                String value = page.getValue();
                                if (value.length() > ViaRewind.getConfig().getMaxBookPageSize()) {
                                    packetWrapper.user().disconnect("Book page too large");
                                    return;
                                }
                                value = ChatUtil.jsonToLegacy(value);
                                page.setValue(value);
                            }
                        }
                    } else if (channel.equalsIgnoreCase("MC|AdvCdm")) {
                        channel = "MC|AdvCmd";
                        packetWrapper.set(Type.STRING, 0, "MC|AdvCmd");
                    }
                });
            }
        });
    }
}

