/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_19_1to1_19;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_0;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.libs.opennbt.stringified.SNBT;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ChatDecorationResult;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ServerboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.ChatTypeStorage;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.storage.NonceStorage;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_19_1To1_19
extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19_1, ServerboundPackets1_19, ServerboundPackets1_19_1> {
    private static final String CHAT_REGISTRY_SNBT = "{\n  \"minecraft:chat_type\": {\n    \"type\": \"minecraft:chat_type\",\n    \"value\": [\n         {\n            \"name\":\"minecraft:chat\",\n            \"id\":1,\n            \"element\":{\n               \"chat\":{\n                  \"translation_key\":\"chat.type.text\",\n                  \"parameters\":[\n                     \"sender\",\n                     \"content\"\n                  ]\n               },\n               \"narration\":{\n                  \"translation_key\":\"chat.type.text.narrate\",\n                  \"parameters\":[\n                     \"sender\",\n                     \"content\"\n                  ]\n               }\n            }\n         }    ]\n  }\n}";
    private static final CompoundTag CHAT_REGISTRY = (CompoundTag)SNBT.deserializeCompoundTag("{\n  \"minecraft:chat_type\": {\n    \"type\": \"minecraft:chat_type\",\n    \"value\": [\n         {\n            \"name\":\"minecraft:chat\",\n            \"id\":1,\n            \"element\":{\n               \"chat\":{\n                  \"translation_key\":\"chat.type.text\",\n                  \"parameters\":[\n                     \"sender\",\n                     \"content\"\n                  ]\n               },\n               \"narration\":{\n                  \"translation_key\":\"chat.type.text.narrate\",\n                  \"parameters\":[\n                     \"sender\",\n                     \"content\"\n                  ]\n               }\n            }\n         }    ]\n  }\n}").get("minecraft:chat_type");

    public Protocol1_19_1To1_19() {
        super(ClientboundPackets1_19.class, ClientboundPackets1_19_1.class, ServerboundPackets1_19.class, ServerboundPackets1_19_1.class);
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.COMPONENT);
                this.handler(wrapper -> {
                    int type = wrapper.read(Type.VAR_INT);
                    boolean overlay = type == 2;
                    wrapper.write(Type.BOOLEAN, overlay);
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_19.PLAYER_CHAT, ClientboundPackets1_19_1.SYSTEM_CHAT, (PacketHandler)new PacketHandlers(){

            @Override
            public void register() {
                this.handler(wrapper -> {
                    JsonElement signedContent = wrapper.read(Type.COMPONENT);
                    JsonElement unsignedContent = wrapper.read(Type.OPTIONAL_COMPONENT);
                    int chatTypeId = wrapper.read(Type.VAR_INT);
                    wrapper.read(Type.UUID);
                    JsonElement senderName = wrapper.read(Type.COMPONENT);
                    JsonElement teamName = wrapper.read(Type.OPTIONAL_COMPONENT);
                    CompoundTag chatType = wrapper.user().get(ChatTypeStorage.class).chatType(chatTypeId);
                    ChatDecorationResult decorationResult = Protocol1_19_1To1_19.decorateChatMessage(chatType, chatTypeId, senderName, teamName, unsignedContent != null ? unsignedContent : signedContent);
                    if (decorationResult == null) {
                        wrapper.cancel();
                        return;
                    }
                    wrapper.write(Type.COMPONENT, decorationResult.content());
                    wrapper.write(Type.BOOLEAN, decorationResult.overlay());
                });
                this.read(Type.LONG);
                this.read(Type.LONG);
                this.read(Type.BYTE_ARRAY_PRIMITIVE);
            }
        });
        this.registerServerbound(ServerboundPackets1_19_1.CHAT_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.map(Type.LONG);
                this.map(Type.LONG);
                this.map(Type.BYTE_ARRAY_PRIMITIVE);
                this.map(Type.BOOLEAN);
                this.handler(wrapper -> {
                    ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    if (chatSession != null) {
                        UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        String message = wrapper.get(Type.STRING, 0);
                        long timestamp = wrapper.get(Type.LONG, 0);
                        long salt = wrapper.get(Type.LONG, 1);
                        MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        DecoratableMessage decoratableMessage = new DecoratableMessage(message);
                        byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage);
                        wrapper.set(Type.BYTE_ARRAY_PRIMITIVE, 0, signature);
                        wrapper.set(Type.BOOLEAN, 0, decoratableMessage.isDecorated());
                    }
                });
                this.read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY);
                this.read(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE);
            }
        });
        this.registerServerbound(ServerboundPackets1_19_1.CHAT_COMMAND, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.map(Type.LONG);
                this.map(Type.LONG);
                this.handler(wrapper -> {
                    ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);
                    int signatures = wrapper.read(Type.VAR_INT);
                    for (int i = 0; i < signatures; ++i) {
                        wrapper.read(Type.STRING);
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                    }
                    if (chatSession != null && argumentsProvider != null) {
                        UUID sender = wrapper.user().getProtocolInfo().getUuid();
                        String message = wrapper.get(Type.STRING, 0);
                        long timestamp = wrapper.get(Type.LONG, 0);
                        long salt = wrapper.get(Type.LONG, 1);
                        List<Pair<String, String>> arguments = argumentsProvider.getSignableArguments(message);
                        wrapper.write(Type.VAR_INT, arguments.size());
                        for (Pair<String, String> argument : arguments) {
                            MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                            DecoratableMessage decoratableMessage = new DecoratableMessage(argument.value());
                            byte[] signature = chatSession.signChatMessage(metadata, decoratableMessage);
                            wrapper.write(Type.STRING, argument.key());
                            wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, signature);
                        }
                    } else {
                        wrapper.write(Type.VAR_INT, 0);
                    }
                });
                this.map(Type.BOOLEAN);
                this.read(Type.PLAYER_MESSAGE_SIGNATURE_ARRAY);
                this.read(Type.OPTIONAL_PLAYER_MESSAGE_SIGNATURE);
            }
        });
        this.cancelServerbound(ServerboundPackets1_19_1.CHAT_ACK);
        this.registerClientbound(ClientboundPackets1_19.JOIN_GAME, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.map(Type.BOOLEAN);
                this.map(Type.BYTE);
                this.map(Type.BYTE);
                this.map(Type.STRING_ARRAY);
                this.handler(wrapper -> {
                    ChatTypeStorage chatTypeStorage = wrapper.user().get(ChatTypeStorage.class);
                    chatTypeStorage.clear();
                    CompoundTag registry = wrapper.passthrough(Type.NAMED_COMPOUND_TAG);
                    ListTag chatTypes = (ListTag)registry.getCompoundTag("minecraft:chat_type").get("value");
                    for (Tag chatType : chatTypes) {
                        CompoundTag chatTypeCompound = (CompoundTag)chatType;
                        NumberTag idTag = (NumberTag)chatTypeCompound.get("id");
                        chatTypeStorage.addChatType(idTag.asInt(), chatTypeCompound);
                    }
                    registry.put("minecraft:chat_type", CHAT_REGISTRY.copy());
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_19.SERVER_DATA, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.OPTIONAL_COMPONENT);
                this.map(Type.OPTIONAL_STRING);
                this.map(Type.BOOLEAN);
                this.create(Type.BOOLEAN, false);
            }
        });
        this.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    ProfileKey profileKey = wrapper.read(Type.OPTIONAL_PROFILE_KEY);
                    ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
                    wrapper.write(Type.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey());
                    if (profileKey == null || chatSession != null) {
                        wrapper.user().put(new NonceStorage(null));
                    }
                });
                this.read(Type.OPTIONAL_UUID);
            }
        });
        this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    if (wrapper.user().has(NonceStorage.class)) {
                        return;
                    }
                    byte[] publicKey = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    byte[] nonce = wrapper.passthrough(Type.BYTE_ARRAY_PRIMITIVE);
                    wrapper.user().put(new NonceStorage(CipherUtil.encryptNonce(publicKey, nonce)));
                });
            }
        });
        this.registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.BYTE_ARRAY_PRIMITIVE);
                this.handler(wrapper -> {
                    NonceStorage nonceStorage = wrapper.user().remove(NonceStorage.class);
                    if (nonceStorage.nonce() == null) {
                        return;
                    }
                    boolean isNonce = wrapper.read(Type.BOOLEAN);
                    wrapper.write(Type.BOOLEAN, true);
                    if (!isNonce) {
                        wrapper.read(Type.LONG);
                        wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                        wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
                    }
                });
            }
        });
        this.registerClientbound(State.LOGIN, ClientboundLoginPackets.CUSTOM_QUERY.getId(), ClientboundLoginPackets.CUSTOM_QUERY.getId(), new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    String identifier = wrapper.get(Type.STRING, 0);
                    if (identifier.equals("velocity:player_info")) {
                        byte[] data = wrapper.passthrough(Type.REMAINING_BYTES);
                        if (data.length == 1 && data[0] > 1) {
                            data[0] = 1;
                        } else if (data.length == 0) {
                            data = new byte[]{1};
                            wrapper.set(Type.REMAINING_BYTES, 0, data);
                        } else {
                            Via.getPlatform().getLogger().warning("Received unexpected data in velocity:player_info (length=" + data.length + ")");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection connection) {
        connection.put(new ChatTypeStorage());
    }

    public static @Nullable ChatDecorationResult decorateChatMessage(CompoundTag chatType, int chatTypeId, JsonElement senderName, @Nullable JsonElement teamName, JsonElement message) {
        CompoundTag decoration;
        if (chatType == null) {
            Via.getPlatform().getLogger().warning("Chat message has unknown chat type id " + chatTypeId + ". Message: " + message);
            return null;
        }
        CompoundTag chatData = chatType.getCompoundTag("element").getCompoundTag("chat");
        boolean overlay = false;
        if (chatData == null) {
            chatData = chatType.getCompoundTag("element").getCompoundTag("overlay");
            if (chatData == null) {
                return null;
            }
            overlay = true;
        }
        if ((decoration = chatData.getCompoundTag("decoration")) == null) {
            return new ChatDecorationResult(message, overlay);
        }
        return new ChatDecorationResult(Protocol1_19_1To1_19.translatabaleComponentFromTag(decoration, senderName, teamName, message), overlay);
    }

    public static JsonElement translatabaleComponentFromTag(CompoundTag tag, JsonElement senderName, @Nullable JsonElement targetName, JsonElement message) {
        String translationKey = tag.getStringTag("translation_key").getValue();
        Style style = new Style();
        CompoundTag styleTag = tag.getCompoundTag("style");
        if (styleTag != null) {
            Object textColor;
            StringTag color = styleTag.getStringTag("color");
            if (color != null && (textColor = TextFormatting.getByName(color.getValue())) != null) {
                style.setFormatting((TextFormatting)textColor);
            }
            for (Map.Entry entry : TextFormatting.FORMATTINGS.entrySet()) {
                NumberTag formattingTag = styleTag.getNumberTag((String)entry.getKey());
                if (!(formattingTag instanceof ByteTag)) continue;
                boolean value = formattingTag.asBoolean();
                TextFormatting formatting = (TextFormatting)entry.getValue();
                if (formatting == TextFormatting.OBFUSCATED) {
                    style.setObfuscated(value);
                    continue;
                }
                if (formatting == TextFormatting.BOLD) {
                    style.setBold(value);
                    continue;
                }
                if (formatting == TextFormatting.STRIKETHROUGH) {
                    style.setStrikethrough(value);
                    continue;
                }
                if (formatting == TextFormatting.UNDERLINE) {
                    style.setUnderlined(value);
                    continue;
                }
                if (formatting != TextFormatting.ITALIC) continue;
                style.setItalic(value);
            }
        }
        ListTag parameters = tag.getListTag("parameters");
        ArrayList<ATextComponent> arguments = new ArrayList<ATextComponent>();
        if (parameters != null) {
            for (Tag element : parameters) {
                JsonElement argument = null;
                switch ((String)element.getValue()) {
                    case "sender": {
                        argument = senderName;
                        break;
                    }
                    case "content": {
                        argument = message;
                        break;
                    }
                    case "team_name": 
                    case "target": {
                        Preconditions.checkNotNull(targetName, "Team name is null");
                        argument = targetName;
                        break;
                    }
                    default: {
                        Via.getPlatform().getLogger().warning("Unknown parameter for chat decoration: " + element.getValue());
                    }
                }
                if (argument == null) continue;
                arguments.add(TextComponentSerializer.V1_18.deserialize(argument));
            }
        }
        return TextComponentSerializer.V1_18.serializeJson(new TranslationComponent(translationKey, arguments));
    }
}

