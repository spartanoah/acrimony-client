/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.MultiResolutionImageSupport$MappedMultiResolutionImage
 *  com.formdev.flatlaf.util.MultiResolutionImageSupport$ProducerMultiResolutionImage
 *  java.awt.image.BaseMultiResolutionImage
 *  java.awt.image.MultiResolutionImage
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.util.MultiResolutionImageSupport;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.MultiResolutionImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class MultiResolutionImageSupport {
    public static boolean isAvailable() {
        return true;
    }

    public static boolean isMultiResolutionImage(Image image) {
        return image instanceof MultiResolutionImage;
    }

    public static Image create(int baseImageIndex, Image ... resolutionVariants) {
        return new BaseMultiResolutionImage(baseImageIndex, resolutionVariants);
    }

    public static Image create(int baseImageIndex, Dimension[] dimensions, Function<Dimension, Image> producer) {
        return new ProducerMultiResolutionImage(dimensions, producer);
    }

    public static Image map(Image image, Function<Image, Image> mapper) {
        return image instanceof MultiResolutionImage ? new MappedMultiResolutionImage(image, mapper) : mapper.apply(image);
    }

    public static Image getResolutionVariant(Image image, int destImageWidth, int destImageHeight) {
        return image instanceof MultiResolutionImage ? ((MultiResolutionImage)image).getResolutionVariant((double)destImageWidth, (double)destImageHeight) : image;
    }

    public static List<Image> getResolutionVariants(Image image) {
        return image instanceof MultiResolutionImage ? ((MultiResolutionImage)image).getResolutionVariants() : Collections.singletonList(image);
    }
}

