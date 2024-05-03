/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CodecUtils {
    public static boolean isString(@Nullable JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    public static boolean isNumber(@Nullable JsonElement element) {
        return element != null && element.isJsonPrimitive() && (element.getAsJsonPrimitive().isNumber() || element.getAsJsonPrimitive().isBoolean());
    }

    public static boolean isObject(@Nullable JsonElement element) {
        return element != null && element.isJsonObject();
    }

    public static boolean containsString(JsonObject obj, String name) {
        return obj.has(name) && CodecUtils.isString(obj.get(name));
    }

    public static boolean containsArray(JsonObject obj, String name) {
        return obj.has(name) && obj.get(name).isJsonArray();
    }

    public static boolean containsObject(JsonObject obj, String name) {
        return obj.has(name) && CodecUtils.isObject(obj.get(name));
    }

    @Nullable
    public static Boolean optionalBoolean(CompoundTag tag, String name) {
        if (!tag.contains(name)) {
            return null;
        }
        return CodecUtils.requiredBoolean(tag, name);
    }

    @Nullable
    public static Boolean optionalBoolean(JsonObject obj, String name) {
        if (!obj.has(name)) {
            return null;
        }
        return CodecUtils.requiredBoolean(obj, name);
    }

    @Nullable
    public static Integer optionalInt(CompoundTag tag, String name) {
        if (!tag.contains(name)) {
            return null;
        }
        return CodecUtils.requiredInt(tag, name);
    }

    @Nullable
    public static Integer optionalInt(JsonObject obj, String name) {
        if (!obj.has(name)) {
            return null;
        }
        return CodecUtils.requiredInt(obj, name);
    }

    @Nullable
    public static String optionalString(CompoundTag tag, String name) {
        if (!tag.contains(name)) {
            return null;
        }
        return CodecUtils.requiredString(tag, name);
    }

    @Nullable
    public static String optionalString(JsonObject obj, String name) {
        if (!obj.has(name)) {
            return null;
        }
        return CodecUtils.requiredString(obj, name);
    }

    public static boolean requiredBoolean(CompoundTag tag, String name) {
        if (!(tag.get(name) instanceof ByteTag)) {
            throw new IllegalArgumentException("Expected byte tag for '" + name + "' tag");
        }
        return tag.get(name) instanceof ByteTag ? ((ByteTag)tag.get(name)).asBoolean() : false;
    }

    public static boolean requiredBoolean(JsonObject obj, String name) {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missing boolean for '" + name + "' tag");
        }
        JsonElement element = obj.get(name);
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected boolean for '" + name + "' tag");
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsInt() != 0;
        }
        throw new IllegalArgumentException("Expected boolean for '" + name + "' tag");
    }

    public static int requiredInt(CompoundTag tag, String name) {
        if (!(tag.get(name) instanceof IntTag)) {
            throw new IllegalArgumentException("Expected int tag for '" + name + "' tag");
        }
        return tag.get(name) instanceof IntTag ? ((IntTag)tag.get(name)).asInt() : 0;
    }

    public static int requiredInt(JsonObject obj, String name) {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missing int for '" + name + "' tag");
        }
        JsonElement element = obj.get(name);
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected int for '" + name + "' tag");
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsInt();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? 1 : 0;
        }
        throw new IllegalArgumentException("Expected int for '" + name + "' tag");
    }

    public static String requiredString(CompoundTag tag, String name) {
        if (!(tag.get(name) instanceof StringTag)) {
            throw new IllegalArgumentException("Expected string tag for '" + name + "' tag");
        }
        return tag.get(name) instanceof StringTag ? ((StringTag)tag.get(name)).getValue() : "";
    }

    public static String requiredString(JsonObject obj, String name) {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missing string for '" + name + "' tag");
        }
        JsonElement element = obj.get(name);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected string for '" + name + "' tag");
        }
        return element.getAsString();
    }

    public static CompoundTag requiredCompound(CompoundTag tag, String name) {
        if (!(tag.get(name) instanceof CompoundTag)) {
            throw new IllegalArgumentException("Expected compound tag for '" + name + "' tag");
        }
        return tag.get(name) instanceof CompoundTag ? (CompoundTag)tag.get(name) : new CompoundTag();
    }

    public static JsonObject requiredObject(JsonObject obj, String name) {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missing object for '" + name + "' tag");
        }
        JsonElement element = obj.get(name);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Expected object for '" + name + "' tag");
        }
        return element.getAsJsonObject();
    }
}

