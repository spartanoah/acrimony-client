/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.util;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.TextHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.LegacyStringDeserializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentUtil {
    public static JsonObject emptyJsonComponent() {
        return ComponentUtil.plainToJson("");
    }

    public static String emptyJsonComponentString() {
        return "{\"text\":\"\"}";
    }

    public static JsonObject plainToJson(String message) {
        JsonObject object = new JsonObject();
        object.addProperty("text", message);
        return object;
    }

    public static @Nullable JsonElement tagToJson(@Nullable Tag tag) {
        try {
            ATextComponent component = TextComponentCodec.V1_20_3.deserializeNbtTree(tag);
            return component != null ? SerializerVersion.V1_19_4.toJson(component) : null;
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + tag, e);
            return ComponentUtil.plainToJson("<error>");
        }
    }

    public static @Nullable Tag jsonToTag(@Nullable JsonElement element) {
        if (element == null) {
            return null;
        }
        try {
            ATextComponent component = TextComponentSerializer.V1_19_4.deserialize(element);
            return TextComponentCodec.V1_20_3.serializeNbt(component);
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + element, e);
            return new StringTag("<error>");
        }
    }

    public static @Nullable JsonElement convertJson(@Nullable JsonElement element, SerializerVersion from, SerializerVersion to) {
        return element != null ? ComponentUtil.convert(from, to, from.jsonSerializer.deserialize(element)) : null;
    }

    public static @Nullable JsonElement convertJson(@Nullable String json, SerializerVersion from, SerializerVersion to) {
        return json != null ? ComponentUtil.convert(from, to, from.jsonSerializer.deserializeReader(json)) : null;
    }

    private static JsonElement convert(SerializerVersion from, SerializerVersion to, ATextComponent component) {
        Style style;
        AHoverEvent hoverEvent;
        if (from.ordinal() >= SerializerVersion.V1_16.ordinal() && to.ordinal() < SerializerVersion.V1_16.ordinal() && (hoverEvent = (style = component.getStyle()).getHoverEvent()) != null && !(hoverEvent instanceof TextHoverEvent)) {
            style.setHoverEvent(hoverEvent.toLegacy(to.jsonSerializer, to.snbtSerializer));
        }
        return to.toJson(component);
    }

    public static JsonElement legacyToJson(String message) {
        return SerializerVersion.V1_12.toJson(LegacyStringDeserializer.parse(message, true));
    }

    public static String legacyToJsonString(String message) {
        return ComponentUtil.legacyToJsonString(message, false);
    }

    public static String legacyToJsonString(String message, boolean itemData) {
        ATextComponent component = LegacyStringDeserializer.parse(message, true);
        if (itemData) {
            component.setParentStyle(new Style().setItalic(false));
        }
        return TextComponentSerializer.V1_12.serialize(component);
    }

    public static String jsonToLegacy(String value) {
        return TextComponentSerializer.V1_12.deserializeReader(value).asLegacyFormatString();
    }

    public static String jsonToLegacy(JsonElement value) {
        return TextComponentSerializer.V1_12.deserialize(value).asLegacyFormatString();
    }

    public static enum SerializerVersion {
        V1_8(TextComponentSerializer.V1_8, SNbtSerializer.V1_8),
        V1_9(TextComponentSerializer.V1_9, SNbtSerializer.V1_8),
        V1_12(TextComponentSerializer.V1_12, SNbtSerializer.V1_12),
        V1_14(TextComponentSerializer.V1_14, SNbtSerializer.V1_14),
        V1_15(TextComponentSerializer.V1_15, SNbtSerializer.V1_14),
        V1_16(TextComponentSerializer.V1_16, SNbtSerializer.V1_14),
        V1_17(TextComponentSerializer.V1_17, SNbtSerializer.V1_14),
        V1_18(TextComponentSerializer.V1_18, SNbtSerializer.V1_14),
        V1_19_4(TextComponentSerializer.V1_19_4, SNbtSerializer.V1_14),
        V1_20_3(TextComponentCodec.V1_20_3, SNbtSerializer.V1_14);

        private final TextComponentSerializer jsonSerializer;
        private final SNbtSerializer<?> snbtSerializer;
        private final TextComponentCodec codec;

        private SerializerVersion(TextComponentSerializer jsonSerializer, SNbtSerializer<?> snbtSerializer) {
            this.jsonSerializer = jsonSerializer;
            this.snbtSerializer = snbtSerializer;
            this.codec = null;
        }

        private SerializerVersion(TextComponentCodec codec, SNbtSerializer<?> snbtSerializer) {
            this.codec = codec;
            this.jsonSerializer = codec.asSerializer();
            this.snbtSerializer = snbtSerializer;
        }

        public JsonElement toJson(ATextComponent component) {
            return this.jsonSerializer.serializeJson(component);
        }
    }
}

