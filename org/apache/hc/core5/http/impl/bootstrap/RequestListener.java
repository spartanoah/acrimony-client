/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.impl.bootstrap.Worker;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.io.SocketConfig;

class RequestListener
implements Runnable {
    private final SocketConfig socketConfig;
    private final ServerSocket serverSocket;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory;
    private final ExceptionListener exceptionListener;
    private final ExecutorService executorService;
    private final AtomicBoolean terminated;

    public RequestListener(SocketConfig socketConfig, ServerSocket serversocket, HttpService httpService, HttpConnectionFactory<? extends HttpServerConnection> connectionFactory, ExceptionListener exceptionListener, ExecutorService executorService) {
        this.socketConfig = socketConfig;
        this.serverSocket = serversocket;
        this.connectionFactory = connectionFactory;
        this.httpService = httpService;
        this.exceptionListener = exceptionListener;
        this.executorService = executorService;
        this.terminated = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        try {
            while (!this.isTerminated() && !Thread.interrupted()) {
                Socket socket = this.serverSocket.accept();
                socket.setSoTimeout(this.socketConfig.getSoTimeout().toMillisecondsIntBound());
                socket.setKeepAlive(this.socketConfig.isSoKeepAlive());
                socket.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
                if (this.socketConfig.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
                }
                if (this.socketConfig.getSndBufSize() > 0) {
                    socket.setSendBufferSize(this.socketConfig.getSndBufSize());
                }
                if (this.socketConfig.getSoLinger().toSeconds() >= 0L) {
                    socket.setSoLinger(true, this.socketConfig.getSoLinger().toSecondsIntBound());
                }
                HttpServerConnection conn = this.connectionFactory.createConnection(socket);
                Worker worker = new Worker(this.httpService, conn, this.exceptionListener);
                this.executorService.execute(worker);
            }
        } catch (Exception ex) {
            this.exceptionListener.onError(ex);
        }
    }

    public boolean isTerminated() {
        return this.terminated.get();
    }

    public void terminate() throws IOException {
        if (this.terminated.compareAndSet(false, true)) {
            this.serverSocket.close();
        }
    }
}

