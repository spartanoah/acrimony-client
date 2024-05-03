/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.image.ImageDimensions;
import de.jcm.discordgamesdk.image.ImageHandle;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ImageManager {
    private final long pointer;
    private final Core core;

    ImageManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public void fetch(ImageHandle handle, boolean refresh, BiConsumer<Result, ImageHandle> callback) {
        this.core.execute(() -> this.fetch(this.pointer, handle.getType().ordinal(), handle.getId(), handle.getSize(), refresh, Objects.requireNonNull(callback)));
    }

    public ImageDimensions getDimensions(ImageHandle handle) {
        Object ret = this.core.execute(() -> this.getDimensions(this.pointer, handle.getType().ordinal(), handle.getId(), handle.getSize()));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (ImageDimensions)ret;
    }

    public byte[] getData(ImageHandle handle, ImageDimensions dimensions) {
        return this.getData(handle, dimensions.getWidth() * dimensions.getHeight() * 4);
    }

    public byte[] getData(ImageHandle handle, int length) {
        Object ret = this.core.execute(() -> this.getData(this.pointer, handle.getType().ordinal(), handle.getId(), handle.getSize(), length));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (byte[])ret;
    }

    public BufferedImage getAsBufferedImage(ImageHandle handle, ImageDimensions dimensions) {
        byte[] data = this.getData(handle, dimensions);
        BufferedImage image = new BufferedImage(dimensions.getWidth(), dimensions.getHeight(), 6);
        image.getRaster().setDataElements(0, 0, dimensions.getWidth(), dimensions.getHeight(), data);
        return image;
    }

    public BufferedImage getAsBufferedImage(ImageHandle handle) {
        ImageDimensions dimensions = this.getDimensions(handle);
        return this.getAsBufferedImage(handle, dimensions);
    }

    private native void fetch(long var1, int var3, long var4, int var6, boolean var7, BiConsumer<Result, ImageHandle> var8);

    private native Object getDimensions(long var1, int var3, long var4, int var6);

    private native Object getData(long var1, int var3, long var4, int var6, int var7);
}

