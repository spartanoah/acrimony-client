/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.reactor.AbstractSingleCoreIOReactor;
import org.apache.hc.core5.reactor.ConnectionAcceptor;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.ListenerEndpoint;
import org.apache.hc.core5.reactor.ListenerEndpointImpl;
import org.apache.hc.core5.reactor.ListenerEndpointRequest;

class SingleCoreListeningIOReactor
extends AbstractSingleCoreIOReactor
implements ConnectionAcceptor {
    private final IOReactorConfig reactorConfig;
    private final Callback<SocketChannel> callback;
    private final Queue<ListenerEndpointRequest> requestQueue;
    private final ConcurrentMap<ListenerEndpoint, Boolean> endpoints;
    private final AtomicBoolean paused;
    private final long selectTimeoutMillis;

    SingleCoreListeningIOReactor(Callback<Exception> exceptionCallback, IOReactorConfig ioReactorConfig, Callback<SocketChannel> callback) {
        super(exceptionCallback);
        this.reactorConfig = ioReactorConfig != null ? ioReactorConfig : IOReactorConfig.DEFAULT;
        this.callback = callback;
        this.requestQueue = new ConcurrentLinkedQueue<ListenerEndpointRequest>();
        this.endpoints = new ConcurrentHashMap<ListenerEndpoint, Boolean>();
        this.paused = new AtomicBoolean(false);
        this.selectTimeoutMillis = this.reactorConfig.getSelectInterval().toMilliseconds();
    }

    @Override
    void doTerminate() {
        ListenerEndpointRequest request;
        while ((request = this.requestQueue.poll()) != null) {
            request.cancel();
        }
    }

    @Override
    protected final void doExecute() throws IOException {
        while (!Thread.currentThread().isInterrupted() && this.getStatus() == IOReactorStatus.ACTIVE) {
            int readyCount = this.selector.select(this.selectTimeoutMillis);
            if (this.getStatus() != IOReactorStatus.ACTIVE) break;
            this.processEvents(readyCount);
        }
    }

    private void processEvents(int readyCount) throws IOException {
        if (!this.paused.get()) {
            this.processSessionRequests();
        }
        if (readyCount > 0) {
            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                this.processEvent(key);
            }
            selectedKeys.clear();
        }
    }

    private void processEvent(SelectionKey key) throws IOException {
        try {
            if (key.isAcceptable()) {
                SocketChannel socketChannel;
                ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
                while ((socketChannel = serverChannel.accept()) != null) {
                    this.callback.execute(socketChannel);
                }
            }
        } catch (CancelledKeyException ex) {
            ListenerEndpoint endpoint = (ListenerEndpoint)key.attachment();
            this.endpoints.remove(endpoint);
            key.attach(null);
        }
    }

    @Override
    public Future<ListenerEndpoint> listen(SocketAddress address, FutureCallback<ListenerEndpoint> callback) {
        if (this.getStatus().compareTo(IOReactorStatus.SHUTTING_DOWN) >= 0) {
            throw new IOReactorShutdownException("I/O reactor has been shut down");
        }
        BasicFuture<ListenerEndpoint> future = new BasicFuture<ListenerEndpoint>(callback);
        this.requestQueue.add(new ListenerEndpointRequest(address, future));
        this.selector.wakeup();
        return future;
    }

    private void processSessionRequests() throws IOException {
        ListenerEndpointRequest request;
        while ((request = this.requestQueue.poll()) != null) {
            if (request.isCancelled()) continue;
            SocketAddress address = request.address;
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            try {
                ServerSocket socket = serverChannel.socket();
                socket.setReuseAddress(this.reactorConfig.isSoReuseAddress());
                if (this.reactorConfig.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.reactorConfig.getRcvBufSize());
                }
                serverChannel.configureBlocking(false);
                try {
                    socket.bind(address, this.reactorConfig.getBacklogSize());
                } catch (BindException ex) {
                    BindException detailedEx = new BindException(String.format("Socket bind failure for socket %s, address=%s, BacklogSize=%d: %s", socket, address, this.reactorConfig.getBacklogSize(), ex));
                    detailedEx.setStackTrace(ex.getStackTrace());
                    throw detailedEx;
                }
                SelectionKey key = serverChannel.register(this.selector, 16);
                key.attach(request);
                ListenerEndpointImpl endpoint = new ListenerEndpointImpl(key, socket.getLocalSocketAddress());
                this.endpoints.put(endpoint, Boolean.TRUE);
                request.completed(endpoint);
            } catch (IOException ex) {
                Closer.closeQuietly(serverChannel);
                request.failed(ex);
            }
        }
    }

    @Override
    public Set<ListenerEndpoint> getEndpoints() {
        HashSet<ListenerEndpoint> set = new HashSet<ListenerEndpoint>();
        Iterator it = this.endpoints.keySet().iterator();
        while (it.hasNext()) {
            ListenerEndpoint endpoint = (ListenerEndpoint)it.next();
            if (!endpoint.isClosed()) {
                set.add(endpoint);
                continue;
            }
            it.remove();
        }
        return set;
    }

    @Override
    public void pause() throws IOException {
        if (this.paused.compareAndSet(false, true)) {
            Iterator it = this.endpoints.keySet().iterator();
            while (it.hasNext()) {
                ListenerEndpoint endpoint = (ListenerEndpoint)it.next();
                if (!endpoint.isClosed()) {
                    endpoint.close();
                    this.requestQueue.add(new ListenerEndpointRequest(endpoint.getAddress(), null));
                }
                it.remove();
            }
        }
    }

    @Override
    public void resume() throws IOException {
        if (this.paused.compareAndSet(true, false)) {
            this.selector.wakeup();
        }
    }
}

