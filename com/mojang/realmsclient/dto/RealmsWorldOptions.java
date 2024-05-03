/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.realms.RealmsScreen;

public class RealmsWorldOptions {
    public Boolean pvp;
    public Boolean spawnAnimals;
    public Boolean spawnMonsters;
    public Boolean spawnNPCs;
    public Integer spawnProtection;
    public Boolean commandBlocks;
    public Boolean forceGameMode;
    public Integer difficulty;
    public Integer gameMode;
    public String slotName;
    public long templateId;
    public String templateImage;
    public boolean empty = false;
    private static boolean forceGameModeDefault = false;
    private static boolean pvpDefault = true;
    private static boolean spawnAnimalsDefault = true;
    private static boolean spawnMonstersDefault = true;
    private static boolean spawnNPCsDefault = true;
    private static int spawnProtectionDefault = 0;
    private static boolean commandBlocksDefault = false;
    private static int difficultyDefault = 2;
    private static int gameModeDefault = 0;
    private static String slotNameDefault = null;
    private static long templateIdDefault = -1L;
    private static String templateImageDefault = null;

    public RealmsWorldOptions(Boolean pvp, Boolean spawnAnimals, Boolean spawnMonsters, Boolean spawnNPCs, Integer spawnProtection, Boolean commandBlocks, Integer difficulty, Integer gameMode, Boolean forceGameMode, String slotName) {
        this.pvp = pvp;
        this.spawnAnimals = spawnAnimals;
        this.spawnMonsters = spawnMonsters;
        this.spawnNPCs = spawnNPCs;
        this.spawnProtection = spawnProtection;
        this.commandBlocks = commandBlocks;
        this.difficulty = difficulty;
        this.gameMode = gameMode;
        this.forceGameMode = forceGameMode;
        this.slotName = slotName;
    }

    public static RealmsWorldOptions getDefaults() {
        return new RealmsWorldOptions(pvpDefault, spawnAnimalsDefault, spawnMonstersDefault, spawnNPCsDefault, spawnProtectionDefault, commandBlocksDefault, difficultyDefault, gameModeDefault, forceGameModeDefault, slotNameDefault);
    }

    public static RealmsWorldOptions getEmptyDefaults() {
        RealmsWorldOptions options = new RealmsWorldOptions(pvpDefault, spawnAnimalsDefault, spawnMonstersDefault, spawnNPCsDefault, spawnProtectionDefault, commandBlocksDefault, difficultyDefault, gameModeDefault, forceGameModeDefault, slotNameDefault);
        options.setEmpty(true);
        return options;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public static RealmsWorldOptions parse(JsonObject jsonObject) {
        RealmsWorldOptions newOptions = new RealmsWorldOptions(JsonUtils.getBooleanOr("pvp", jsonObject, pvpDefault), JsonUtils.getBooleanOr("spawnAnimals", jsonObject, spawnAnimalsDefault), JsonUtils.getBooleanOr("spawnMonsters", jsonObject, spawnMonstersDefault), JsonUtils.getBooleanOr("spawnNPCs", jsonObject, spawnNPCsDefault), JsonUtils.getIntOr("spawnProtection", jsonObject, spawnProtectionDefault), JsonUtils.getBooleanOr("commandBlocks", jsonObject, commandBlocksDefault), JsonUtils.getIntOr("difficulty", jsonObject, difficultyDefault), JsonUtils.getIntOr("gameMode", jsonObject, gameModeDefault), JsonUtils.getBooleanOr("forceGameMode", jsonObject, forceGameModeDefault), JsonUtils.getStringOr("slotName", jsonObject, slotNameDefault));
        newOptions.templateId = JsonUtils.getLongOr("worldTemplateId", jsonObject, templateIdDefault);
        newOptions.templateImage = JsonUtils.getStringOr("worldTemplateImage", jsonObject, templateImageDefault);
        return newOptions;
    }

    public String getSlotName(int i) {
        if (this.slotName == null || this.slotName.equals("")) {
            if (this.empty) {
                return RealmsScreen.getLocalizedString("mco.configure.world.slot.empty");
            }
            return RealmsScreen.getLocalizedString("mco.configure.world.slot", i);
        }
        return this.slotName;
    }

    public String getDefaultSlotName(int i) {
        return RealmsScreen.getLocalizedString("mco.configure.world.slot", i);
    }

    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        if (this.pvp != pvpDefault) {
            jsonObject.addProperty("pvp", this.pvp);
        }
        if (this.spawnAnimals != spawnAnimalsDefault) {
            jsonObject.addProperty("spawnAnimals", this.spawnAnimals);
        }
        if (this.spawnMonsters != spawnMonstersDefault) {
            jsonObject.addProperty("spawnMonsters", this.spawnMonsters);
        }
        if (this.spawnNPCs != spawnNPCsDefault) {
            jsonObject.addProperty("spawnNPCs", this.spawnNPCs);
        }
        if (this.spawnProtection != spawnProtectionDefault) {
            jsonObject.addProperty("spawnProtection", this.spawnProtection);
        }
        if (this.commandBlocks != commandBlocksDefault) {
            jsonObject.addProperty("commandBlocks", this.commandBlocks);
        }
        if (this.difficulty != difficultyDefault) {
            jsonObject.addProperty("difficulty", this.difficulty);
        }
        if (this.gameMode != gameModeDefault) {
            jsonObject.addProperty("gameMode", this.gameMode);
        }
        if (this.forceGameMode != forceGameModeDefault) {
            jsonObject.addProperty("forceGameMode", this.forceGameMode);
        }
        if (!this.slotName.equals(slotNameDefault) && !this.slotName.equals("")) {
            jsonObject.addProperty("slotName", this.slotName);
        }
        return jsonObject.toString();
    }

    public RealmsWorldOptions clone() {
        return new RealmsWorldOptions(this.pvp, this.spawnAnimals, this.spawnMonsters, this.spawnNPCs, this.spawnProtection, this.commandBlocks, this.difficulty, this.gameMode, this.forceGameMode, this.slotName);
    }
}

