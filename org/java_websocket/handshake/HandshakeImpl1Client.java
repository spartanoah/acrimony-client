/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.handshake;

import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakedataImpl1;

public class HandshakeImpl1Client
extends HandshakedataImpl1
implements ClientHandshakeBuilder {
    private String resourceDescriptor = "*";

    @Override
    public void setResourceDescriptor(String resourceDescriptor) {
        if (resourceDescriptor == null) {
            throw new IllegalArgumentException("http resource descriptor must not be null");
        }
        this.resourceDescriptor = resourceDescriptor;
    }

    @Override
    public String getResourceDescriptor() {
        return this.resourceDescriptor;
    }
}

