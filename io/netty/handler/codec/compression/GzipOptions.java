/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.handler.codec.compression.DeflateOptions;

public final class GzipOptions
extends DeflateOptions {
    static final GzipOptions DEFAULT = new GzipOptions(6, 15, 8);

    GzipOptions(int compressionLevel, int windowBits, int memLevel) {
        super(compressionLevel, windowBits, memLevel);
    }
}

