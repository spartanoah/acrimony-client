/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

public class ActivityAssets {
    private final long pointer;

    ActivityAssets(long pointer) {
        this.pointer = pointer;
    }

    public void setLargeImage(String assetKey) {
        if (assetKey.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setLargeImage(this.pointer, assetKey);
    }

    public String getLargeImage() {
        return this.getLargeImage(this.pointer);
    }

    public void setLargeText(String text) {
        if (text.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setLargeText(this.pointer, text);
    }

    public String getLargeText() {
        return this.getLargeText(this.pointer);
    }

    public void setSmallImage(String assetKey) {
        if (assetKey.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setSmallImage(this.pointer, assetKey);
    }

    public String getSmallImage() {
        return this.getSmallImage(this.pointer);
    }

    public void setSmallText(String text) {
        if (text.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setSmallText(this.pointer, text);
    }

    public String getSmallText() {
        return this.getSmallText(this.pointer);
    }

    private native void setLargeImage(long var1, String var3);

    private native String getLargeImage(long var1);

    private native void setLargeText(long var1, String var3);

    private native String getLargeText(long var1);

    private native void setSmallImage(long var1, String var3);

    private native String getSmallImage(long var1);

    private native void setSmallText(long var1, String var3);

    private native String getSmallText(long var1);
}

