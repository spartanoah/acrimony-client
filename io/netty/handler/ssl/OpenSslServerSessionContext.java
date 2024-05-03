/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSL
 *  io.netty.internal.tcnative.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.OpenSslSessionCache;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import java.util.concurrent.locks.Lock;

public final class OpenSslServerSessionContext
extends OpenSslSessionContext {
    OpenSslServerSessionContext(ReferenceCountedOpenSslContext context, OpenSslKeyMaterialProvider provider) {
        super(context, provider, SSL.SSL_SESS_CACHE_SERVER, new OpenSslSessionCache(context.engineMap));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setSessionIdContext(byte[] sidCtx) {
        Lock writerLock = this.context.ctxLock.writeLock();
        writerLock.lock();
        try {
            boolean bl = SSLContext.setSessionIdContext((long)this.context.ctx, (byte[])sidCtx);
            return bl;
        } finally {
            writerLock.unlock();
        }
    }
}

