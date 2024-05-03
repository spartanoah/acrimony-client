/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.conn;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.AbstractClientConnAdapter;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public abstract class AbstractPooledConnAdapter
extends AbstractClientConnAdapter {
    protected volatile AbstractPoolEntry poolEntry;

    protected AbstractPooledConnAdapter(ClientConnectionManager manager, AbstractPoolEntry entry) {
        super(manager, entry.connection);
        this.poolEntry = entry;
    }

    public String getId() {
        return null;
    }

    @Deprecated
    protected AbstractPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    protected void assertValid(AbstractPoolEntry entry) {
        if (this.isReleased() || entry == null) {
            throw new ConnectionShutdownException();
        }
    }

    @Deprecated
    protected final void assertAttached() {
        if (this.poolEntry == null) {
            throw new ConnectionShutdownException();
        }
    }

    protected synchronized void detach() {
        this.poolEntry = null;
        super.detach();
    }

    public HttpRoute getRoute() {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        return entry.tracker == null ? null : entry.tracker.toRoute();
    }

    public void open(HttpRoute route, HttpContext context, HttpParams params) throws IOException {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        entry.open(route, context, params);
    }

    public void tunnelTarget(boolean secure, HttpParams params) throws IOException {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        entry.tunnelTarget(secure, params);
    }

    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params) throws IOException {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        entry.tunnelProxy(next, secure, params);
    }

    public void layerProtocol(HttpContext context, HttpParams params) throws IOException {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        entry.layerProtocol(context, params);
    }

    public void close() throws IOException {
        OperatedClientConnection conn;
        AbstractPoolEntry entry = this.getPoolEntry();
        if (entry != null) {
            entry.shutdownEntry();
        }
        if ((conn = this.getWrappedConnection()) != null) {
            conn.close();
        }
    }

    public void shutdown() throws IOException {
        OperatedClientConnection conn;
        AbstractPoolEntry entry = this.getPoolEntry();
        if (entry != null) {
            entry.shutdownEntry();
        }
        if ((conn = this.getWrappedConnection()) != null) {
            conn.shutdown();
        }
    }

    public Object getState() {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        return entry.getState();
    }

    public void setState(Object state) {
        AbstractPoolEntry entry = this.getPoolEntry();
        this.assertValid(entry);
        entry.setState(state);
    }
}

