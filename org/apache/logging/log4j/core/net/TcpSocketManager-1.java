/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.SocketOptions;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.core.util.NullOutputStream;
import org.apache.logging.log4j.util.Strings;

public class TcpSocketManager
extends AbstractSocketManager {
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 30000;
    private static final int DEFAULT_PORT = 4560;
    private static final TcpSocketManagerFactory<TcpSocketManager, FactoryData> FACTORY = new TcpSocketManagerFactory();
    private final int reconnectionDelayMillis;
    private Reconnector reconnector;
    private Socket socket;
    private final SocketOptions socketOptions;
    private final boolean retry;
    private final boolean immediateFail;
    private final int connectTimeoutMillis;

    @Deprecated
    public TcpSocketManager(String name, OutputStream os, Socket socket, InetAddress inetAddress, String host, int port, int connectTimeoutMillis, int reconnectionDelayMillis, boolean immediateFail, Layout<? extends Serializable> layout, int bufferSize) {
        this(name, os, socket, inetAddress, host, port, connectTimeoutMillis, reconnectionDelayMillis, immediateFail, layout, bufferSize, null);
    }

    public TcpSocketManager(String name, OutputStream os, Socket socket, InetAddress inetAddress, String host, int port, int connectTimeoutMillis, int reconnectionDelayMillis, boolean immediateFail, Layout<? extends Serializable> layout, int bufferSize, SocketOptions socketOptions) {
        super(name, os, inetAddress, host, port, layout, true, bufferSize);
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.reconnectionDelayMillis = reconnectionDelayMillis;
        this.socket = socket;
        this.immediateFail = immediateFail;
        boolean bl = this.retry = reconnectionDelayMillis > 0;
        if (socket == null) {
            this.reconnector = this.createReconnector();
            this.reconnector.start();
        }
        this.socketOptions = socketOptions;
    }

    @Deprecated
    public static TcpSocketManager getSocketManager(String host, int port, int connectTimeoutMillis, int reconnectDelayMillis, boolean immediateFail, Layout<? extends Serializable> layout, int bufferSize) {
        return TcpSocketManager.getSocketManager(host, port, connectTimeoutMillis, reconnectDelayMillis, immediateFail, layout, bufferSize, null);
    }

    public static TcpSocketManager getSocketManager(String host, int port, int connectTimeoutMillis, int reconnectDelayMillis, boolean immediateFail, Layout<? extends Serializable> layout, int bufferSize, SocketOptions socketOptions) {
        if (Strings.isEmpty(host)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (port <= 0) {
            port = 4560;
        }
        if (reconnectDelayMillis == 0) {
            reconnectDelayMillis = 30000;
        }
        return (TcpSocketManager)TcpSocketManager.getManager("TCP:" + host + ':' + port, new FactoryData(host, port, connectTimeoutMillis, reconnectDelayMillis, immediateFail, layout, bufferSize, socketOptions), FACTORY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void write(byte[] bytes, int offset, int length, boolean immediateFlush) {
        if (this.socket == null) {
            if (this.reconnector != null && !this.immediateFail) {
                this.reconnector.latch();
            }
            if (this.socket == null) {
                throw new AppenderLoggingException("Error writing to " + this.getName() + ": socket not available");
            }
        }
        TcpSocketManager tcpSocketManager = this;
        synchronized (tcpSocketManager) {
            try {
                this.writeAndFlush(bytes, offset, length, immediateFlush);
            } catch (IOException causeEx) {
                String config = this.inetAddress + ":" + this.port;
                if (this.retry && this.reconnector == null) {
                    this.reconnector = this.createReconnector();
                    try {
                        this.reconnector.reconnect();
                    } catch (IOException reconnEx) {
                        LOGGER.debug("Cannot reestablish socket connection to {}: {}; starting reconnector thread {}", (Object)config, (Object)reconnEx.getLocalizedMessage(), (Object)this.reconnector.getName(), (Object)reconnEx);
                        this.reconnector.start();
                        throw new AppenderLoggingException(String.format("Error sending to %s for %s", this.getName(), config), causeEx);
                    }
                    try {
                        this.writeAndFlush(bytes, offset, length, immediateFlush);
                    } catch (IOException e) {
                        throw new AppenderLoggingException(String.format("Error writing to %s after reestablishing connection for %s", this.getName(), config), causeEx);
                    }
                    return;
                }
                String message = String.format("Error writing to %s for connection %s", this.getName(), config);
                throw new AppenderLoggingException(message, causeEx);
            }
        }
    }

    private void writeAndFlush(byte[] bytes, int offset, int length, boolean immediateFlush) throws IOException {
        OutputStream outputStream = this.getOutputStream();
        outputStream.write(bytes, offset, length);
        if (immediateFlush) {
            outputStream.flush();
        }
    }

    @Override
    protected synchronized boolean closeOutputStream() {
        boolean closed = super.closeOutputStream();
        if (this.reconnector != null) {
            this.reconnector.shutdown();
            this.reconnector.interrupt();
            this.reconnector = null;
        }
        Socket oldSocket = this.socket;
        this.socket = null;
        if (oldSocket != null) {
            try {
                oldSocket.close();
            } catch (IOException e) {
                LOGGER.error("Could not close socket {}", (Object)this.socket);
                return false;
            }
        }
        return closed;
    }

    public int getConnectTimeoutMillis() {
        return this.connectTimeoutMillis;
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("protocol", "tcp");
        result.put("direction", "out");
        return result;
    }

    private Reconnector createReconnector() {
        Reconnector recon = new Reconnector(this);
        recon.setDaemon(true);
        recon.setPriority(1);
        return recon;
    }

    protected Socket createSocket(InetSocketAddress socketAddress) throws IOException {
        return TcpSocketManager.createSocket(socketAddress, this.socketOptions, this.connectTimeoutMillis);
    }

    protected static Socket createSocket(InetSocketAddress socketAddress, SocketOptions socketOptions, int connectTimeoutMillis) throws IOException {
        LOGGER.debug("Creating socket {}", (Object)socketAddress.toString());
        Socket newSocket = new Socket();
        if (socketOptions != null) {
            socketOptions.apply(newSocket);
        }
        newSocket.connect(socketAddress, connectTimeoutMillis);
        if (socketOptions != null) {
            socketOptions.apply(newSocket);
        }
        return newSocket;
    }

    public static void setHostResolver(HostResolver resolver) {
        TcpSocketManagerFactory.RESOLVER = resolver;
    }

    public SocketOptions getSocketOptions() {
        return this.socketOptions;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public int getReconnectionDelayMillis() {
        return this.reconnectionDelayMillis;
    }

    public String toString() {
        return "TcpSocketManager [reconnectionDelayMillis=" + this.reconnectionDelayMillis + ", reconnector=" + this.reconnector + ", socket=" + this.socket + ", socketOptions=" + this.socketOptions + ", retry=" + this.retry + ", immediateFail=" + this.immediateFail + ", connectTimeoutMillis=" + this.connectTimeoutMillis + ", inetAddress=" + this.inetAddress + ", host=" + this.host + ", port=" + this.port + ", layout=" + this.layout + ", byteBuffer=" + this.byteBuffer + ", count=" + this.count + "]";
    }

    public static class HostResolver {
        public static final HostResolver INSTANCE = new HostResolver();

        public List<InetSocketAddress> resolveHost(String host, int port) throws UnknownHostException {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            ArrayList<InetSocketAddress> socketAddresses = new ArrayList<InetSocketAddress>(addresses.length);
            for (InetAddress address : addresses) {
                socketAddresses.add(new InetSocketAddress(address, port));
            }
            return socketAddresses;
        }
    }

    protected static class TcpSocketManagerFactory<M extends TcpSocketManager, T extends FactoryData>
    implements ManagerFactory<M, T> {
        static volatile HostResolver RESOLVER = HostResolver.INSTANCE;

        protected TcpSocketManagerFactory() {
        }

        @Override
        public M createManager(String name, T data) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(((FactoryData)data).host);
            } catch (UnknownHostException ex) {
                LOGGER.error("Could not find address of {}: {}", (Object)((FactoryData)data).host, (Object)ex, (Object)ex);
                return null;
            }
            Socket socket = null;
            try {
                socket = this.createSocket(data);
                OutputStream os = socket.getOutputStream();
                return this.createManager(name, os, socket, inetAddress, data);
            } catch (IOException ex) {
                LOGGER.error("TcpSocketManager ({}) caught exception and will continue:", (Object)name, (Object)ex);
                NullOutputStream os = NullOutputStream.getInstance();
                if (((FactoryData)data).reconnectDelayMillis == 0) {
                    Closer.closeSilently(socket);
                    return null;
                }
                return this.createManager(name, os, null, inetAddress, data);
            }
        }

        M createManager(String name, OutputStream os, Socket socket, InetAddress inetAddress, T data) {
            return (M)new TcpSocketManager(name, os, socket, inetAddress, ((FactoryData)data).host, ((FactoryData)data).port, ((FactoryData)data).connectTimeoutMillis, ((FactoryData)data).reconnectDelayMillis, ((FactoryData)data).immediateFail, ((FactoryData)data).layout, ((FactoryData)data).bufferSize, ((FactoryData)data).socketOptions);
        }

        Socket createSocket(T data) throws IOException {
            List<InetSocketAddress> socketAddresses = RESOLVER.resolveHost(((FactoryData)data).host, ((FactoryData)data).port);
            IOException ioe = null;
            for (InetSocketAddress socketAddress : socketAddresses) {
                try {
                    return TcpSocketManager.createSocket(socketAddress, ((FactoryData)data).socketOptions, ((FactoryData)data).connectTimeoutMillis);
                } catch (IOException ex) {
                    ioe = ex;
                }
            }
            throw new IOException(this.errorMessage(data, socketAddresses), ioe);
        }

        protected String errorMessage(T data, List<InetSocketAddress> socketAddresses) {
            StringBuilder sb = new StringBuilder("Unable to create socket for ");
            sb.append(((FactoryData)data).host).append(" at port ").append(((FactoryData)data).port);
            if (socketAddresses.size() == 1) {
                if (!socketAddresses.get(0).getAddress().getHostAddress().equals(((FactoryData)data).host)) {
                    sb.append(" using ip address ").append(socketAddresses.get(0).getAddress().getHostAddress());
                    sb.append(" and port ").append(socketAddresses.get(0).getPort());
                }
            } else {
                sb.append(" using ip addresses and ports ");
                for (int i = 0; i < socketAddresses.size(); ++i) {
                    if (i <= 0) continue;
                    sb.append(", ");
                    sb.append(socketAddresses.get(i).getAddress().getHostAddress());
                    sb.append(":").append(socketAddresses.get(i).getPort());
                }
            }
            return sb.toString();
        }
    }

    static class FactoryData {
        protected final String host;
        protected final int port;
        protected final int connectTimeoutMillis;
        protected final int reconnectDelayMillis;
        protected final boolean immediateFail;
        protected final Layout<? extends Serializable> layout;
        protected final int bufferSize;
        protected final SocketOptions socketOptions;

        public FactoryData(String host, int port, int connectTimeoutMillis, int reconnectDelayMillis, boolean immediateFail, Layout<? extends Serializable> layout, int bufferSize, SocketOptions socketOptions) {
            this.host = host;
            this.port = port;
            this.connectTimeoutMillis = connectTimeoutMillis;
            this.reconnectDelayMillis = reconnectDelayMillis;
            this.immediateFail = immediateFail;
            this.layout = layout;
            this.bufferSize = bufferSize;
            this.socketOptions = socketOptions;
        }

        public String toString() {
            return "FactoryData [host=" + this.host + ", port=" + this.port + ", connectTimeoutMillis=" + this.connectTimeoutMillis + ", reconnectDelayMillis=" + this.reconnectDelayMillis + ", immediateFail=" + this.immediateFail + ", layout=" + this.layout + ", bufferSize=" + this.bufferSize + ", socketOptions=" + this.socketOptions + "]";
        }
    }

    private class Reconnector
    extends Log4jThread {
        private final CountDownLatch latch;
        private boolean shutdown;
        private final Object owner;

        public Reconnector(OutputStreamManager owner) {
            super("TcpSocketManager-Reconnector");
            this.latch = new CountDownLatch(1);
            this.shutdown = false;
            this.owner = owner;
        }

        public void latch() {
            try {
                this.latch.await();
            } catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }

        public void shutdown() {
            this.shutdown = true;
        }

        @Override
        public void run() {
            while (!this.shutdown) {
                try {
                    Reconnector.sleep(TcpSocketManager.this.reconnectionDelayMillis);
                    this.reconnect();
                } catch (InterruptedException ie) {
                    LOGGER.debug("Reconnection interrupted.");
                } catch (ConnectException ex) {
                    LOGGER.debug("{}:{} refused connection", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port);
                } catch (IOException ioe) {
                    LOGGER.debug("Unable to reconnect to {}:{}", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port);
                } finally {
                    this.latch.countDown();
                }
            }
        }

        void reconnect() throws IOException {
            List<InetSocketAddress> socketAddresses = TcpSocketManagerFactory.RESOLVER.resolveHost(TcpSocketManager.this.host, TcpSocketManager.this.port);
            if (socketAddresses.size() != 1) {
                IOException ioe = null;
                for (InetSocketAddress socketAddress : socketAddresses) {
                    try {
                        LOGGER.debug("Reconnecting " + socketAddress);
                        this.connect(socketAddress);
                        return;
                    } catch (IOException ex) {
                        ioe = ex;
                    }
                }
                throw ioe;
            }
            LOGGER.debug("Reconnecting " + socketAddresses.get(0));
            this.connect(socketAddresses.get(0));
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void connect(InetSocketAddress socketAddress) throws IOException {
            Socket sock = TcpSocketManager.this.createSocket(socketAddress);
            OutputStream newOS = sock.getOutputStream();
            InetAddress prev = TcpSocketManager.this.socket != null ? TcpSocketManager.this.socket.getInetAddress() : null;
            Object object = this.owner;
            synchronized (object) {
                Closer.closeSilently(TcpSocketManager.this.getOutputStream());
                TcpSocketManager.this.setOutputStream(newOS);
                TcpSocketManager.this.socket = sock;
                TcpSocketManager.this.reconnector = null;
                this.shutdown = true;
            }
            String type = prev != null && prev.getHostAddress().equals(socketAddress.getAddress().getHostAddress()) ? "reestablished" : "established";
            LOGGER.debug("Connection to {}:{} {}: {}", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port, (Object)type, (Object)TcpSocketManager.this.socket);
        }

        @Override
        public String toString() {
            return "Reconnector [latch=" + this.latch + ", shutdown=" + this.shutdown + "]";
        }
    }
}

