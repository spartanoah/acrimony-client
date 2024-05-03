/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSL
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslSessionCache;
import io.netty.internal.tcnative.SSL;
import io.netty.util.AsciiString;
import java.util.HashMap;
import java.util.Map;

final class OpenSslClientSessionCache
extends OpenSslSessionCache {
    private final Map<HostPort, OpenSslSessionCache.NativeSslSession> sessions = new HashMap<HostPort, OpenSslSessionCache.NativeSslSession>();

    OpenSslClientSessionCache(OpenSslEngineMap engineMap) {
        super(engineMap);
    }

    @Override
    protected boolean sessionCreated(OpenSslSessionCache.NativeSslSession session) {
        assert (Thread.holdsLock(this));
        HostPort hostPort = OpenSslClientSessionCache.keyFor(session.getPeerHost(), session.getPeerPort());
        if (hostPort == null || this.sessions.containsKey(hostPort)) {
            return false;
        }
        this.sessions.put(hostPort, session);
        return true;
    }

    @Override
    protected void sessionRemoved(OpenSslSessionCache.NativeSslSession session) {
        assert (Thread.holdsLock(this));
        HostPort hostPort = OpenSslClientSessionCache.keyFor(session.getPeerHost(), session.getPeerPort());
        if (hostPort == null) {
            return;
        }
        this.sessions.remove(hostPort);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void setSession(long ssl, String host, int port) {
        boolean reused;
        OpenSslSessionCache.NativeSslSession session;
        HostPort hostPort = OpenSslClientSessionCache.keyFor(host, port);
        if (hostPort == null) {
            return;
        }
        OpenSslClientSessionCache openSslClientSessionCache = this;
        synchronized (openSslClientSessionCache) {
            session = this.sessions.get(hostPort);
            if (session == null) {
                return;
            }
            if (!session.isValid()) {
                this.removeSessionWithId(session.sessionId());
                return;
            }
            reused = SSL.setSession((long)ssl, (long)session.session());
        }
        if (reused) {
            if (session.shouldBeSingleUse()) {
                session.invalidate();
            }
            session.updateLastAccessedTime();
        }
    }

    private static HostPort keyFor(String host, int port) {
        if (host == null && port < 1) {
            return null;
        }
        return new HostPort(host, port);
    }

    @Override
    synchronized void clear() {
        super.clear();
        this.sessions.clear();
    }

    private static final class HostPort {
        private final int hash;
        private final String host;
        private final int port;

        HostPort(String host, int port) {
            this.host = host;
            this.port = port;
            this.hash = 31 * AsciiString.hashCode(host) + port;
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof HostPort)) {
                return false;
            }
            HostPort other = (HostPort)obj;
            return this.port == other.port && this.host.equalsIgnoreCase(other.host);
        }

        public String toString() {
            return "HostPort{host='" + this.host + '\'' + ", port=" + this.port + '}';
        }
    }
}

