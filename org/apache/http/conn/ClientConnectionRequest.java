/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.conn;

import java.util.concurrent.TimeUnit;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;

@Deprecated
public interface ClientConnectionRequest {
    public ManagedClientConnection getConnection(long var1, TimeUnit var3) throws InterruptedException, ConnectionPoolTimeoutException;

    public void abortRequest();
}

