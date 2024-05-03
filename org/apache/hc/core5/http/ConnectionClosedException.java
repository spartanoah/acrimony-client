/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;

public class ConnectionClosedException
extends IOException {
    private static final long serialVersionUID = 617550366255636674L;

    public ConnectionClosedException() {
        super("Connection is closed");
    }

    public ConnectionClosedException(String message) {
        super(HttpException.clean(message));
    }

    public ConnectionClosedException(String format, Object ... args) {
        super(HttpException.clean(String.format(format, args)));
    }

    public ConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}

