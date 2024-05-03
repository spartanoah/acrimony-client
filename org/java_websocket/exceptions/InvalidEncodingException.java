/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.exceptions;

import java.io.UnsupportedEncodingException;

public class InvalidEncodingException
extends RuntimeException {
    private final UnsupportedEncodingException encodingException;

    public InvalidEncodingException(UnsupportedEncodingException encodingException) {
        if (encodingException == null) {
            throw new IllegalArgumentException();
        }
        this.encodingException = encodingException;
    }

    public UnsupportedEncodingException getEncodingException() {
        return this.encodingException;
    }
}

