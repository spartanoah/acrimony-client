/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;

public class HttpStreamResetException
extends IOException {
    private static final long serialVersionUID = 1L;

    public HttpStreamResetException(String message) {
        super(message);
    }

    public HttpStreamResetException(String message, Throwable cause) {
        super(message, cause);
    }
}

