/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.ReferenceCounted;
import java.security.cert.X509Certificate;

interface OpenSslKeyMaterial
extends ReferenceCounted {
    public X509Certificate[] certificateChain();

    public long certificateChainAddress();

    public long privateKeyAddress();

    @Override
    public OpenSslKeyMaterial retain();

    @Override
    public OpenSslKeyMaterial retain(int var1);

    public OpenSslKeyMaterial touch();

    public OpenSslKeyMaterial touch(Object var1);

    @Override
    public boolean release();

    @Override
    public boolean release(int var1);
}

