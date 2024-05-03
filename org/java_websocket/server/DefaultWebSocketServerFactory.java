/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketListener;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.drafts.Draft;

public class DefaultWebSocketServerFactory
implements WebSocketServerFactory {
    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d) {
        return new WebSocketImpl((WebSocketListener)a, d);
    }

    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> d) {
        return new WebSocketImpl((WebSocketListener)a, d);
    }

    @Override
    public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key) {
        return channel;
    }

    @Override
    public void close() {
    }
}

