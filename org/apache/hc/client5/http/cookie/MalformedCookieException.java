/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cookie;

import org.apache.hc.core5.http.ProtocolException;

public class MalformedCookieException
extends ProtocolException {
    private static final long serialVersionUID = -6695462944287282185L;

    public MalformedCookieException() {
    }

    public MalformedCookieException(String message) {
        super(message);
    }

    public MalformedCookieException(String message, Throwable cause) {
        super(message, cause);
    }
}

