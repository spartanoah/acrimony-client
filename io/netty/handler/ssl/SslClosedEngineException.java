/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import javax.net.ssl.SSLException;

public final class SslClosedEngineException
extends SSLException {
    private static final long serialVersionUID = -5204207600474401904L;

    public SslClosedEngineException(String reason) {
        super(reason);
    }
}

