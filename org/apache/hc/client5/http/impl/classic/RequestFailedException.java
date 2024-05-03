/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.InterruptedIOException;

public class RequestFailedException
extends InterruptedIOException {
    private static final long serialVersionUID = 4973849966012490112L;

    public RequestFailedException(String message) {
        super(message);
    }

    public RequestFailedException(String message, Throwable cause) {
        super(message);
        if (cause != null) {
            this.initCause(cause);
        }
    }
}

