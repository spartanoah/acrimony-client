/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.aayushatharva.brotli4j.encoder.Encoder$Mode
 *  com.aayushatharva.brotli4j.encoder.Encoder$Parameters
 */
package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.util.internal.ObjectUtil;

public final class BrotliOptions
implements CompressionOptions {
    private final Encoder.Parameters parameters;
    static final BrotliOptions DEFAULT = new BrotliOptions(new Encoder.Parameters().setQuality(4).setMode(Encoder.Mode.TEXT));

    BrotliOptions(Encoder.Parameters parameters) {
        this.parameters = ObjectUtil.checkNotNull(parameters, "Parameters");
        if (!Brotli.isAvailable()) {
            throw new IllegalStateException("Brotli is not available", Brotli.cause());
        }
    }

    public Encoder.Parameters parameters() {
        return this.parameters;
    }
}

