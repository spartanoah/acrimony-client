/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.core;

import java.util.Objects;

public class Identifier {
    public static final String VALID_KEY_CHARS = "[_\\-a-z0-9.]*";
    public static final String VALID_VALUE_CHARS = "[_\\-a-z0-9/.]*";
    private final String key;
    private final String value;

    public static Identifier of(String value) {
        int splitIndex = value.indexOf(58);
        String key = splitIndex <= 0 ? "minecraft" : value.substring(0, splitIndex);
        String val2 = splitIndex == -1 ? value : value.substring(splitIndex + 1);
        return Identifier.of(key, val2);
    }

    public static Identifier tryOf(String value) {
        try {
            return Identifier.of(value);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Identifier of(String key, String value) {
        return new Identifier(key, value);
    }

    private Identifier(String key, String value) {
        if (!key.matches(VALID_KEY_CHARS)) {
            throw new IllegalArgumentException("Key contains illegal chars");
        }
        if (!value.matches(VALID_VALUE_CHARS)) {
            throw new IllegalArgumentException("Value contains illegal chars");
        }
        this.key = key;
        this.value = value;
    }

    public String get() {
        return this.key + ":" + this.value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Identifier that = (Identifier)o;
        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    public int hashCode() {
        return Objects.hash(this.key, this.value);
    }

    public String toString() {
        return "Identifier{key='" + this.key + '\'' + ", value='" + this.value + '\'' + '}';
    }
}

