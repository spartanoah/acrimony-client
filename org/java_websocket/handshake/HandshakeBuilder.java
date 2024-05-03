/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.Handshakedata;

public interface HandshakeBuilder
extends Handshakedata {
    public void setContent(byte[] var1);

    public void put(String var1, String var2);
}

