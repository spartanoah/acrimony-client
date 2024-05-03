/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslAsyncPrivateKeyMethod;
import io.netty.handler.ssl.OpenSslPrivateKeyMethod;
import io.netty.handler.ssl.SslContextOption;

public final class OpenSslContextOption<T>
extends SslContextOption<T> {
    public static final OpenSslContextOption<Boolean> USE_TASKS = new OpenSslContextOption("USE_TASKS");
    public static final OpenSslContextOption<Boolean> TLS_FALSE_START = new OpenSslContextOption("TLS_FALSE_START");
    public static final OpenSslContextOption<OpenSslPrivateKeyMethod> PRIVATE_KEY_METHOD = new OpenSslContextOption("PRIVATE_KEY_METHOD");
    public static final OpenSslContextOption<OpenSslAsyncPrivateKeyMethod> ASYNC_PRIVATE_KEY_METHOD = new OpenSslContextOption("ASYNC_PRIVATE_KEY_METHOD");

    private OpenSslContextOption(String name) {
        super(name);
    }
}

