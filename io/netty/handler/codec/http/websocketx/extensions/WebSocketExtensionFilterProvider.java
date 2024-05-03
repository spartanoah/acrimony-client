/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http.websocketx.extensions;

import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

public interface WebSocketExtensionFilterProvider {
    public static final WebSocketExtensionFilterProvider DEFAULT = new WebSocketExtensionFilterProvider(){

        @Override
        public WebSocketExtensionFilter encoderFilter() {
            return WebSocketExtensionFilter.NEVER_SKIP;
        }

        @Override
        public WebSocketExtensionFilter decoderFilter() {
            return WebSocketExtensionFilter.NEVER_SKIP;
        }
    };

    public WebSocketExtensionFilter encoderFilter();

    public WebSocketExtensionFilter decoderFilter();
}

