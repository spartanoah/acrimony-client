/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.exceptions;

import org.java_websocket.exceptions.InvalidDataException;

public class InvalidHandshakeException
extends InvalidDataException {
    private static final long serialVersionUID = -1426533877490484964L;

    public InvalidHandshakeException() {
        super(1002);
    }

    public InvalidHandshakeException(String s, Throwable t) {
        super(1002, s, t);
    }

    public InvalidHandshakeException(String s) {
        super(1002, s);
    }

    public InvalidHandshakeException(Throwable t) {
        super(1002, t);
    }
}

