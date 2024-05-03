/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.SslCompletionEvent;

public final class SniCompletionEvent
extends SslCompletionEvent {
    private final String hostname;

    SniCompletionEvent(String hostname) {
        this.hostname = hostname;
    }

    SniCompletionEvent(String hostname, Throwable cause) {
        super(cause);
        this.hostname = hostname;
    }

    SniCompletionEvent(Throwable cause) {
        this(null, cause);
    }

    public String hostname() {
        return this.hostname;
    }

    @Override
    public String toString() {
        Throwable cause = this.cause();
        return cause == null ? this.getClass().getSimpleName() + "(SUCCESS='" + this.hostname + "'\")" : this.getClass().getSimpleName() + '(' + cause + ')';
    }
}

