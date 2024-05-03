/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.nio;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;

@Internal
public interface ManagedAsyncClientConnection
extends HttpConnection,
TransportSecurityLayer {
    public void submitCommand(Command var1, Command.Priority var2);

    public void passivate();

    public void activate();
}

