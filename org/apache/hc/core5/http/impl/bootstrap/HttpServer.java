/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.bootstrap.RequestListener;
import org.apache.hc.core5.http.impl.bootstrap.Worker;
import org.apache.hc.core5.http.impl.bootstrap.WorkerPoolExecutor;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

public class HttpServer
implements ModalCloseable {
    private final int port;
    private final InetAddress ifAddress;
    private final SocketConfig socketConfig;
    private final ServerSocketFactory serverSocketFactory;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private final Callback<SSLParameters> sslSetupHandler;
    private final ExceptionListener exceptionListener;
    private final ThreadPoolExecutor listenerExecutorService;
    private final ThreadGroup workerThreads;
    private final WorkerPoolExecutor workerExecutorService;
    private final AtomicReference<Status> status;
    private volatile ServerSocket serverSocket;
    private volatile RequestListener requestListener;

    @Internal
    public HttpServer(int port, HttpService httpService, InetAddress ifAddress, SocketConfig socketConfig, ServerSocketFactory serverSocketFactory, HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory, Callback<SSLParameters> sslSetupHandler, ExceptionListener exceptionListener) {
        this.port = Args.notNegative(port, "Port value is negative");
        this.httpService = Args.notNull(httpService, "HTTP service");
        this.ifAddress = ifAddress;
        this.socketConfig = socketConfig != null ? socketConfig : SocketConfig.DEFAULT;
        ServerSocketFactory serverSocketFactory2 = this.serverSocketFactory = serverSocketFactory != null ? serverSocketFactory : ServerSocketFactory.getDefault();
        this.connectionFactory = connectionFactory != null ? connectionFactory : new DefaultBHttpServerConnectionFactory(this.serverSocketFactory instanceof SSLServerSocketFactory ? URIScheme.HTTPS.id : URIScheme.HTTP.id, Http1Config.DEFAULT, CharCodingConfig.DEFAULT);
        this.sslSetupHandler = sslSetupHandler;
        this.exceptionListener = exceptionListener != null ? exceptionListener : ExceptionListener.NO_OP;
        this.listenerExecutorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), new DefaultThreadFactory("HTTP-listener-" + this.port));
        this.workerThreads = new ThreadGroup("HTTP-workers");
        this.workerExecutorService = new WorkerPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new DefaultThreadFactory("HTTP-worker", this.workerThreads, true));
        this.status = new AtomicReference<Status>(Status.READY);
    }

    public InetAddress getInetAddress() {
        ServerSocket localSocket = this.serverSocket;
        if (localSocket != null) {
            return localSocket.getInetAddress();
        }
        return null;
    }

    public int getLocalPort() {
        ServerSocket localSocket = this.serverSocket;
        if (localSocket != null) {
            return localSocket.getLocalPort();
        }
        return -1;
    }

    public void start() throws IOException {
        if (this.status.compareAndSet(Status.READY, Status.ACTIVE)) {
            this.serverSocket = this.serverSocketFactory.createServerSocket(this.port, this.socketConfig.getBacklogSize(), this.ifAddress);
            this.serverSocket.setReuseAddress(this.socketConfig.isSoReuseAddress());
            if (this.socketConfig.getRcvBufSize() > 0) {
                this.serverSocket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
            }
            if (this.sslSetupHandler != null && this.serverSocket instanceof SSLServerSocket) {
                SSLServerSocket sslServerSocket = (SSLServerSocket)this.serverSocket;
                SSLParameters sslParameters = sslServerSocket.getSSLParameters();
                this.sslSetupHandler.execute(sslParameters);
                sslServerSocket.setSSLParameters(sslParameters);
            }
            this.requestListener = new RequestListener(this.socketConfig, this.serverSocket, this.httpService, this.connectionFactory, this.exceptionListener, this.workerExecutorService);
            this.listenerExecutorService.execute(this.requestListener);
        }
    }

    public void stop() {
        if (this.status.compareAndSet(Status.ACTIVE, Status.STOPPING)) {
            this.listenerExecutorService.shutdownNow();
            this.workerExecutorService.shutdown();
            RequestListener local = this.requestListener;
            if (local != null) {
                try {
                    local.terminate();
                } catch (IOException ex) {
                    this.exceptionListener.onError(ex);
                }
            }
            this.workerThreads.interrupt();
        }
    }

    public void initiateShutdown() {
        this.stop();
    }

    public void awaitTermination(TimeValue waitTime) throws InterruptedException {
        Args.notNull(waitTime, "Wait time");
        this.workerExecutorService.awaitTermination(waitTime.getDuration(), waitTime.getTimeUnit());
    }

    @Override
    public void close(CloseMode closeMode) {
        this.initiateShutdown();
        if (closeMode == CloseMode.GRACEFUL) {
            try {
                this.awaitTermination(TimeValue.ofSeconds(5L));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        Set<Worker> workers = this.workerExecutorService.getWorkers();
        for (Worker worker : workers) {
            Closer.close(worker.getConnection(), CloseMode.GRACEFUL);
        }
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    static enum Status {
        READY,
        ACTIVE,
        STOPPING;

    }
}

