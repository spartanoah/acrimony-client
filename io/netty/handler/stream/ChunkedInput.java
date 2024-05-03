/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.stream;

import io.netty.channel.ChannelHandlerContext;

public interface ChunkedInput<B> {
    public boolean isEndOfInput() throws Exception;

    public void close() throws Exception;

    public B readChunk(ChannelHandlerContext var1) throws Exception;
}

