/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.Handshakedata;

public interface ServerHandshake
extends Handshakedata {
    public short getHttpStatus();

    public String getHttpStatusMessage();
}

