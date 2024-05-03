/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.DecoderResult;

public interface HttpObject {
    public DecoderResult getDecoderResult();

    public void setDecoderResult(DecoderResult var1);
}

