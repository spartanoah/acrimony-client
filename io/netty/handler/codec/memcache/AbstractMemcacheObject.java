/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.memcache;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.memcache.MemcacheObject;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractMemcacheObject
extends AbstractReferenceCounted
implements MemcacheObject {
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    protected AbstractMemcacheObject() {
    }

    @Override
    public DecoderResult decoderResult() {
        return this.decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        this.decoderResult = ObjectUtil.checkNotNull(result, "DecoderResult should not be null.");
    }
}

