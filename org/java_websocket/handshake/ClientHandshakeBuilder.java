/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeBuilder;

public interface ClientHandshakeBuilder
extends HandshakeBuilder,
ClientHandshake {
    public void setResourceDescriptor(String var1);
}

