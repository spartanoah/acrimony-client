/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.io.SocketTimeoutExceptionFactory;
import org.apache.hc.core5.reactor.IOSessionRequest;
import org.apache.hc.core5.reactor.InternalChannel;
import org.apache.hc.core5.reactor.InternalDataChannel;
import org.apache.hc.core5.reactor.InternalDataChannelFactory;
import org.apache.hc.core5.util.Timeout;

final class InternalConnectChannel
extends InternalChannel {
    private final SelectionKey key;
    private final SocketChannel socketChannel;
    private final IOSessionRequest sessionRequest;
    private final long creationTimeMillis;
    private final InternalDataChannelFactory dataChannelFactory;

    InternalConnectChannel(SelectionKey key, SocketChannel socketChannel, IOSessionRequest sessionRequest, InternalDataChannelFactory dataChannelFactory) {
        this.key = key;
        this.socketChannel = socketChannel;
        this.sessionRequest = sessionRequest;
        this.creationTimeMillis = System.currentTimeMillis();
        this.dataChannelFactory = dataChannelFactory;
    }

    @Override
    void onIOEvent(int readyOps) throws IOException {
        if ((readyOps & 8) != 0) {
            long now;
            if (this.socketChannel.isConnectionPending()) {
                this.socketChannel.finishConnect();
            }
            if (this.checkTimeout(now = System.currentTimeMillis())) {
                InternalDataChannel dataChannel = this.dataChannelFactory.create(this.key, this.socketChannel, this.sessionRequest.remoteEndpoint, this.sessionRequest.attachment);
                this.key.attach(dataChannel);
                this.sessionRequest.completed(dataChannel);
                dataChannel.handleIOEvent(8);
            }
        }
    }

    @Override
    Timeout getTimeout() {
        return this.sessionRequest.timeout;
    }

    @Override
    long getLastEventTime() {
        return this.creationTimeMillis;
    }

    @Override
    void onTimeout(Timeout timeout) throws IOException {
        this.sessionRequest.failed(SocketTimeoutExceptionFactory.create(timeout));
        this.close();
    }

    @Override
    void onException(Exception cause) {
        this.sessionRequest.failed(cause);
    }

    @Override
    public void close() throws IOException {
        this.key.cancel();
        this.socketChannel.close();
    }

    @Override
    public void close(CloseMode closeMode) {
        this.key.cancel();
        Closer.closeQuietly(this.socketChannel);
    }

    public String toString() {
        return this.sessionRequest.toString();
    }
}

