/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

import net.minecraft.util.ChatComponentTranslation;

public class ChatComponentTranslationFormatException
extends IllegalArgumentException {
    public ChatComponentTranslationFormatException(ChatComponentTranslation component, String message) {
        super(String.format("Error parsing: %s: %s", component, message));
    }

    public ChatComponentTranslationFormatException(ChatComponentTranslation component, int index) {
        super(String.format("Invalid index %d requested for %s", index, component));
    }

    public ChatComponentTranslationFormatException(ChatComponentTranslation component, Throwable cause) {
        super(String.format("Error while parsing: %s", component), cause);
    }
}

