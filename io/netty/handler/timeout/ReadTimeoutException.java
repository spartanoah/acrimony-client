/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.timeout;

import io.netty.handler.timeout.TimeoutException;

public final class ReadTimeoutException
extends TimeoutException {
    private static final long serialVersionUID = 169287984113283421L;
    public static final ReadTimeoutException INSTANCE = new ReadTimeoutException();

    private ReadTimeoutException() {
    }
}

