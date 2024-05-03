/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.image;

public class ImageDimensions {
    private final int width;
    private final int height;

    ImageDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String toString() {
        return "ImageDimensions{width=" + this.width + ", height=" + this.height + '}';
    }
}

