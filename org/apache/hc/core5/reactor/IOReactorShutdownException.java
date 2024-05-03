/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

public class IOReactorShutdownException
extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public IOReactorShutdownException(String message) {
        super(message);
    }
}

