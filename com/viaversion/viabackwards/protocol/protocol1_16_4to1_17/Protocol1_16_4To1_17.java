/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_16_4to1_17;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappings;
import com.viaversion.viabackwards.api.rewriters.SoundRewriter;
import com.viaversion.viabackwards.api.rewriters.TranslatableRewriter;
import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.packets.BlockItemPackets1_17;
import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.packets.EntityPackets1_17;
import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.storage.PingRequests;
import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.storage.PlayerLastCursorItem;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_17;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.IdRewriteFunction;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Protocol1_16_4To1_17
extends BackwardsProtocol<ClientboundPackets1_17, ClientboundPackets1_16_2, ServerboundPackets1_17, ServerboundPackets1_16_2> {
    public static final BackwardsMappings MAPPINGS = new BackwardsMappings("1.17", "1.16.2", Protocol1_17To1_16_4.class);
    private static final RegistryType[] TAG_REGISTRY_TYPES = new RegistryType[]{RegistryType.BLOCK, RegistryType.ITEM, RegistryType.FLUID, RegistryType.ENTITY};
    private static final int[] EMPTY_ARRAY = new int[0];
    private final EntityPackets1_17 entityRewriter = new EntityPackets1_17(this);
    private final BlockItemPackets1_17 blockItemPackets = new BlockItemPackets1_17(this);
    private final TranslatableRewriter<ClientboundPackets1_17> translatableRewriter = new TranslatableRewriter<ClientboundPackets1_17>(this, ComponentRewriter.ReadType.JSON);

    public Protocol1_16_4To1_17() {
        super(ClientboundPackets1_17.class, ClientboundPackets1_16_2.class, ServerboundPackets1_17.class, ServerboundPackets1_16_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();
        this.translatableRewriter.registerComponentPacket(ClientboundPackets1_17.CHAT_MESSAGE);
        this.translatableRewriter.registerBossBar(ClientboundPackets1_17.BOSSBAR);
        this.translatableRewriter.registerComponentPacket(ClientboundPackets1_17.DISCONNECT);
        this.translatableRewriter.registerTabList(ClientboundPackets1_17.TAB_LIST);
        this.translatableRewriter.registerOpenWindow(ClientboundPackets1_17.OPEN_WINDOW);
        this.translatableRewriter.registerPing();
        SoundRewriter<ClientboundPackets1_17> soundRewriter = new SoundRewriter<ClientboundPackets1_17>(this);
        soundRewriter.registerSound(ClientboundPackets1_17.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_17.ENTITY_SOUND);
        soundRewriter.registerNamedSound(ClientboundPackets1_17.NAMED_SOUND);
        soundRewriter.registerStopSound(ClientboundPackets1_17.STOP_SOUND);
        TagRewriter<ClientboundPackets1_17> tagRewriter = new TagRewriter<ClientboundPackets1_17>(this);
        this.registerClientbound(ClientboundPackets1_17.TAGS, (PacketWrapper wrapper) -> {
            HashMap tags = new HashMap();
            int length = wrapper.read(Type.VAR_INT);
            for (int i = 0; i < length; ++i) {
                String resourceKey = Key.stripMinecraftNamespace(wrapper.read(Type.STRING));
                ArrayList<TagData> tagList = new ArrayList<TagData>();
                tags.put(resourceKey, tagList);
                int tagLength = wrapper.read(Type.VAR_INT);
                for (int j = 0; j < tagLength; ++j) {
                    String identifier = wrapper.read(Type.STRING);
                    int[] entries = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
                    tagList.add(new TagData(identifier, entries));
                }
            }
            for (RegistryType type : TAG_REGISTRY_TYPES) {
                List tagList = (List)tags.get(type.resourceLocation());
                if (tagList == null) {
                    wrapper.write(Type.VAR_INT, 0);
                    continue;
                }
                IdRewriteFunction rewriter = tagRewriter.getRewriter(type);
                wrapper.write(Type.VAR_INT, tagList.size());
                for (TagData tagData : tagList) {
                    int[] entries = tagData.entries();
                    if (rewriter != null) {
                        IntArrayList idList = new IntArrayList(entries.length);
                        for (int id : entries) {
                            int mappedId = rewriter.rewrite(id);
                            if (mappedId == -1) continue;
                            idList.add(mappedId);
                        }
                        entries = idList.toArray(EMPTY_ARRAY);
                    }
                    wrapper.write(Type.STRING, tagData.identifier());
                    wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, entries);
                }
            }
        });
        new StatisticsRewriter<ClientboundPackets1_17>(this).register(ClientboundPackets1_17.STATISTICS);
        this.registerClientbound(ClientboundPackets1_17.RESOURCE_PACK, (PacketWrapper wrapper) -> {
            wrapper.passthrough(Type.STRING);
            wrapper.passthrough(Type.STRING);
            wrapper.read(Type.BOOLEAN);
            wrapper.read(Type.OPTIONAL_COMPONENT);
        });
        this.registerClientbound(ClientboundPackets1_17.EXPLOSION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.handler(wrapper -> wrapper.write(Type.INT, wrapper.read(Type.VAR_INT)));
            }
        });
        this.registerClientbound(ClientboundPackets1_17.SPAWN_POSITION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.POSITION1_14);
                this.handler(wrapper -> wrapper.read(Type.FLOAT));
            }
        });
        this.registerClientbound(ClientboundPackets1_17.PING, null, (PacketWrapper wrapper) -> {
            wrapper.cancel();
            int id = wrapper.read(Type.INT);
            short shortId = (short)id;
            if (id == shortId && ViaBackwards.getConfig().handlePingsAsInvAcknowledgements()) {
                wrapper.user().get(PingRequests.class).addId(shortId);
                PacketWrapper acknowledgementPacket = wrapper.create(ClientboundPackets1_16_2.WINDOW_CONFIRMATION);
                acknowledgementPacket.write(Type.UNSIGNED_BYTE, (short)0);
                acknowledgementPacket.write(Type.SHORT, shortId);
                acknowledgementPacket.write(Type.BOOLEAN, false);
                acknowledgementPacket.send(Protocol1_16_4To1_17.class);
                return;
            }
            PacketWrapper pongPacket = wrapper.create(ServerboundPackets1_17.PONG);
            pongPacket.write(Type.INT, id);
            pongPacket.sendToServer(Protocol1_16_4To1_17.class);
        });
        this.registerServerbound(ServerboundPackets1_16_2.CLIENT_SETTINGS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.map(Type.BYTE);
                this.map(Type.VAR_INT);
                this.map(Type.BOOLEAN);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.VAR_INT);
                this.handler(wrapper -> wrapper.write(Type.BOOLEAN, false));
            }
        });
        this.mergePacket(ClientboundPackets1_17.TITLE_TEXT, ClientboundPackets1_16_2.TITLE, 0);
        this.mergePacket(ClientboundPackets1_17.TITLE_SUBTITLE, ClientboundPackets1_16_2.TITLE, 1);
        this.mergePacket(ClientboundPackets1_17.ACTIONBAR, ClientboundPackets1_16_2.TITLE, 2);
        this.mergePacket(ClientboundPackets1_17.TITLE_TIMES, ClientboundPackets1_16_2.TITLE, 3);
        this.registerClientbound(ClientboundPackets1_17.CLEAR_TITLES, ClientboundPackets1_16_2.TITLE, (PacketWrapper wrapper) -> {
            if (wrapper.read(Type.BOOLEAN).booleanValue()) {
                wrapper.write(Type.VAR_INT, 5);
            } else {
                wrapper.write(Type.VAR_INT, 4);
            }
        });
        this.cancelClientbound(ClientboundPackets1_17.ADD_VIBRATION_SIGNAL);
    }

    @Override
    public void init(UserConnection user) {
        this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_17.PLAYER));
        user.put(new PingRequests());
        user.put(new PlayerLastCursorItem());
    }

    @Override
    public BackwardsMappings getMappingData() {
        return MAPPINGS;
    }

    @Override
    public TranslatableRewriter<ClientboundPackets1_17> getTranslatableRewriter() {
        return this.translatableRewriter;
    }

    public void mergePacket(ClientboundPackets1_17 newPacketType, ClientboundPackets1_16_2 oldPacketType, int type) {
        this.registerClientbound(newPacketType, oldPacketType, (PacketWrapper wrapper) -> wrapper.write(Type.VAR_INT, type));
    }

    public EntityPackets1_17 getEntityRewriter() {
        return this.entityRewriter;
    }

    public BlockItemPackets1_17 getItemRewriter() {
        return this.blockItemPackets;
    }
}

