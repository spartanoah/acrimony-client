/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.chat;

import com.viaversion.viabackwards.api.rewriters.TranslatableRewriter;
import com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;

public class TranslatableRewriter1_16
extends TranslatableRewriter<ClientboundPackets1_16> {
    private static final ChatColor[] COLORS = new ChatColor[]{new ChatColor("black", 0), new ChatColor("dark_blue", 170), new ChatColor("dark_green", 43520), new ChatColor("dark_aqua", 43690), new ChatColor("dark_red", 0xAA0000), new ChatColor("dark_purple", 0xAA00AA), new ChatColor("gold", 0xFFAA00), new ChatColor("gray", 0xAAAAAA), new ChatColor("dark_gray", 0x555555), new ChatColor("blue", 0x5555FF), new ChatColor("green", 0x55FF55), new ChatColor("aqua", 0x55FFFF), new ChatColor("red", 0xFF5555), new ChatColor("light_purple", 0xFF55FF), new ChatColor("yellow", 0xFFFF55), new ChatColor("white", 0xFFFFFF)};

    public TranslatableRewriter1_16(Protocol1_15_2To1_16 protocol) {
        super(protocol, ComponentRewriter.ReadType.JSON);
    }

    @Override
    public void processText(JsonElement value) {
        JsonObject hoverEvent;
        String colorName;
        super.processText(value);
        if (value == null || !value.isJsonObject()) {
            return;
        }
        JsonObject object = value.getAsJsonObject();
        JsonPrimitive color = object.getAsJsonPrimitive("color");
        if (color != null && !(colorName = color.getAsString()).isEmpty() && colorName.charAt(0) == '#') {
            int rgb = Integer.parseInt(colorName.substring(1), 16);
            String closestChatColor = this.getClosestChatColor(rgb);
            object.addProperty("color", closestChatColor);
        }
        if ((hoverEvent = object.getAsJsonObject("hoverEvent")) == null || !hoverEvent.has("contents")) {
            return;
        }
        JsonObject convertedObject = (JsonObject)ComponentUtil.convertJson(object, ComponentUtil.SerializerVersion.V1_16, ComponentUtil.SerializerVersion.V1_15);
        object.add("hoverEvent", convertedObject.getAsJsonObject("hoverEvent"));
    }

    private String getClosestChatColor(int rgb) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        ChatColor closest = null;
        int smallestDiff = 0;
        for (ChatColor color : COLORS) {
            if (color.rgb == rgb) {
                return color.colorName;
            }
            int rAverage = (color.r + r) / 2;
            int rDiff = color.r - r;
            int gDiff = color.g - g;
            int bDiff = color.b - b;
            int diff = (2 + (rAverage >> 8)) * rDiff * rDiff + 4 * gDiff * gDiff + (2 + (255 - rAverage >> 8)) * bDiff * bDiff;
            if (closest != null && diff >= smallestDiff) continue;
            closest = color;
            smallestDiff = diff;
        }
        return closest.colorName;
    }

    private static final class ChatColor {
        private final String colorName;
        private final int rgb;
        private final int r;
        private final int g;
        private final int b;

        ChatColor(String colorName, int rgb) {
            this.colorName = colorName;
            this.rgb = rgb;
            this.r = rgb >> 16 & 0xFF;
            this.g = rgb >> 8 & 0xFF;
            this.b = rgb & 0xFF;
        }
    }
}

