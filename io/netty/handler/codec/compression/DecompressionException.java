/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.handler.codec.DecoderException;

public class DecompressionException
extends DecoderException {
    private static final long serialVersionUID = 3546272712208105199L;

    public DecompressionException() {
    }

    public DecompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecompressionException(String message) {
        super(message);
    }

    public DecompressionException(Throwable cause) {
        super(cause);
    }
}

