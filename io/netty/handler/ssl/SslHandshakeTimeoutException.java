/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import javax.net.ssl.SSLHandshakeException;

public final class SslHandshakeTimeoutException
extends SSLHandshakeException {
    SslHandshakeTimeoutException(String reason) {
        super(reason);
    }
}

