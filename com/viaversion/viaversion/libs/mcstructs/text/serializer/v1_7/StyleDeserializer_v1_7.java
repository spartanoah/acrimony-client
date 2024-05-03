/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_7;

import com.viaversion.viaversion.libs.gson.JsonDeserializationContext;
import com.viaversion.viaversion.libs.gson.JsonDeserializer;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.TextHoverEvent;
import java.lang.reflect.Type;

public class StyleDeserializer_v1_7
implements JsonDeserializer<Style> {
    @Override
    public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject rawHoverEvent;
            JsonPrimitive rawAction;
            JsonObject rawClickEvent;
            JsonObject rawStyle = json.getAsJsonObject();
            Style style = new Style();
            if (rawStyle.has("bold")) {
                style.setBold(rawStyle.get("bold").getAsBoolean());
            }
            if (rawStyle.has("italic")) {
                style.setItalic(rawStyle.get("italic").getAsBoolean());
            }
            if (rawStyle.has("underlined")) {
                style.setUnderlined(rawStyle.get("underlined").getAsBoolean());
            }
            if (rawStyle.has("strikethrough")) {
                style.setStrikethrough(rawStyle.get("strikethrough").getAsBoolean());
            }
            if (rawStyle.has("obfuscated")) {
                style.setObfuscated(rawStyle.get("obfuscated").getAsBoolean());
            }
            if (rawStyle.has("color")) {
                style.setFormatting(TextFormatting.getByName(rawStyle.get("color").getAsString()));
            }
            if (rawStyle.has("clickEvent") && (rawClickEvent = rawStyle.getAsJsonObject("clickEvent")) != null) {
                rawAction = rawClickEvent.getAsJsonPrimitive("action");
                JsonPrimitive rawValue = rawClickEvent.getAsJsonPrimitive("value");
                ClickEventAction action = null;
                String value = null;
                if (rawAction != null) {
                    action = ClickEventAction.getByName(rawAction.getAsString());
                }
                if (rawValue != null) {
                    value = rawValue.getAsString();
                }
                if (action != null && value != null && action.isUserDefinable()) {
                    style.setClickEvent(new ClickEvent(action, value));
                }
            }
            if (rawStyle.has("hoverEvent") && (rawHoverEvent = rawStyle.getAsJsonObject("hoverEvent")) != null) {
                rawAction = rawHoverEvent.getAsJsonPrimitive("action");
                HoverEventAction action = null;
                ATextComponent value = (ATextComponent)context.deserialize(rawHoverEvent.get("value"), (Type)((Object)ATextComponent.class));
                if (rawAction != null) {
                    action = HoverEventAction.getByName(rawAction.getAsString());
                }
                if (action != null && value != null && action.isUserDefinable()) {
                    style.setHoverEvent(new TextHoverEvent(action, value));
                }
            }
            return style;
        }
        return null;
    }
}

