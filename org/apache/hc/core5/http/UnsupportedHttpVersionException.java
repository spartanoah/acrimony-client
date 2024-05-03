/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;

public class UnsupportedHttpVersionException
extends ProtocolException {
    private static final long serialVersionUID = -1348448090193107031L;

    public UnsupportedHttpVersionException() {
    }

    public UnsupportedHttpVersionException(ProtocolVersion protocolVersion) {
        super("Unsupported version: " + protocolVersion);
    }

    public UnsupportedHttpVersionException(String message) {
        super(message);
    }
}

