/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.util.internal.StringUtil;

final class DefaultHttp2SettingsAckFrame
implements Http2SettingsAckFrame {
    DefaultHttp2SettingsAckFrame() {
    }

    @Override
    public String name() {
        return "SETTINGS(ACK)";
    }

    public String toString() {
        return StringUtil.simpleClassName(this);
    }
}

