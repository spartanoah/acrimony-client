/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum LongSerializationPolicy {
    DEFAULT{

        public JsonElement serialize(Long value) {
            return new JsonPrimitive(value);
        }
    }
    ,
    STRING{

        public JsonElement serialize(Long value) {
            return new JsonPrimitive(String.valueOf(value));
        }
    };


    public abstract JsonElement serialize(Long var1);
}

