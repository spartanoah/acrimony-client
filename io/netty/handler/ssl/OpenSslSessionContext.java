/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSL
 *  io.netty.internal.tcnative.SSLContext
 *  io.netty.internal.tcnative.SSLSessionCache
 *  io.netty.internal.tcnative.SessionTicketKey
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.OpenSslSessionCache;
import io.netty.handler.ssl.OpenSslSessionId;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.OpenSslSessionTicketKey;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SSLSessionCache;
import io.netty.internal.tcnative.SessionTicketKey;
import io.netty.util.internal.ObjectUtil;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

public abstract class OpenSslSessionContext
implements SSLSessionContext {
    private final OpenSslSessionStats stats;
    private final OpenSslKeyMaterialProvider provider;
    final ReferenceCountedOpenSslContext context;
    private final OpenSslSessionCache sessionCache;
    private final long mask;

    OpenSslSessionContext(ReferenceCountedOpenSslContext context, OpenSslKeyMaterialProvider provider, long mask, OpenSslSessionCache cache) {
        this.context = context;
        this.provider = provider;
        this.mask = mask;
        this.stats = new OpenSslSessionStats(context);
        this.sessionCache = cache;
        SSLContext.setSSLSessionCache((long)context.ctx, (SSLSessionCache)cache);
    }

    final boolean useKeyManager() {
        return this.provider != null;
    }

    @Override
    public void setSessionCacheSize(int size) {
        ObjectUtil.checkPositiveOrZero(size, "size");
        this.sessionCache.setSessionCacheSize(size);
    }

    @Override
    public int getSessionCacheSize() {
        return this.sessionCache.getSessionCacheSize();
    }

    @Override
    public void setSessionTimeout(int seconds) {
        ObjectUtil.checkPositiveOrZero(seconds, "seconds");
        Lock writerLock = this.context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setSessionCacheTimeout((long)this.context.ctx, (long)seconds);
            this.sessionCache.setSessionTimeout(seconds);
        } finally {
            writerLock.unlock();
        }
    }

    @Override
    public int getSessionTimeout() {
        return this.sessionCache.getSessionTimeout();
    }

    @Override
    public SSLSession getSession(byte[] bytes) {
        return this.sessionCache.getSession(new OpenSslSessionId(bytes));
    }

    @Override
    public Enumeration<byte[]> getIds() {
        return new Enumeration<byte[]>(){
            private final Iterator<OpenSslSessionId> ids;
            {
                this.ids = OpenSslSessionContext.this.sessionCache.getIds().iterator();
            }

            @Override
            public boolean hasMoreElements() {
                return this.ids.hasNext();
            }

            @Override
            public byte[] nextElement() {
                return this.ids.next().cloneBytes();
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public void setTicketKeys(byte[] keys) {
        if (keys.length % 48 != 0) {
            throw new IllegalArgumentException("keys.length % 48 != 0");
        }
        SessionTicketKey[] tickets = new SessionTicketKey[keys.length / 48];
        int a = 0;
        for (int i = 0; i < tickets.length; ++i) {
            byte[] name = Arrays.copyOfRange(keys, a, 16);
            byte[] hmacKey = Arrays.copyOfRange(keys, a += 16, 16);
            byte[] aesKey = Arrays.copyOfRange(keys, a, 16);
            a += 16;
            tickets[i += 16] = new SessionTicketKey(name, hmacKey, aesKey);
        }
        Lock writerLock = this.context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.clearOptions((long)this.context.ctx, (int)SSL.SSL_OP_NO_TICKET);
            SSLContext.setSessionTicketKeys((long)this.context.ctx, (SessionTicketKey[])tickets);
        } finally {
            writerLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTicketKeys(OpenSslSessionTicketKey ... keys) {
        ObjectUtil.checkNotNull(keys, "keys");
        SessionTicketKey[] ticketKeys = new SessionTicketKey[keys.length];
        for (int i = 0; i < ticketKeys.length; ++i) {
            ticketKeys[i] = keys[i].key;
        }
        Lock writerLock = this.context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.clearOptions((long)this.context.ctx, (int)SSL.SSL_OP_NO_TICKET);
            if (ticketKeys.length > 0) {
                SSLContext.setSessionTicketKeys((long)this.context.ctx, (SessionTicketKey[])ticketKeys);
            }
        } finally {
            writerLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setSessionCacheEnabled(boolean enabled) {
        long mode = enabled ? this.mask | SSL.SSL_SESS_CACHE_NO_INTERNAL_LOOKUP | SSL.SSL_SESS_CACHE_NO_INTERNAL_STORE : SSL.SSL_SESS_CACHE_OFF;
        Lock writerLock = this.context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setSessionCacheMode((long)this.context.ctx, (long)mode);
            if (!enabled) {
                this.sessionCache.clear();
            }
        } finally {
            writerLock.unlock();
        }
    }

    public boolean isSessionCacheEnabled() {
        Lock readerLock = this.context.ctxLock.readLock();
        readerLock.lock();
        try {
            boolean bl = (SSLContext.getSessionCacheMode((long)this.context.ctx) & this.mask) != 0L;
            return bl;
        } finally {
            readerLock.unlock();
        }
    }

    public OpenSslSessionStats stats() {
        return this.stats;
    }

    final void removeFromCache(OpenSslSessionId id) {
        this.sessionCache.removeSessionWithId(id);
    }

    final boolean isInCache(OpenSslSessionId id) {
        return this.sessionCache.containsSessionWithId(id);
    }

    void setSessionFromCache(String host, int port, long ssl) {
        this.sessionCache.setSession(ssl, host, port);
    }

    final void destroy() {
        if (this.provider != null) {
            this.provider.destroy();
        }
        this.sessionCache.clear();
    }
}

