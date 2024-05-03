/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshake;

public interface ServerHandshakeBuilder
extends HandshakeBuilder,
ServerHandshake {
    public void setHttpStatus(short var1);

    public void setHttpStatusMessage(String var1);
}

