/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.snbt.exceptions;

public class SNbtDeserializeException
extends Exception {
    private static String trim(String rawTag, int position) {
        StringBuilder out = new StringBuilder();
        int end = Math.min(rawTag.length(), position);
        if (end > 35) {
            out.append("...");
        }
        out.append(rawTag, Math.max(0, end - 35), end).append("<--[HERE]");
        return out.toString();
    }

    public SNbtDeserializeException(String message) {
        super(message);
    }

    public SNbtDeserializeException(String message, String rawTag, int position) {
        super(message + " at: " + SNbtDeserializeException.trim(rawTag, position));
    }
}

