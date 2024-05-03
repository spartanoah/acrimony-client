/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

public final class SslHandshakeCompletionEvent {
    public static final SslHandshakeCompletionEvent SUCCESS = new SslHandshakeCompletionEvent();
    private final Throwable cause;

    private SslHandshakeCompletionEvent() {
        this.cause = null;
    }

    public SslHandshakeCompletionEvent(Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.cause = cause;
    }

    public boolean isSuccess() {
        return this.cause == null;
    }

    public Throwable cause() {
        return this.cause;
    }
}

