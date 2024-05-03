/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSLSession
 *  io.netty.internal.tcnative.SSLSessionCache
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslSession;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionId;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.internal.tcnative.SSLSession;
import io.netty.internal.tcnative.SSLSessionCache;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.SystemPropertyUtil;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.security.cert.X509Certificate;

class OpenSslSessionCache
implements SSLSessionCache {
    private static final OpenSslSession[] EMPTY_SESSIONS = new OpenSslSession[0];
    private static final int DEFAULT_CACHE_SIZE;
    private final OpenSslEngineMap engineMap;
    private final Map<OpenSslSessionId, NativeSslSession> sessions = new LinkedHashMap<OpenSslSessionId, NativeSslSession>(){
        private static final long serialVersionUID = -7773696788135734448L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<OpenSslSessionId, NativeSslSession> eldest) {
            int maxSize = OpenSslSessionCache.this.maximumCacheSize.get();
            if (maxSize >= 0 && this.size() > maxSize) {
                OpenSslSessionCache.this.removeSessionWithId(eldest.getKey());
            }
            return false;
        }
    };
    private final AtomicInteger maximumCacheSize = new AtomicInteger(DEFAULT_CACHE_SIZE);
    private final AtomicInteger sessionTimeout = new AtomicInteger(300);
    private int sessionCounter;

    OpenSslSessionCache(OpenSslEngineMap engineMap) {
        this.engineMap = engineMap;
    }

    final void setSessionTimeout(int seconds) {
        int oldTimeout = this.sessionTimeout.getAndSet(seconds);
        if (oldTimeout > seconds) {
            this.clear();
        }
    }

    final int getSessionTimeout() {
        return this.sessionTimeout.get();
    }

    protected boolean sessionCreated(NativeSslSession session) {
        return true;
    }

    protected void sessionRemoved(NativeSslSession session) {
    }

    final void setSessionCacheSize(int size) {
        long oldSize = this.maximumCacheSize.getAndSet(size);
        if (oldSize > (long)size || size == 0) {
            this.clear();
        }
    }

    final int getSessionCacheSize() {
        return this.maximumCacheSize.get();
    }

    private void expungeInvalidSessions() {
        NativeSslSession session;
        if (this.sessions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<OpenSslSessionId, NativeSslSession>> iterator = this.sessions.entrySet().iterator();
        while (iterator.hasNext() && !(session = iterator.next().getValue()).isValid(now)) {
            iterator.remove();
            this.notifyRemovalAndFree(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final boolean sessionCreated(long ssl, long sslSession) {
        ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
        if (engine == null) {
            return false;
        }
        NativeSslSession session = new NativeSslSession(sslSession, engine.getPeerHost(), engine.getPeerPort(), (long)this.getSessionTimeout() * 1000L);
        engine.setSessionId(session.sessionId());
        OpenSslSessionCache openSslSessionCache = this;
        synchronized (openSslSessionCache) {
            if (++this.sessionCounter == 255) {
                this.sessionCounter = 0;
                this.expungeInvalidSessions();
            }
            if (!this.sessionCreated(session)) {
                session.close();
                return false;
            }
            NativeSslSession old = this.sessions.put(session.sessionId(), session);
            if (old != null) {
                this.notifyRemovalAndFree(old);
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final long getSession(long ssl, byte[] sessionId) {
        NativeSslSession session;
        OpenSslSessionId id = new OpenSslSessionId(sessionId);
        OpenSslSessionCache openSslSessionCache = this;
        synchronized (openSslSessionCache) {
            session = this.sessions.get(id);
            if (session == null) {
                return -1L;
            }
            if (!session.isValid() || !session.upRef()) {
                this.removeSessionWithId(session.sessionId());
                return -1L;
            }
            if (session.shouldBeSingleUse()) {
                this.removeSessionWithId(session.sessionId());
            }
        }
        session.updateLastAccessedTime();
        return session.session();
    }

    void setSession(long ssl, String host, int port) {
    }

    final synchronized void removeSessionWithId(OpenSslSessionId id) {
        NativeSslSession sslSession = this.sessions.remove(id);
        if (sslSession != null) {
            this.notifyRemovalAndFree(sslSession);
        }
    }

    final synchronized boolean containsSessionWithId(OpenSslSessionId id) {
        return this.sessions.containsKey(id);
    }

    private void notifyRemovalAndFree(NativeSslSession session) {
        this.sessionRemoved(session);
        session.free();
    }

    final synchronized OpenSslSession getSession(OpenSslSessionId id) {
        NativeSslSession session = this.sessions.get(id);
        if (session != null && !session.isValid()) {
            this.removeSessionWithId(session.sessionId());
            return null;
        }
        return session;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final List<OpenSslSessionId> getIds() {
        OpenSslSession[] sessionsArray;
        OpenSslSessionCache openSslSessionCache = this;
        synchronized (openSslSessionCache) {
            sessionsArray = this.sessions.values().toArray(EMPTY_SESSIONS);
        }
        ArrayList<OpenSslSessionId> ids = new ArrayList<OpenSslSessionId>(sessionsArray.length);
        for (OpenSslSession session : sessionsArray) {
            if (!session.isValid()) continue;
            ids.add(session.sessionId());
        }
        return ids;
    }

    synchronized void clear() {
        Iterator<Map.Entry<OpenSslSessionId, NativeSslSession>> iterator = this.sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            NativeSslSession session = iterator.next().getValue();
            iterator.remove();
            this.notifyRemovalAndFree(session);
        }
    }

    static {
        int cacheSize = SystemPropertyUtil.getInt("javax.net.ssl.sessionCacheSize", 20480);
        DEFAULT_CACHE_SIZE = cacheSize >= 0 ? cacheSize : 20480;
    }

    static final class NativeSslSession
    implements OpenSslSession {
        static final ResourceLeakDetector<NativeSslSession> LEAK_DETECTOR = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(NativeSslSession.class);
        private final ResourceLeakTracker<NativeSslSession> leakTracker;
        private final long session;
        private final String peerHost;
        private final int peerPort;
        private final OpenSslSessionId id;
        private final long timeout;
        private final long creationTime;
        private volatile long lastAccessedTime;
        private volatile boolean valid;
        private boolean freed;

        NativeSslSession(long session, String peerHost, int peerPort, long timeout) {
            this.lastAccessedTime = this.creationTime = System.currentTimeMillis();
            this.valid = true;
            this.session = session;
            this.peerHost = peerHost;
            this.peerPort = peerPort;
            this.timeout = timeout;
            this.id = new OpenSslSessionId(SSLSession.getSessionId((long)session));
            this.leakTracker = LEAK_DETECTOR.track(this);
        }

        @Override
        public void setSessionId(OpenSslSessionId id) {
            throw new UnsupportedOperationException();
        }

        boolean shouldBeSingleUse() {
            assert (!this.freed);
            return SSLSession.shouldBeSingleUse((long)this.session);
        }

        long session() {
            assert (!this.freed);
            return this.session;
        }

        boolean upRef() {
            assert (!this.freed);
            return SSLSession.upRef((long)this.session);
        }

        synchronized void free() {
            this.close();
            SSLSession.free((long)this.session);
        }

        void close() {
            assert (!this.freed);
            this.freed = true;
            this.invalidate();
            if (this.leakTracker != null) {
                this.leakTracker.close(this);
            }
        }

        @Override
        public OpenSslSessionId sessionId() {
            return this.id;
        }

        boolean isValid(long now) {
            return this.creationTime + this.timeout >= now && this.valid;
        }

        @Override
        public void setLocalCertificate(Certificate[] localCertificate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OpenSslSessionContext getSessionContext() {
            return null;
        }

        @Override
        public void tryExpandApplicationBufferSize(int packetLengthDataOnly) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handshakeFinished(byte[] id, String cipher, String protocol, byte[] peerCertificate, byte[][] peerCertificateChain, long creationTime, long timeout) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getId() {
            return this.id.cloneBytes();
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        void updateLastAccessedTime() {
            this.lastAccessedTime = System.currentTimeMillis();
        }

        @Override
        public long getLastAccessedTime() {
            return this.lastAccessedTime;
        }

        @Override
        public void invalidate() {
            this.valid = false;
        }

        @Override
        public boolean isValid() {
            return this.isValid(System.currentTimeMillis());
        }

        @Override
        public void putValue(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(String name) {
            return null;
        }

        @Override
        public void removeValue(String name) {
        }

        @Override
        public String[] getValueNames() {
            return EmptyArrays.EMPTY_STRINGS;
        }

        @Override
        public Certificate[] getPeerCertificates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Certificate[] getLocalCertificates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public X509Certificate[] getPeerCertificateChain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getPeerPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getLocalPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCipherSuite() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getPeerHost() {
            return this.peerHost;
        }

        @Override
        public int getPeerPort() {
            return this.peerPort;
        }

        @Override
        public int getPacketBufferSize() {
            return ReferenceCountedOpenSslEngine.MAX_RECORD_SIZE;
        }

        @Override
        public int getApplicationBufferSize() {
            return ReferenceCountedOpenSslEngine.MAX_PLAINTEXT_LENGTH;
        }

        public int hashCode() {
            return this.id.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OpenSslSession)) {
                return false;
            }
            OpenSslSession session1 = (OpenSslSession)o;
            return this.id.equals(session1.sessionId());
        }
    }
}

