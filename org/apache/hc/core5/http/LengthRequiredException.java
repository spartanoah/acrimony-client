/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.ProtocolException;

public class LengthRequiredException
extends ProtocolException {
    private static final long serialVersionUID = 1049109801075840707L;

    public LengthRequiredException() {
        super("Length required");
    }

    public LengthRequiredException(String message) {
        super(message);
    }
}

