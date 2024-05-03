/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.util.internal.ObjectUtil;

public class ZstdOptions
implements CompressionOptions {
    private final int blockSize;
    private final int compressionLevel;
    private final int maxEncodeSize;
    static final ZstdOptions DEFAULT = new ZstdOptions(3, 65536, 0x2000000);

    ZstdOptions(int compressionLevel, int blockSize, int maxEncodeSize) {
        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 22, "compressionLevel");
        this.blockSize = ObjectUtil.checkPositive(blockSize, "blockSize");
        this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
    }

    public int compressionLevel() {
        return this.compressionLevel;
    }

    public int blockSize() {
        return this.blockSize;
    }

    public int maxEncodeSize() {
        return this.maxEncodeSize;
    }
}

