/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public interface FullHttpRequest
extends HttpRequest,
FullHttpMessage {
    @Override
    public FullHttpRequest copy();

    @Override
    public FullHttpRequest retain(int var1);

    @Override
    public FullHttpRequest retain();

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion var1);

    @Override
    public FullHttpRequest setMethod(HttpMethod var1);

    @Override
    public FullHttpRequest setUri(String var1);
}

