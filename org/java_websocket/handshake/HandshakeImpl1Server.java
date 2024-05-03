/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.HandshakedataImpl1;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public class HandshakeImpl1Server
extends HandshakedataImpl1
implements ServerHandshakeBuilder {
    private short httpstatus;
    private String httpstatusmessage;

    @Override
    public String getHttpStatusMessage() {
        return this.httpstatusmessage;
    }

    @Override
    public short getHttpStatus() {
        return this.httpstatus;
    }

    @Override
    public void setHttpStatusMessage(String message) {
        this.httpstatusmessage = message;
    }

    @Override
    public void setHttpStatus(short status) {
        this.httpstatus = status;
    }
}

