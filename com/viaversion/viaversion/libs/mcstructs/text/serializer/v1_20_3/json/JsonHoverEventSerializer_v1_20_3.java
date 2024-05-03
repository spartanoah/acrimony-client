/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.json;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.EntityHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.ItemHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.TextHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.ITypedSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;
import com.viaversion.viaversion.libs.mcstructs.text.utils.CodecUtils;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import java.util.UUID;

public class JsonHoverEventSerializer_v1_20_3
implements ITypedSerializer<JsonElement, AHoverEvent> {
    private static final String ACTION = "action";
    private static final String CONTENTS = "contents";
    private static final String VALUE = "value";
    private final TextComponentCodec codec;
    private final ITypedSerializer<JsonElement, ATextComponent> textSerializer;
    private final SNbtSerializer<CompoundTag> sNbtSerializer;

    public JsonHoverEventSerializer_v1_20_3(TextComponentCodec codec, ITypedSerializer<JsonElement, ATextComponent> textSerializer, SNbtSerializer<CompoundTag> sNbtSerializer) {
        this.codec = codec;
        this.textSerializer = textSerializer;
        this.sNbtSerializer = sNbtSerializer;
    }

    @Override
    public JsonElement serialize(AHoverEvent object) {
        JsonObject out = new JsonObject();
        out.addProperty(ACTION, object.getAction().getName());
        if (object instanceof TextHoverEvent) {
            TextHoverEvent textHoverEvent = (TextHoverEvent)object;
            out.add(CONTENTS, this.textSerializer.serialize(textHoverEvent.getText()));
        } else if (object instanceof ItemHoverEvent) {
            ItemHoverEvent itemHoverEvent = (ItemHoverEvent)object;
            JsonObject contents = new JsonObject();
            contents.addProperty("id", itemHoverEvent.getItem().get());
            if (itemHoverEvent.getCount() != 1) {
                contents.addProperty("count", itemHoverEvent.getCount());
            }
            if (itemHoverEvent.getNbt() != null) {
                try {
                    contents.addProperty("tag", this.sNbtSerializer.serialize(itemHoverEvent.getNbt()));
                } catch (SNbtSerializeException e) {
                    throw new IllegalStateException("Failed to serialize nbt", e);
                }
            }
            out.add(CONTENTS, contents);
        } else if (object instanceof EntityHoverEvent) {
            EntityHoverEvent entityHoverEvent = (EntityHoverEvent)object;
            JsonObject contents = new JsonObject();
            contents.addProperty("type", entityHoverEvent.getEntityType().get());
            JsonArray id = new JsonArray();
            id.add((int)(entityHoverEvent.getUuid().getMostSignificantBits() >> 32));
            id.add((int)(entityHoverEvent.getUuid().getMostSignificantBits() & 0xFFFFFFFFL));
            id.add((int)(entityHoverEvent.getUuid().getLeastSignificantBits() >> 32));
            id.add((int)(entityHoverEvent.getUuid().getLeastSignificantBits() & 0xFFFFFFFFL));
            contents.add("id", id);
            if (entityHoverEvent.getName() != null) {
                contents.add("name", this.textSerializer.serialize(entityHoverEvent.getName()));
            }
            out.add(CONTENTS, contents);
        } else {
            throw new IllegalArgumentException("Unknown hover event type: " + object.getClass().getName());
        }
        return out;
    }

    @Override
    public AHoverEvent deserialize(JsonElement object) {
        if (!object.isJsonObject()) {
            throw new IllegalArgumentException("Element must be a json object");
        }
        JsonObject obj = object.getAsJsonObject();
        HoverEventAction action = HoverEventAction.getByName(CodecUtils.requiredString(obj, ACTION), false);
        if (action == null) {
            throw new IllegalArgumentException("Unknown hover event action: " + obj.get(ACTION).getAsString());
        }
        if (!action.isUserDefinable()) {
            throw new IllegalArgumentException("Hover event action is not user definable: " + (Object)((Object)action));
        }
        if (obj.has(CONTENTS)) {
            switch (action) {
                case SHOW_TEXT: {
                    return new TextHoverEvent(action, this.textSerializer.deserialize(obj.get(CONTENTS)));
                }
                case SHOW_ITEM: {
                    JsonObject contents;
                    if (obj.has(CONTENTS) && CodecUtils.isString(obj.get(CONTENTS))) {
                        return new ItemHoverEvent(action, Identifier.of(obj.get(CONTENTS).getAsString()), 1, null);
                    }
                    if (obj.has(CONTENTS) && CodecUtils.isObject(obj.get(CONTENTS))) {
                        contents = obj.getAsJsonObject(CONTENTS);
                        String id = CodecUtils.requiredString(contents, "id");
                        Integer count = CodecUtils.optionalInt(contents, "count");
                        String itemTag = CodecUtils.optionalString(contents, "tag");
                        try {
                            return new ItemHoverEvent(action, Identifier.of(id), count == null ? 1 : count, itemTag == null ? null : this.sNbtSerializer.deserialize(itemTag));
                        } catch (Throwable t) {
                            this.sneak(t);
                        }
                    } else {
                        throw new IllegalArgumentException("Expected string or json array for 'contents' tag");
                    }
                }
                case SHOW_ENTITY: {
                    JsonObject contents = CodecUtils.requiredObject(obj, CONTENTS);
                    Identifier type = Identifier.of(CodecUtils.requiredString(contents, "type"));
                    UUID id = this.getUUID(contents.get("id"));
                    ATextComponent name = contents.has("name") ? this.textSerializer.deserialize(contents.get("name")) : null;
                    return new EntityHoverEvent(action, type, id, name);
                }
            }
            throw new IllegalArgumentException("Unknown hover event action: " + (Object)((Object)action));
        }
        if (obj.has(VALUE)) {
            ATextComponent value = this.textSerializer.deserialize(obj.get(VALUE));
            try {
                switch (action) {
                    case SHOW_TEXT: {
                        return new TextHoverEvent(action, value);
                    }
                    case SHOW_ITEM: {
                        CompoundTag parsed = this.sNbtSerializer.deserialize(value.asUnformattedString());
                        Identifier id = Identifier.of(parsed.get("id") instanceof StringTag ? ((StringTag)parsed.get("id")).getValue() : "");
                        byte count = parsed.get("Count") instanceof ByteTag ? ((ByteTag)parsed.get("Count")).asByte() : (byte)0;
                        CompoundTag itemTag = parsed.get("tag") instanceof CompoundTag ? (CompoundTag)parsed.get("tag") : null;
                        return new ItemHoverEvent(action, id, count, itemTag);
                    }
                    case SHOW_ENTITY: {
                        CompoundTag parsed = this.sNbtSerializer.deserialize(value.asUnformattedString());
                        ATextComponent name = this.codec.deserializeJson(parsed.get("name") instanceof StringTag ? ((StringTag)parsed.get("name")).getValue() : "");
                        Identifier type = Identifier.of(parsed.get("type") instanceof StringTag ? ((StringTag)parsed.get("type")).getValue() : "");
                        UUID uuid = UUID.fromString(parsed.get("id") instanceof StringTag ? ((StringTag)parsed.get("id")).getValue() : "");
                        return new EntityHoverEvent(action, type, uuid, name);
                    }
                }
                throw new IllegalArgumentException("Unknown hover event action: " + (Object)((Object)action));
            } catch (Throwable t) {
                this.sneak(t);
            }
        }
        throw new IllegalArgumentException("Missing 'contents' or 'value' tag");
    }

    private <T extends Throwable> void sneak(Throwable t) throws T {
        throw t;
    }

    private UUID getUUID(JsonElement element) {
        if (!(element != null && (element.isJsonArray() || element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()))) {
            throw new IllegalArgumentException("Expected json array or string for 'id' tag");
        }
        if (element.isJsonPrimitive()) {
            return UUID.fromString(element.getAsString());
        }
        JsonArray array = element.getAsJsonArray();
        if (array.size() != 4) {
            throw new IllegalArgumentException("Expected json array with 4 elements for 'id' tag");
        }
        int[] ints = new int[4];
        for (int i = 0; i < ints.length; ++i) {
            JsonElement e = array.get(i);
            if (!e.isJsonPrimitive()) {
                throw new IllegalArgumentException("Expected json primitive for array element " + i + " of 'id' tag");
            }
            JsonPrimitive primitive = e.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                ints[i] = primitive.getAsInt();
                continue;
            }
            if (primitive.isBoolean()) {
                ints[i] = primitive.getAsBoolean() ? 1 : 0;
                continue;
            }
            throw new IllegalArgumentException("Expected int for array element " + i + " of 'id' tag");
        }
        return new UUID((long)ints[0] << 32 | (long)ints[1] & 0xFFFFFFFFL, (long)ints[2] << 32 | (long)ints[3] & 0xFFFFFFFFL);
    }
}

