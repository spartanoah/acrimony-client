/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface JsonSerializer<T> {
    public JsonElement serialize(T var1, Type var2, JsonSerializationContext var3);
}

