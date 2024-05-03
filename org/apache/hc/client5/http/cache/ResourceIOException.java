/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.io.IOException;

public class ResourceIOException
extends IOException {
    private static final long serialVersionUID = 1L;

    public ResourceIOException(String message) {
        super(message);
    }

    public ResourceIOException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}

