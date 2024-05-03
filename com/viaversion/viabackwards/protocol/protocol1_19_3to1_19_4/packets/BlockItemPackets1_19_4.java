/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_19_3to1_19_4.packets;

import com.viaversion.viabackwards.api.rewriters.ItemRewriter;
import com.viaversion.viabackwards.protocol.protocol1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.rewriter.RecipeRewriter1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.Key;

public final class BlockItemPackets1_19_4
extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_19_3, Protocol1_19_3To1_19_4> {
    public BlockItemPackets1_19_4(Protocol1_19_3To1_19_4 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        BlockRewriter<ClientboundPackets1_19_4> blockRewriter = BlockRewriter.for1_14(this.protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_19_4.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_19_4.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_19_4.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_19_4.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_19_4.CHUNK_DATA, ChunkType1_18::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA);
        ((Protocol1_19_3To1_19_4)this.protocol).registerClientbound(ClientboundPackets1_19_4.OPEN_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.map(Type.COMPONENT);
                this.handler(wrapper -> {
                    int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType == 21) {
                        wrapper.cancel();
                    } else if (windowType > 21) {
                        wrapper.set(Type.VAR_INT, 1, windowType - 1);
                    }
                    ((Protocol1_19_3To1_19_4)BlockItemPackets1_19_4.this.protocol).getTranslatableRewriter().processText(wrapper.get(Type.COMPONENT, 0));
                });
            }
        });
        this.registerSetCooldown(ClientboundPackets1_19_4.COOLDOWN);
        this.registerWindowItems1_17_1(ClientboundPackets1_19_4.WINDOW_ITEMS);
        this.registerSetSlot1_17_1(ClientboundPackets1_19_4.SET_SLOT);
        this.registerAdvancements(ClientboundPackets1_19_4.ADVANCEMENTS);
        this.registerEntityEquipmentArray(ClientboundPackets1_19_4.ENTITY_EQUIPMENT);
        this.registerClickWindow1_17_1(ServerboundPackets1_19_3.CLICK_WINDOW);
        this.registerTradeList1_19(ClientboundPackets1_19_4.TRADE_LIST);
        this.registerCreativeInvAction(ServerboundPackets1_19_3.CREATIVE_INVENTORY_ACTION);
        this.registerWindowPropertyEnchantmentHandler(ClientboundPackets1_19_4.WINDOW_PROPERTY);
        this.registerSpawnParticle1_19(ClientboundPackets1_19_4.SPAWN_PARTICLE);
        RecipeRewriter1_19_3<ClientboundPackets1_19_4> recipeRewriter = new RecipeRewriter1_19_3<ClientboundPackets1_19_4>(this.protocol){

            @Override
            public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
                int ingredients = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                wrapper.passthrough(Type.STRING);
                wrapper.passthrough(Type.VAR_INT);
                for (int i = 0; i < ingredients; ++i) {
                    this.handleIngredient(wrapper);
                }
                this.rewrite(wrapper.passthrough(Type.ITEM1_13_2));
                wrapper.read(Type.BOOLEAN);
            }
        };
        ((Protocol1_19_3To1_19_4)this.protocol).registerClientbound(ClientboundPackets1_19_4.DECLARE_RECIPES, wrapper -> {
            int size;
            int newSize = size = wrapper.passthrough(Type.VAR_INT).intValue();
            for (int i = 0; i < size; ++i) {
                String type = wrapper.read(Type.STRING);
                String cutType = Key.stripMinecraftNamespace(type);
                if (cutType.equals("smithing_transform") || cutType.equals("smithing_trim")) {
                    --newSize;
                    wrapper.read(Type.STRING);
                    wrapper.read(Type.ITEM1_13_2_ARRAY);
                    wrapper.read(Type.ITEM1_13_2_ARRAY);
                    wrapper.read(Type.ITEM1_13_2_ARRAY);
                    if (!cutType.equals("smithing_transform")) continue;
                    wrapper.read(Type.ITEM1_13_2);
                    continue;
                }
                if (cutType.equals("crafting_decorated_pot")) {
                    --newSize;
                    wrapper.read(Type.STRING);
                    wrapper.read(Type.VAR_INT);
                    continue;
                }
                wrapper.write(Type.STRING, type);
                wrapper.passthrough(Type.STRING);
                recipeRewriter.handleRecipeType(wrapper, cutType);
            }
            wrapper.set(Type.VAR_INT, 0, newSize);
        });
    }
}

