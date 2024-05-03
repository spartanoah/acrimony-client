/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractSocksMessage
implements SocksMessage {
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    @Override
    public DecoderResult decoderResult() {
        return this.decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
    }
}

