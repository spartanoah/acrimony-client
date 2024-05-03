/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLSession;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.nio.AbstractHttp1StreamDuplexer;
import org.apache.hc.core5.http.impl.nio.HttpConnectionEventHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

class AbstractHttp1IOEventHandler
implements HttpConnectionEventHandler {
    final AbstractHttp1StreamDuplexer<?, ?> streamDuplexer;

    AbstractHttp1IOEventHandler(AbstractHttp1StreamDuplexer<?, ?> streamDuplexer) {
        this.streamDuplexer = Args.notNull(streamDuplexer, "Stream multiplexer");
    }

    @Override
    public void connected(IOSession session) throws IOException {
        try {
            this.streamDuplexer.onConnect();
        } catch (HttpException ex) {
            this.streamDuplexer.onException(ex);
        }
    }

    @Override
    public void inputReady(IOSession session, ByteBuffer src) throws IOException {
        try {
            this.streamDuplexer.onInput(src);
        } catch (HttpException ex) {
            this.streamDuplexer.onException(ex);
        }
    }

    @Override
    public void outputReady(IOSession session) throws IOException {
        try {
            this.streamDuplexer.onOutput();
        } catch (HttpException ex) {
            this.streamDuplexer.onException(ex);
        }
    }

    @Override
    public void timeout(IOSession session, Timeout timeout) throws IOException {
        try {
            this.streamDuplexer.onTimeout(timeout);
        } catch (HttpException ex) {
            this.streamDuplexer.onException(ex);
        }
    }

    @Override
    public void exception(IOSession session, Exception cause) {
        this.streamDuplexer.onException(cause);
    }

    @Override
    public void disconnected(IOSession session) {
        this.streamDuplexer.onDisconnect();
    }

    @Override
    public void close() throws IOException {
        this.streamDuplexer.close();
    }

    @Override
    public void close(CloseMode closeMode) {
        this.streamDuplexer.close(closeMode);
    }

    @Override
    public boolean isOpen() {
        return this.streamDuplexer.isOpen();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.streamDuplexer.setSocketTimeout(timeout);
    }

    @Override
    public SSLSession getSSLSession() {
        return this.streamDuplexer.getSSLSession();
    }

    @Override
    public EndpointDetails getEndpointDetails() {
        return this.streamDuplexer.getEndpointDetails();
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.streamDuplexer.getSocketTimeout();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.streamDuplexer.getProtocolVersion();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.streamDuplexer.getRemoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.streamDuplexer.getLocalAddress();
    }
}

