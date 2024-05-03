/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.Http2Settings;

public interface Http2SettingsReceivedConsumer {
    public void consumeReceivedSettings(Http2Settings var1);
}

