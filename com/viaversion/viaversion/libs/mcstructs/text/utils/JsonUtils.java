/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JsonUtils {
    public static boolean getBoolean(JsonElement element, String key) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        throw new JsonSyntaxException("Expected " + key + " to be a boolean, was " + element);
    }

    public static boolean getBoolean(JsonObject object, String key) {
        if (object.has(key)) {
            return JsonUtils.getBoolean(object.get(key), key);
        }
        throw new JsonSyntaxException("Missing " + key + ", expected to find a boolean");
    }

    public static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        if (object.has(key)) {
            return JsonUtils.getBoolean(object, key);
        }
        return fallback;
    }

    public static int getInt(JsonElement element, String key) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        throw new JsonSyntaxException("Expected " + key + " to be a boolean, was " + element);
    }

    public static int getInt(JsonObject object, String key) {
        if (object.has(key)) {
            return JsonUtils.getInt(object.get(key), key);
        }
        throw new JsonSyntaxException("Missing " + key + ", expected to find a boolean");
    }

    public static int getInt(JsonObject object, String key, int fallback) {
        if (object.has(key)) {
            return JsonUtils.getInt(object, key);
        }
        return fallback;
    }

    public static String getString(JsonElement element, String key) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        throw new JsonSyntaxException("Expected " + key + " to be a string, was " + element);
    }

    public static String getString(JsonObject object, String key) {
        if (object.has(key)) {
            return JsonUtils.getString(object.get(key), key);
        }
        throw new JsonSyntaxException("Missing " + key + ", expected to find a string");
    }

    public static String getString(JsonObject object, String key, String fallback) {
        if (object.has(key)) {
            return JsonUtils.getString(object, key);
        }
        return fallback;
    }

    public static JsonObject getJsonObject(JsonElement element, String key) {
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        throw new JsonSyntaxException("Expected " + key + " to be a JsonObject, was " + element);
    }

    public static JsonObject getJsonObject(JsonObject object, String key) {
        if (object.has(key)) {
            return JsonUtils.getJsonObject(object.get(key), key);
        }
        throw new JsonSyntaxException("Missing " + key + ", expected to find a JsonObject");
    }

    public static String toSortedString(@Nullable JsonElement element, @Nullable Comparator<String> comparator) {
        if (element == null) {
            return null;
        }
        if (comparator != null) {
            return JsonUtils.sort(element, comparator).toString();
        }
        return JsonUtils.sort(element, Comparator.naturalOrder()).toString();
    }

    public static JsonElement sort(@Nullable JsonElement element, @Nonnull Comparator<String> comparator) {
        if (element == null) {
            return null;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); ++i) {
                array.set(i, JsonUtils.sort(array.get(i), comparator));
            }
            return array;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonObject sorted = new JsonObject();
            ArrayList<String> keys = new ArrayList<String>(object.keySet());
            keys.sort(comparator);
            for (String key : keys) {
                sorted.add(key, JsonUtils.sort(object.get(key), comparator));
            }
            return sorted;
        }
        return element;
    }
}

