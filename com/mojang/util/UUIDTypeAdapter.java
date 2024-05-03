/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;

public class UUIDTypeAdapter
extends TypeAdapter<UUID> {
    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        out.value(UUIDTypeAdapter.fromUUID(value));
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        return UUIDTypeAdapter.fromString(in.nextString());
    }

    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }

    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}

