/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_16;

import com.viaversion.viaversion.libs.gson.JsonDeserializationContext;
import com.viaversion.viaversion.libs.gson.JsonDeserializer;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.utils.JsonUtils;
import java.lang.reflect.Type;

public class StyleDeserializer_v1_16
implements JsonDeserializer<Style> {
    @Override
    public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject rawHoverEvent;
        AHoverEvent hoverEvent;
        if (!json.isJsonObject()) {
            return null;
        }
        JsonObject rawStyle = json.getAsJsonObject();
        if (rawStyle == null) {
            return null;
        }
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
            style.setFormatting(TextFormatting.parse(JsonUtils.getString(rawStyle, "color")));
        }
        if (rawStyle.has("insertion")) {
            style.setInsertion(JsonUtils.getString(rawStyle, "insertion", null));
        }
        if (rawStyle.has("clickEvent")) {
            JsonObject rawClickEvent = JsonUtils.getJsonObject(rawStyle, "clickEvent");
            String rawAction = JsonUtils.getString(rawClickEvent, "action");
            ClickEventAction action = null;
            String value = JsonUtils.getString(rawClickEvent, "value");
            if (rawAction != null) {
                action = ClickEventAction.getByName(rawAction);
            }
            if (action != null && value != null && action.isUserDefinable()) {
                style.setClickEvent(new ClickEvent(action, value));
            }
        }
        if (rawStyle.has("hoverEvent") && (hoverEvent = (AHoverEvent)context.deserialize(rawHoverEvent = JsonUtils.getJsonObject(rawStyle, "hoverEvent"), (Type)((Object)AHoverEvent.class))) != null && hoverEvent.getAction().isUserDefinable()) {
            style.setHoverEvent(hoverEvent);
        }
        if (rawStyle.has("font")) {
            String font = JsonUtils.getString(rawStyle, "font");
            try {
                style.setFont(Identifier.of(font));
            } catch (Throwable t) {
                throw new JsonSyntaxException("Invalid font name: " + font);
            }
        }
        return style;
    }
}

