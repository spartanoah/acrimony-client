/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.Backup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupList {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String json) {
        JsonParser jsonParser = new JsonParser();
        BackupList backupList = new BackupList();
        backupList.backups = new ArrayList<Backup>();
        try {
            JsonElement node = jsonParser.parse(json).getAsJsonObject().get("backups");
            if (node.isJsonArray()) {
                Iterator<JsonElement> iterator = node.getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    backupList.backups.add(Backup.parse(iterator.next()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not parse BackupList: " + e.getMessage());
        }
        return backupList;
    }
}

