/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum JsonFormatTypes {
    STRING,
    NUMBER,
    INTEGER,
    BOOLEAN,
    OBJECT,
    ARRAY,
    NULL,
    ANY;

    private static final Map<String, JsonFormatTypes> _byLCName;

    @JsonValue
    public String value() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static JsonFormatTypes forValue(String s) {
        return _byLCName.get(s);
    }

    static {
        _byLCName = new HashMap<String, JsonFormatTypes>();
        for (JsonFormatTypes t : JsonFormatTypes.values()) {
            _byLCName.put(t.name().toLowerCase(), t);
        }
    }
}

