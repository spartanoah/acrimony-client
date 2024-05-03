/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer;

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.GsonBuilder;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.internal.Streams;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtDeserializeException;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.ITypedSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.json.JsonTextSerializer_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.nbt.NbtTextSerializer_v1_20_3;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.io.StringReader;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TextComponentCodec {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final TextComponentCodec V1_20_3;
    public static final TextComponentCodec LATEST;
    private final Supplier<SNbtSerializer<CompoundTag>> sNbtSerializerSupplier;
    private final BiFunction<TextComponentCodec, SNbtSerializer<CompoundTag>, ITypedSerializer<JsonElement, ATextComponent>> jsonSerializerSupplier;
    private final BiFunction<TextComponentCodec, SNbtSerializer<CompoundTag>, ITypedSerializer<Tag, ATextComponent>> nbtSerializerSupplier;
    private SNbtSerializer<CompoundTag> sNbtSerializer;
    private ITypedSerializer<JsonElement, ATextComponent> jsonSerializer;
    private ITypedSerializer<Tag, ATextComponent> nbtSerializer;

    public TextComponentCodec(Supplier<SNbtSerializer<CompoundTag>> sNbtSerializerSupplier, BiFunction<TextComponentCodec, SNbtSerializer<CompoundTag>, ITypedSerializer<JsonElement, ATextComponent>> jsonSerializerSupplier, BiFunction<TextComponentCodec, SNbtSerializer<CompoundTag>, ITypedSerializer<Tag, ATextComponent>> nbtSerializerSupplier) {
        this.sNbtSerializerSupplier = sNbtSerializerSupplier;
        this.jsonSerializerSupplier = jsonSerializerSupplier;
        this.nbtSerializerSupplier = nbtSerializerSupplier;
    }

    private SNbtSerializer<CompoundTag> getSNbtSerializer() {
        if (this.sNbtSerializer == null) {
            this.sNbtSerializer = this.sNbtSerializerSupplier.get();
        }
        return this.sNbtSerializerSupplier.get();
    }

    public ITypedSerializer<JsonElement, ATextComponent> getJsonSerializer() {
        if (this.jsonSerializer == null) {
            this.jsonSerializer = this.jsonSerializerSupplier.apply(this, this.getSNbtSerializer());
        }
        return this.jsonSerializer;
    }

    public ITypedSerializer<Tag, ATextComponent> getNbtSerializer() {
        if (this.nbtSerializer == null) {
            this.nbtSerializer = this.nbtSerializerSupplier.apply(this, this.getSNbtSerializer());
        }
        return this.nbtSerializer;
    }

    public ATextComponent deserializeJson(String json) {
        return this.deserializeJsonTree(JsonParser.parseString(json));
    }

    public ATextComponent deserializeJsonReader(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(false);
        try {
            return this.deserialize(Streams.parse(reader));
        } catch (StackOverflowError e) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
        }
    }

    public ATextComponent deserializeLenientJson(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return this.deserializeJsonTree(JsonParser.parseReader(reader));
    }

    public ATextComponent deserializeNbt(String nbt) {
        try {
            return this.deserialize(this.getSNbtSerializer().getDeserializer().deserializeValue(nbt));
        } catch (SNbtDeserializeException e) {
            throw new RuntimeException("Failed to deserialize SNbt", e);
        }
    }

    public ATextComponent deserializeJsonTree(@Nullable JsonElement element) {
        if (element == null) {
            return null;
        }
        return this.deserialize(element);
    }

    public ATextComponent deserializeNbtTree(@Nullable Tag nbt) {
        if (nbt == null) {
            return null;
        }
        return this.deserialize(nbt);
    }

    public ATextComponent deserialize(JsonElement json) {
        return this.getJsonSerializer().deserialize(json);
    }

    public ATextComponent deserialize(Tag nbt) {
        return this.getNbtSerializer().deserialize(nbt);
    }

    public JsonElement serializeJsonTree(ATextComponent component) {
        return this.getJsonSerializer().serialize(component);
    }

    public Tag serializeNbt(ATextComponent component) {
        return this.getNbtSerializer().serialize(component);
    }

    public String serializeJsonString(ATextComponent component) {
        return GSON.toJson(this.serializeJsonTree(component));
    }

    public String serializeNbtString(ATextComponent component) {
        try {
            return this.getSNbtSerializer().serialize(this.serializeNbt(component));
        } catch (SNbtSerializeException e) {
            throw new RuntimeException("Failed to serialize SNbt", e);
        }
    }

    public TextComponentSerializer asSerializer() {
        return new TextComponentSerializer(this, () -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, (src, typeOfSrc, context) -> this.serializeJsonTree((ATextComponent)src)).registerTypeHierarchyAdapter(ATextComponent.class, (src, typeOfSrc, context) -> this.deserializeJsonTree(src)).disableHtmlEscaping().create());
    }

    static {
        LATEST = V1_20_3 = new TextComponentCodec(() -> SNbtSerializer.V1_14, JsonTextSerializer_v1_20_3::new, NbtTextSerializer_v1_20_3::new);
    }
}

