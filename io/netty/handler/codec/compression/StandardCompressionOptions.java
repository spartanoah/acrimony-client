/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.aayushatharva.brotli4j.encoder.Encoder$Parameters
 */
package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.BrotliOptions;
import io.netty.handler.codec.compression.DeflateOptions;
import io.netty.handler.codec.compression.GzipOptions;
import io.netty.handler.codec.compression.ZstdOptions;

public final class StandardCompressionOptions {
    private StandardCompressionOptions() {
    }

    public static BrotliOptions brotli() {
        return BrotliOptions.DEFAULT;
    }

    public static BrotliOptions brotli(Encoder.Parameters parameters) {
        return new BrotliOptions(parameters);
    }

    public static ZstdOptions zstd() {
        return ZstdOptions.DEFAULT;
    }

    public static ZstdOptions zstd(int compressionLevel, int blockSize, int maxEncodeSize) {
        return new ZstdOptions(compressionLevel, blockSize, maxEncodeSize);
    }

    public static GzipOptions gzip() {
        return GzipOptions.DEFAULT;
    }

    public static GzipOptions gzip(int compressionLevel, int windowBits, int memLevel) {
        return new GzipOptions(compressionLevel, windowBits, memLevel);
    }

    public static DeflateOptions deflate() {
        return DeflateOptions.DEFAULT;
    }

    public static DeflateOptions deflate(int compressionLevel, int windowBits, int memLevel) {
        return new DeflateOptions(compressionLevel, windowBits, memLevel);
    }
}

