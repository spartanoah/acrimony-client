/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import org.apache.hc.core5.http.ProtocolException;

public class MalformedChallengeException
extends ProtocolException {
    private static final long serialVersionUID = 814586927989932284L;

    public MalformedChallengeException() {
    }

    public MalformedChallengeException(String message) {
        super(message);
    }

    public MalformedChallengeException(String message, Throwable cause) {
        super(message, cause);
    }
}

