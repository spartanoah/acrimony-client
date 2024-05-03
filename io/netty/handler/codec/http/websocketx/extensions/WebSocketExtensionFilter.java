/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http.websocketx.extensions;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public interface WebSocketExtensionFilter {
    public static final WebSocketExtensionFilter NEVER_SKIP = new WebSocketExtensionFilter(){

        @Override
        public boolean mustSkip(WebSocketFrame frame) {
            return false;
        }
    };
    public static final WebSocketExtensionFilter ALWAYS_SKIP = new WebSocketExtensionFilter(){

        @Override
        public boolean mustSkip(WebSocketFrame frame) {
            return true;
        }
    };

    public boolean mustSkip(WebSocketFrame var1);
}

