/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.LastHttpContent;

public interface FullHttpMessage
extends HttpMessage,
LastHttpContent {
    @Override
    public FullHttpMessage copy();

    @Override
    public FullHttpMessage retain(int var1);

    @Override
    public FullHttpMessage retain();
}

