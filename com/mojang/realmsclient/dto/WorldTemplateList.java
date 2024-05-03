/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.dto.WorldTemplate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldTemplateList
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<WorldTemplate> templates;

    public static WorldTemplateList parse(String json) {
        WorldTemplateList list = new WorldTemplateList();
        list.templates = new ArrayList<WorldTemplate>();
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(json).getAsJsonObject();
            if (object.get("templates").isJsonArray()) {
                Iterator<JsonElement> it = object.get("templates").getAsJsonArray().iterator();
                while (it.hasNext()) {
                    list.templates.add(WorldTemplate.parse(it.next().getAsJsonObject()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not parse WorldTemplateList: " + e.getMessage());
        }
        return list;
    }
}

