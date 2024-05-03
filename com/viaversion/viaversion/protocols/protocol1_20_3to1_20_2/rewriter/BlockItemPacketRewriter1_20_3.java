/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter.RecipeRewriter1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_3
extends ItemRewriter<ClientboundPackets1_20_2, ServerboundPackets1_20_3, Protocol1_20_3To1_20_2> {
    public BlockItemPacketRewriter1_20_3(Protocol1_20_3To1_20_2 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        BlockRewriter<ClientboundPackets1_20_2> blockRewriter = BlockRewriter.for1_20_2(this.protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_2.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_2.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_2.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_2.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_2.CHUNK_DATA, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_2.BLOCK_ENTITY_DATA);
        this.registerSetCooldown(ClientboundPackets1_20_2.COOLDOWN);
        this.registerWindowItems1_17_1(ClientboundPackets1_20_2.WINDOW_ITEMS);
        this.registerSetSlot1_17_1(ClientboundPackets1_20_2.SET_SLOT);
        this.registerEntityEquipmentArray(ClientboundPackets1_20_2.ENTITY_EQUIPMENT);
        this.registerClickWindow1_17_1(ServerboundPackets1_20_3.CLICK_WINDOW);
        this.registerTradeList1_19(ClientboundPackets1_20_2.TRADE_LIST);
        this.registerCreativeInvAction(ServerboundPackets1_20_3.CREATIVE_INVENTORY_ACTION);
        this.registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_2.WINDOW_PROPERTY);
        ((Protocol1_20_3To1_20_2)this.protocol).registerClientbound(ClientboundPackets1_20_2.SPAWN_PARTICLE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.BOOLEAN);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.DOUBLE);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.FLOAT);
                this.map(Type.INT);
                this.handler(wrapper -> {
                    ParticleMappings particleMappings;
                    int id = wrapper.get(Type.VAR_INT, 0);
                    if (id == (particleMappings = ((Protocol1_20_3To1_20_2)BlockItemPacketRewriter1_20_3.this.protocol).getMappingData().getParticleMappings()).id("vibration")) {
                        String resourceLocation = Key.stripMinecraftNamespace(wrapper.read(Type.STRING));
                        wrapper.write(Type.VAR_INT, resourceLocation.equals("block") ? 0 : 1);
                    }
                });
                this.handler(BlockItemPacketRewriter1_20_3.this.getSpawnParticleHandler(Type.VAR_INT));
            }
        });
        new RecipeRewriter1_20_2<ClientboundPackets1_20_2>(this.protocol){

            @Override
            public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
                int width = wrapper.read(Type.VAR_INT);
                int height = wrapper.read(Type.VAR_INT);
                wrapper.passthrough(Type.STRING);
                wrapper.passthrough(Type.VAR_INT);
                wrapper.write(Type.VAR_INT, width);
                wrapper.write(Type.VAR_INT, height);
                int ingredients = height * width;
                for (int i = 0; i < ingredients; ++i) {
                    this.handleIngredient(wrapper);
                }
                this.rewrite(wrapper.passthrough(this.itemType()));
                wrapper.passthrough(Type.BOOLEAN);
            }
        }.register(ClientboundPackets1_20_2.DECLARE_RECIPES);
        ((Protocol1_20_3To1_20_2)this.protocol).registerClientbound(ClientboundPackets1_20_2.EXPLOSION, wrapper -> {
            wrapper.passthrough(Type.DOUBLE);
            wrapper.passthrough(Type.DOUBLE);
            wrapper.passthrough(Type.DOUBLE);
            wrapper.passthrough(Type.FLOAT);
            int blocks = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blocks; ++i) {
                wrapper.passthrough(Type.BYTE);
                wrapper.passthrough(Type.BYTE);
                wrapper.passthrough(Type.BYTE);
            }
            wrapper.passthrough(Type.FLOAT);
            wrapper.passthrough(Type.FLOAT);
            wrapper.passthrough(Type.FLOAT);
            wrapper.write(Type.VAR_INT, 1);
            wrapper.write(Types1_20_3.PARTICLE, new Particle(((Protocol1_20_3To1_20_2)this.protocol).getMappingData().getParticleMappings().mappedId("explosion")));
            wrapper.write(Types1_20_3.PARTICLE, new Particle(((Protocol1_20_3To1_20_2)this.protocol).getMappingData().getParticleMappings().mappedId("explosion_emitter")));
            wrapper.write(Type.STRING, "minecraft:entity.generic.explode");
            wrapper.write(Type.OPTIONAL_FLOAT, null);
        });
    }

    @Override
    public @Nullable Item handleItemToClient(@Nullable Item item) {
        if (item == null) {
            return null;
        }
        CompoundTag tag = item.tag();
        if (tag != null && item.identifier() == 1047) {
            this.updatePages(tag, "pages");
            this.updatePages(tag, "filtered_pages");
        }
        return super.handleItemToClient(item);
    }

    private void updatePages(CompoundTag tag, String key) {
        ListTag pages = tag.getListTag(key);
        if (pages == null) {
            return;
        }
        for (Tag pageTag : pages) {
            if (!(pageTag instanceof StringTag)) continue;
            StringTag stringTag = (StringTag)pageTag;
            try {
                JsonElement updatedComponent = ComponentUtil.convertJson(stringTag.getValue(), ComponentUtil.SerializerVersion.V1_19_4, ComponentUtil.SerializerVersion.V1_20_3);
                stringTag.setValue(updatedComponent.toString());
            } catch (Exception e) {
                Via.getManager().debugHandler().error("Error during book conversion", e);
            }
        }
    }
}

