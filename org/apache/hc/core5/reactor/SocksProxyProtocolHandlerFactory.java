/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.reactor.SocksProxyProtocolHandler;

public class SocksProxyProtocolHandlerFactory
implements IOEventHandlerFactory {
    private final InetSocketAddress targetAddress;
    private final String username;
    private final String password;
    private final IOEventHandlerFactory eventHandlerFactory;

    public SocksProxyProtocolHandlerFactory(SocketAddress targetAddress, String username, String password, IOEventHandlerFactory eventHandlerFactory) throws IOException {
        this.eventHandlerFactory = eventHandlerFactory;
        this.username = username;
        this.password = password;
        if (!(targetAddress instanceof InetSocketAddress)) {
            throw new IOException("Unsupported target address type for SOCKS proxy connection: " + targetAddress.getClass());
        }
        this.targetAddress = (InetSocketAddress)targetAddress;
    }

    @Override
    public IOEventHandler createHandler(ProtocolIOSession ioSession, Object attachment) {
        return new SocksProxyProtocolHandler(ioSession, attachment, this.targetAddress, this.username, this.password, this.eventHandlerFactory);
    }
}

