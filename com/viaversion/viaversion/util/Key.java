/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.util;

public final class Key {
    public static String stripNamespace(String identifier) {
        int index = identifier.indexOf(58);
        if (index == -1) {
            return identifier;
        }
        return identifier.substring(index + 1);
    }

    public static String stripMinecraftNamespace(String identifier) {
        if (identifier.startsWith("minecraft:")) {
            return identifier.substring(10);
        }
        if (identifier.startsWith(":")) {
            return identifier.substring(1);
        }
        return identifier;
    }

    public static String namespaced(String identifier) {
        int index = identifier.indexOf(58);
        if (index == -1) {
            return "minecraft:" + identifier;
        }
        if (index == 0) {
            return "minecraft" + identifier;
        }
        return identifier;
    }

    public static boolean isValid(String identifier) {
        return identifier.matches("([0-9a-z_.-]*:)?[0-9a-z_/.-]*");
    }
}

