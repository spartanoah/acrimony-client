/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.AbstractSingleCoreIOReactor;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionImpl;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.reactor.IOSessionRequest;
import org.apache.hc.core5.reactor.InternalChannel;
import org.apache.hc.core5.reactor.InternalConnectChannel;
import org.apache.hc.core5.reactor.InternalDataChannel;
import org.apache.hc.core5.reactor.InternalDataChannelFactory;
import org.apache.hc.core5.reactor.SocksProxyProtocolHandlerFactory;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.Timeout;

class SingleCoreIOReactor
extends AbstractSingleCoreIOReactor
implements ConnectionInitiator {
    private static final int MAX_CHANNEL_REQUESTS = 10000;
    private final IOEventHandlerFactory eventHandlerFactory;
    private final IOReactorConfig reactorConfig;
    private final Decorator<IOSession> ioSessionDecorator;
    private final IOSessionListener sessionListener;
    private final Callback<IOSession> sessionShutdownCallback;
    private final Queue<InternalDataChannel> closedSessions;
    private final Queue<SocketChannel> channelQueue;
    private final Queue<IOSessionRequest> requestQueue;
    private final AtomicBoolean shutdownInitiated;
    private final long selectTimeoutMillis;
    private volatile long lastTimeoutCheckMillis;

    SingleCoreIOReactor(Callback<Exception> exceptionCallback, IOEventHandlerFactory eventHandlerFactory, IOReactorConfig reactorConfig, Decorator<IOSession> ioSessionDecorator, IOSessionListener sessionListener, Callback<IOSession> sessionShutdownCallback) {
        super(exceptionCallback);
        this.eventHandlerFactory = Args.notNull(eventHandlerFactory, "Event handler factory");
        this.reactorConfig = Args.notNull(reactorConfig, "I/O reactor config");
        this.ioSessionDecorator = ioSessionDecorator;
        this.sessionListener = sessionListener;
        this.sessionShutdownCallback = sessionShutdownCallback;
        this.shutdownInitiated = new AtomicBoolean(false);
        this.closedSessions = new ConcurrentLinkedQueue<InternalDataChannel>();
        this.channelQueue = new ConcurrentLinkedQueue<SocketChannel>();
        this.requestQueue = new ConcurrentLinkedQueue<IOSessionRequest>();
        this.selectTimeoutMillis = this.reactorConfig.getSelectInterval().toMilliseconds();
    }

    void enqueueChannel(SocketChannel socketChannel) throws IOReactorShutdownException {
        Args.notNull(socketChannel, "SocketChannel");
        if (this.getStatus().compareTo(IOReactorStatus.ACTIVE) > 0) {
            throw new IOReactorShutdownException("I/O reactor has been shut down");
        }
        this.channelQueue.add(socketChannel);
        this.selector.wakeup();
    }

    @Override
    void doTerminate() {
        this.closePendingChannels();
        this.closePendingConnectionRequests();
        this.processClosedSessions();
    }

    @Override
    void doExecute() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            int readyCount = this.selector.select(this.selectTimeoutMillis);
            if (this.getStatus().compareTo(IOReactorStatus.SHUTTING_DOWN) >= 0) {
                if (this.shutdownInitiated.compareAndSet(false, true)) {
                    this.initiateSessionShutdown();
                }
                this.closePendingChannels();
            }
            if (this.getStatus() == IOReactorStatus.SHUT_DOWN) break;
            if (readyCount > 0) {
                this.processEvents(this.selector.selectedKeys());
            }
            this.validateActiveChannels();
            this.processClosedSessions();
            if (this.getStatus() == IOReactorStatus.ACTIVE) {
                this.processPendingChannels();
                this.processPendingConnectionRequests();
            }
            if ((this.getStatus() != IOReactorStatus.SHUTTING_DOWN || !this.selector.keys().isEmpty()) && this.getStatus() != IOReactorStatus.SHUT_DOWN) continue;
            break;
        }
    }

    private void initiateSessionShutdown() {
        if (this.sessionShutdownCallback != null) {
            Set<SelectionKey> keys = this.selector.keys();
            for (SelectionKey key : keys) {
                InternalChannel channel = (InternalChannel)key.attachment();
                if (!(channel instanceof InternalDataChannel)) continue;
                this.sessionShutdownCallback.execute((InternalDataChannel)channel);
            }
        }
    }

    private void validateActiveChannels() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.lastTimeoutCheckMillis >= this.selectTimeoutMillis) {
            this.lastTimeoutCheckMillis = currentTimeMillis;
            for (SelectionKey key : this.selector.keys()) {
                this.checkTimeout(key, currentTimeMillis);
            }
        }
    }

    private void processEvents(Set<SelectionKey> selectedKeys) {
        for (SelectionKey key : selectedKeys) {
            InternalChannel channel = (InternalChannel)key.attachment();
            if (channel == null) continue;
            try {
                channel.handleIOEvent(key.readyOps());
            } catch (CancelledKeyException ex) {
                this.close(CloseMode.GRACEFUL);
            }
        }
        selectedKeys.clear();
    }

    private void processPendingChannels() throws IOException {
        SocketChannel socketChannel;
        for (int i = 0; i < 10000 && (socketChannel = this.channelQueue.poll()) != null; ++i) {
            SelectionKey key;
            try {
                this.prepareSocket(socketChannel.socket());
                socketChannel.configureBlocking(false);
            } catch (IOException ex) {
                this.logException(ex);
                try {
                    socketChannel.close();
                } catch (IOException ex2) {
                    this.logException(ex2);
                }
                throw ex;
            }
            try {
                key = socketChannel.register(this.selector, 1);
            } catch (ClosedChannelException ex) {
                return;
            }
            IOSessionImpl ioSession = new IOSessionImpl("a", key, socketChannel);
            InternalDataChannel dataChannel = new InternalDataChannel(this.ioSessionDecorator != null ? this.ioSessionDecorator.decorate(ioSession) : ioSession, null, this.sessionListener, this.closedSessions);
            dataChannel.upgrade(this.eventHandlerFactory.createHandler(dataChannel, null));
            dataChannel.setSocketTimeout(this.reactorConfig.getSoTimeout());
            key.attach(dataChannel);
            dataChannel.handleIOEvent(8);
        }
    }

    private void processClosedSessions() {
        InternalDataChannel dataChannel;
        while ((dataChannel = this.closedSessions.poll()) != null) {
            try {
                dataChannel.disconnected();
            } catch (CancelledKeyException cancelledKeyException) {}
        }
    }

    private void checkTimeout(SelectionKey key, long nowMillis) {
        InternalChannel channel = (InternalChannel)key.attachment();
        if (channel != null) {
            channel.checkTimeout(nowMillis);
        }
    }

    @Override
    public Future<IOSession> connect(NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, SocketAddress localAddress, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) throws IOReactorShutdownException {
        Args.notNull(remoteEndpoint, "Remote endpoint");
        IOSessionRequest sessionRequest = new IOSessionRequest(remoteEndpoint, remoteAddress != null ? remoteAddress : new InetSocketAddress(remoteEndpoint.getHostName(), remoteEndpoint.getPort()), localAddress, timeout, attachment, callback);
        this.requestQueue.add(sessionRequest);
        this.selector.wakeup();
        return sessionRequest;
    }

    private void prepareSocket(Socket socket) throws IOException {
        int linger;
        socket.setTcpNoDelay(this.reactorConfig.isTcpNoDelay());
        socket.setKeepAlive(this.reactorConfig.isSoKeepalive());
        if (this.reactorConfig.getSndBufSize() > 0) {
            socket.setSendBufferSize(this.reactorConfig.getSndBufSize());
        }
        if (this.reactorConfig.getRcvBufSize() > 0) {
            socket.setReceiveBufferSize(this.reactorConfig.getRcvBufSize());
        }
        if ((linger = this.reactorConfig.getSoLinger().toSecondsIntBound()) >= 0) {
            socket.setSoLinger(true, linger);
        }
    }

    private void validateAddress(SocketAddress address) throws UnknownHostException {
        InetSocketAddress endpoint;
        if (address instanceof InetSocketAddress && (endpoint = (InetSocketAddress)address).isUnresolved()) {
            throw new UnknownHostException(endpoint.getHostName());
        }
    }

    private void processPendingConnectionRequests() {
        IOSessionRequest sessionRequest;
        for (int i = 0; i < 10000 && (sessionRequest = this.requestQueue.poll()) != null; ++i) {
            SocketChannel socketChannel;
            if (sessionRequest.isCancelled()) continue;
            try {
                socketChannel = SocketChannel.open();
            } catch (IOException ex) {
                sessionRequest.failed(ex);
                return;
            }
            try {
                this.processConnectionRequest(socketChannel, sessionRequest);
                continue;
            } catch (IOException | SecurityException ex) {
                Closer.closeQuietly(socketChannel);
                sessionRequest.failed(ex);
            }
        }
    }

    private void processConnectionRequest(final SocketChannel socketChannel, IOSessionRequest sessionRequest) throws IOException {
        boolean connected;
        IOEventHandlerFactory eventHandlerFactory;
        SocketAddress targetAddress;
        this.validateAddress(sessionRequest.localAddress);
        this.validateAddress(sessionRequest.remoteAddress);
        socketChannel.configureBlocking(false);
        this.prepareSocket(socketChannel.socket());
        if (sessionRequest.localAddress != null) {
            Socket sock = socketChannel.socket();
            sock.setReuseAddress(this.reactorConfig.isSoReuseAddress());
            sock.bind(sessionRequest.localAddress);
        }
        if (this.reactorConfig.getSocksProxyAddress() != null) {
            targetAddress = this.reactorConfig.getSocksProxyAddress();
            eventHandlerFactory = new SocksProxyProtocolHandlerFactory(sessionRequest.remoteAddress, this.reactorConfig.getSocksProxyUsername(), this.reactorConfig.getSocksProxyPassword(), this.eventHandlerFactory);
        } else {
            targetAddress = sessionRequest.remoteAddress;
            eventHandlerFactory = this.eventHandlerFactory;
        }
        try {
            connected = AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>(){

                @Override
                public Boolean run() throws IOException {
                    return socketChannel.connect(targetAddress);
                }
            });
        } catch (PrivilegedActionException e) {
            Asserts.check(e.getCause() instanceof IOException, "method contract violation only checked exceptions are wrapped: " + e.getCause());
            throw (IOException)e.getCause();
        }
        SelectionKey key = socketChannel.register(this.selector, 9);
        InternalConnectChannel channel = new InternalConnectChannel(key, socketChannel, sessionRequest, new InternalDataChannelFactory(){

            @Override
            public InternalDataChannel create(SelectionKey key, SocketChannel socketChannel, NamedEndpoint namedEndpoint, Object attachment) {
                IOSessionImpl ioSession = new IOSessionImpl("c", key, socketChannel);
                InternalDataChannel dataChannel = new InternalDataChannel(SingleCoreIOReactor.this.ioSessionDecorator != null ? (IOSession)SingleCoreIOReactor.this.ioSessionDecorator.decorate(ioSession) : ioSession, namedEndpoint, SingleCoreIOReactor.this.sessionListener, SingleCoreIOReactor.this.closedSessions);
                dataChannel.upgrade(eventHandlerFactory.createHandler(dataChannel, attachment));
                dataChannel.setSocketTimeout(SingleCoreIOReactor.this.reactorConfig.getSoTimeout());
                return dataChannel;
            }
        });
        if (connected) {
            channel.handleIOEvent(8);
        } else {
            key.attach(channel);
            sessionRequest.assign(channel);
        }
    }

    private void closePendingChannels() {
        SocketChannel socketChannel;
        while ((socketChannel = this.channelQueue.poll()) != null) {
            try {
                socketChannel.close();
            } catch (IOException ex) {
                this.logException(ex);
            }
        }
    }

    private void closePendingConnectionRequests() {
        IOSessionRequest sessionRequest;
        while ((sessionRequest = this.requestQueue.poll()) != null) {
            sessionRequest.cancel();
        }
    }
}

