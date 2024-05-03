/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.server;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

public class CustomSSLWebSocketServerFactory
extends DefaultSSLWebSocketServerFactory {
    private final String[] enabledProtocols;
    private final String[] enabledCiphersuites;

    public CustomSSLWebSocketServerFactory(SSLContext sslContext, String[] enabledProtocols, String[] enabledCiphersuites) {
        this(sslContext, Executors.newSingleThreadScheduledExecutor(), enabledProtocols, enabledCiphersuites);
    }

    public CustomSSLWebSocketServerFactory(SSLContext sslContext, ExecutorService executerService, String[] enabledProtocols, String[] enabledCiphersuites) {
        super(sslContext, executerService);
        this.enabledProtocols = enabledProtocols;
        this.enabledCiphersuites = enabledCiphersuites;
    }

    @Override
    public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key) throws IOException {
        SSLEngine e = this.sslcontext.createSSLEngine();
        if (this.enabledProtocols != null) {
            e.setEnabledProtocols(this.enabledProtocols);
        }
        if (this.enabledCiphersuites != null) {
            e.setEnabledCipherSuites(this.enabledCiphersuites);
        }
        e.setUseClientMode(false);
        return new SSLSocketChannel2(channel, e, this.exec, key);
    }
}

