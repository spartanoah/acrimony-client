/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer;

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.GsonBuilder;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_12.TextDeserializer_v1_12;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_12.TextSerializer_v1_12;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_14.TextDeserializer_v1_14;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_14.TextSerializer_v1_14;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_15.TextDeserializer_v1_15;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_15.TextSerializer_v1_15;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.HoverEventDeserializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.HoverEventSerializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.StyleDeserializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.StyleSerializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.TextDeserializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16.TextSerializer_v1_16;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_17.TextDeserializer_v1_17;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_17.TextSerializer_v1_17;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_18.HoverEventDeserializer_v1_18;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_19_4.TextDeserializer_v1_19_4;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_19_4.TextSerializer_v1_19_4;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_6.TextDeserializer_v1_6;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_6.TextSerializer_v1_6;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_7.StyleDeserializer_v1_7;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_7.StyleSerializer_v1_7;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_7.TextDeserializer_v1_7;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_7.TextSerializer_v1_7;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_8.StyleDeserializer_v1_8;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_8.StyleSerializer_v1_8;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_8.TextDeserializer_v1_8;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_8.TextSerializer_v1_8;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_9.TextSerializer_v1_9;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class TextComponentSerializer {
    public static final TextComponentSerializer V1_6 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_6()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_6()).create());
    public static final TextComponentSerializer V1_7 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_7()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_7()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_7()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_7(V1_7, SNbtSerializer.V1_7)).create());
    public static final TextComponentSerializer V1_8 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_8()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_8(V1_8, SNbtSerializer.V1_8)).create());
    public static final TextComponentSerializer V1_9 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_9()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_8(V1_9, SNbtSerializer.V1_8)).create());
    public static final TextComponentSerializer V1_12 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_12()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_12()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_8(V1_12, SNbtSerializer.V1_12)).create());
    public static final TextComponentSerializer V1_14 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_14()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_14()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_8(V1_14, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_15 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_15()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_15()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_8()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_8(V1_15, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_16 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_16()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_16()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_16()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_16()).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventDeserializer_v1_16(V1_16, SNbtSerializer.V1_14)).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventSerializer_v1_16(V1_16, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_17 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_17()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_17()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_16()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_16()).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventDeserializer_v1_16(V1_17, SNbtSerializer.V1_14)).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventSerializer_v1_16(V1_17, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_18 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_17()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_17()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_16()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_16()).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventDeserializer_v1_18(V1_18, SNbtSerializer.V1_14)).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventSerializer_v1_16(V1_18, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_19_4 = new TextComponentSerializer(() -> new GsonBuilder().registerTypeHierarchyAdapter(ATextComponent.class, new TextSerializer_v1_19_4()).registerTypeHierarchyAdapter(ATextComponent.class, new TextDeserializer_v1_19_4()).registerTypeAdapter((Type)((Object)Style.class), new StyleDeserializer_v1_16()).registerTypeAdapter((Type)((Object)Style.class), new StyleSerializer_v1_16()).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventDeserializer_v1_18(V1_19_4, SNbtSerializer.V1_14)).registerTypeHierarchyAdapter(AHoverEvent.class, new HoverEventSerializer_v1_16(V1_19_4, SNbtSerializer.V1_14)).disableHtmlEscaping().create());
    public static final TextComponentSerializer V1_20_3;
    public static final TextComponentSerializer LATEST;
    private final TextComponentCodec parentCodec;
    private final Supplier<Gson> gsonSupplier;
    private Gson gson;

    public TextComponentSerializer(Supplier<Gson> gsonSupplier) {
        this(null, gsonSupplier);
    }

    public TextComponentSerializer(TextComponentCodec parentCodec, Supplier<Gson> gsonSupplier) {
        this.parentCodec = parentCodec;
        this.gsonSupplier = gsonSupplier;
    }

    @Nullable
    public TextComponentCodec getParentCodec() {
        return this.parentCodec;
    }

    public boolean isCodec() {
        return this.parentCodec != null;
    }

    public Gson getGson() {
        if (this.gson == null) {
            this.gson = this.gsonSupplier.get();
        }
        return this.gson;
    }

    public String serialize(ATextComponent component) {
        return this.getGson().toJson(component);
    }

    public JsonElement serializeJson(ATextComponent component) {
        return this.getGson().toJsonTree(component);
    }

    public ATextComponent deserialize(String json) {
        return this.getGson().fromJson(json, ATextComponent.class);
    }

    public ATextComponent deserialize(JsonElement element) {
        return this.getGson().fromJson(element, ATextComponent.class);
    }

    public ATextComponent deserializeReader(String json) {
        return this.deserializeReader(json, false);
    }

    public ATextComponent deserializeParser(String json) {
        if (this.parentCodec != null) {
            return this.parentCodec.deserializeJson(json);
        }
        return this.getGson().fromJson(JsonParser.parseString(json), ATextComponent.class);
    }

    public ATextComponent deserializeLenientReader(String json) {
        if (this.parentCodec != null) {
            return this.parentCodec.deserializeLenientJson(json);
        }
        return this.deserializeReader(json, true);
    }

    public ATextComponent deserializeReader(String json, boolean lenient) {
        if (this.parentCodec != null) {
            if (lenient) {
                return this.parentCodec.deserializeLenientJson(json);
            }
            return this.parentCodec.deserializeJsonReader(json);
        }
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(lenient);
            return this.getGson().getAdapter(ATextComponent.class).read(reader);
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse json", e);
        }
    }

    static {
        LATEST = V1_20_3 = TextComponentCodec.V1_20_3.asSerializer();
    }
}

