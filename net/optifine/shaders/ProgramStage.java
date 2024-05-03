/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders;

public enum ProgramStage {
    NONE(""),
    SHADOW("shadow"),
    GBUFFERS("gbuffers"),
    DEFERRED("deferred"),
    COMPOSITE("composite");

    private String name;

    private ProgramStage(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

