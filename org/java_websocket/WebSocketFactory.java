/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket;

import java.util.List;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.drafts.Draft;

public interface WebSocketFactory {
    public WebSocket createWebSocket(WebSocketAdapter var1, Draft var2);

    public WebSocket createWebSocket(WebSocketAdapter var1, List<Draft> var2);
}

