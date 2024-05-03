/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

public class ActivityPartySize {
    private final long pointer;

    ActivityPartySize(long pointer) {
        this.pointer = pointer;
    }

    public void setCurrentSize(int size) {
        this.setCurrentSize(this.pointer, size);
    }

    public int getCurrentSize() {
        return this.getCurrentSize(this.pointer);
    }

    public void setMaxSize(int size) {
        this.setMaxSize(this.pointer, size);
    }

    public int getMaxSize() {
        return this.getMaxSize(this.pointer);
    }

    private native void setCurrentSize(long var1, int var3);

    private native int getCurrentSize(long var1);

    private native void setMaxSize(long var1, int var3);

    private native int getMaxSize(long var1);
}

