/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;

public class StreamClosedException
extends IOException {
    private static final long serialVersionUID = 1L;

    public StreamClosedException() {
        super("Stream already closed");
    }

    public StreamClosedException(String message) {
        super(message);
    }
}

