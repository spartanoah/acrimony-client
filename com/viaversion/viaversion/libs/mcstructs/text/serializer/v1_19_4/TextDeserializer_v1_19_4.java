/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_19_4;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonDeserializationContext;
import com.viaversion.viaversion.libs.gson.JsonDeserializer;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.KeybindComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.ScoreComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.SelectorComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.BlockNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.EntityNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.StorageNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.utils.JsonUtils;
import java.lang.reflect.Type;

public class TextDeserializer_v1_19_4
implements JsonDeserializer<ATextComponent> {
    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ATextComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Style newStyle;
        void var5_21;
        if (json.isJsonPrimitive()) {
            return new StringComponent(json.getAsString());
        }
        if (json.isJsonArray()) {
            void var5_7;
            JsonArray array = json.getAsJsonArray();
            Object var5_6 = null;
            for (JsonElement element : array) {
                ATextComponent serializedElement = this.deserialize(element, element.getClass(), context);
                if (var5_7 == null) {
                    ATextComponent aTextComponent = serializedElement;
                    continue;
                }
                var5_7.append(serializedElement);
            }
            return var5_7;
        }
        if (!json.isJsonObject()) throw new JsonParseException("Don't know how to turn " + json + " into a Component");
        JsonObject rawComponent = json.getAsJsonObject();
        if (rawComponent.has("text")) {
            StringComponent stringComponent = new StringComponent(JsonUtils.getString(rawComponent, "text"));
        } else if (rawComponent.has("translate")) {
            String translate = JsonUtils.getString(rawComponent, "translate");
            String fallback = JsonUtils.getString(rawComponent, "fallback", null);
            if (rawComponent.has("with")) {
                JsonArray with = rawComponent.getAsJsonArray("with");
                Object[] args = new Object[with.size()];
                for (int i = 0; i < with.size(); ++i) {
                    StringComponent stringComponent;
                    ATextComponent element = this.deserialize(with.get(i), typeOfT, context);
                    args[i] = element;
                    if (!(element instanceof StringComponent) || !(stringComponent = (StringComponent)element).getStyle().isEmpty() || !stringComponent.getSiblings().isEmpty()) continue;
                    args[i] = stringComponent.getText();
                }
                TranslationComponent translationComponent = new TranslationComponent(translate, args).setFallback(fallback);
            } else {
                TranslationComponent translationComponent = new TranslationComponent(translate, new Object[0]).setFallback(fallback);
            }
        } else if (rawComponent.has("score")) {
            JsonObject score = rawComponent.getAsJsonObject("score");
            if (!score.has("name") || !score.has("objective")) {
                throw new JsonParseException("A score component needs at least a name and an objective");
            }
            ScoreComponent scoreComponent = new ScoreComponent(JsonUtils.getString(score, "name"), JsonUtils.getString(score, "objective"));
        } else if (rawComponent.has("selector")) {
            if (rawComponent.has("separator")) {
                SelectorComponent selectorComponent = new SelectorComponent(JsonUtils.getString(rawComponent, "selector"), this.deserialize(rawComponent.get("separator"), typeOfT, context));
            } else {
                SelectorComponent selectorComponent = new SelectorComponent(JsonUtils.getString(rawComponent, "selector"), null);
            }
        } else if (rawComponent.has("keybind")) {
            KeybindComponent keybindComponent = new KeybindComponent(JsonUtils.getString(rawComponent, "keybind"));
        } else {
            if (!rawComponent.has("nbt")) throw new JsonParseException("Don't know how to turn " + json + " into a Component");
            String nbt = JsonUtils.getString(rawComponent, "nbt");
            boolean interpret = JsonUtils.getBoolean(rawComponent, "interpret", false);
            ATextComponent separator = null;
            if (rawComponent.has("separator")) {
                separator = this.deserialize(rawComponent.get("separator"), typeOfT, context);
            }
            if (rawComponent.has("block")) {
                BlockNbtComponent blockNbtComponent = new BlockNbtComponent(nbt, interpret, separator, JsonUtils.getString(rawComponent, "block"));
            } else if (rawComponent.has("entity")) {
                EntityNbtComponent entityNbtComponent = new EntityNbtComponent(nbt, interpret, separator, JsonUtils.getString(rawComponent, "entity"));
            } else {
                if (!rawComponent.has("storage")) throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                StorageNbtComponent storageNbtComponent = new StorageNbtComponent(nbt, interpret, separator, Identifier.of(JsonUtils.getString(rawComponent, "storage")));
            }
        }
        if (rawComponent.has("extra")) {
            JsonArray extra = rawComponent.getAsJsonArray("extra");
            if (extra.isEmpty()) {
                throw new JsonParseException("Unexpected empty array of components");
            }
            for (JsonElement element : extra) {
                var5_21.append(this.deserialize(element, typeOfT, context));
            }
        }
        if ((newStyle = (Style)context.deserialize(rawComponent, (Type)((Object)Style.class))) == null) return var5_21;
        var5_21.setStyle(newStyle);
        return var5_21;
    }
}

