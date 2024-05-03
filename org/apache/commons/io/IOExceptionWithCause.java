/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io;

import java.io.IOException;

public class IOExceptionWithCause
extends IOException {
    private static final long serialVersionUID = 1L;

    public IOExceptionWithCause(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    public IOExceptionWithCause(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.initCause(cause);
    }
}

