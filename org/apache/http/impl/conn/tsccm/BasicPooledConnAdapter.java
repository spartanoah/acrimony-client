/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.conn.tsccm;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.AbstractPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

@Deprecated
public class BasicPooledConnAdapter
extends AbstractPooledConnAdapter {
    protected BasicPooledConnAdapter(ThreadSafeClientConnManager tsccm, AbstractPoolEntry entry) {
        super((ClientConnectionManager)tsccm, entry);
        this.markReusable();
    }

    protected ClientConnectionManager getManager() {
        return super.getManager();
    }

    protected AbstractPoolEntry getPoolEntry() {
        return super.getPoolEntry();
    }

    protected void detach() {
        super.detach();
    }
}

