/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.dto;

public class RealmsWorldResetDto {
    private String seed;
    private long worldTemplateId;
    private int levelType;
    private boolean generateStructures;

    public RealmsWorldResetDto(String seed, long worldTemplateId, int levelType, boolean generateStructures) {
        this.seed = seed;
        this.worldTemplateId = worldTemplateId;
        this.levelType = levelType;
        this.generateStructures = generateStructures;
    }
}

