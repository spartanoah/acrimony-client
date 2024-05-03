/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.java_websocket.AbstractWebSocket;
import org.java_websocket.SocketChannelIOHelper;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketFactory;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketServerFactory;
import org.java_websocket.WrappedByteChannel;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.exceptions.WrappedIOException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.server.DefaultWebSocketServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebSocketServer
extends AbstractWebSocket
implements Runnable {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private final Collection<WebSocket> connections;
    private final InetSocketAddress address;
    private ServerSocketChannel server;
    private Selector selector;
    private List<Draft> drafts;
    private Thread selectorthread;
    private final AtomicBoolean isclosed = new AtomicBoolean(false);
    protected List<WebSocketWorker> decoders;
    private List<WebSocketImpl> iqueue;
    private BlockingQueue<ByteBuffer> buffers;
    private int queueinvokes = 0;
    private final AtomicInteger queuesize = new AtomicInteger(0);
    private WebSocketServerFactory wsf = new DefaultWebSocketServerFactory();
    private int maxPendingConnections = -1;

    public WebSocketServer() {
        this(new InetSocketAddress(80), AVAILABLE_PROCESSORS, null);
    }

    public WebSocketServer(InetSocketAddress address) {
        this(address, AVAILABLE_PROCESSORS, null);
    }

    public WebSocketServer(InetSocketAddress address, int decodercount) {
        this(address, decodercount, null);
    }

    public WebSocketServer(InetSocketAddress address, List<Draft> drafts) {
        this(address, AVAILABLE_PROCESSORS, drafts);
    }

    public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts) {
        this(address, decodercount, drafts, new HashSet<WebSocket>());
    }

    public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts, Collection<WebSocket> connectionscontainer) {
        if (address == null || decodercount < 1 || connectionscontainer == null) {
            throw new IllegalArgumentException("address and connectionscontainer must not be null and you need at least 1 decoder");
        }
        this.drafts = drafts == null ? Collections.emptyList() : drafts;
        this.address = address;
        this.connections = connectionscontainer;
        this.setTcpNoDelay(false);
        this.setReuseAddr(false);
        this.iqueue = new LinkedList<WebSocketImpl>();
        this.decoders = new ArrayList<WebSocketWorker>(decodercount);
        this.buffers = new LinkedBlockingQueue<ByteBuffer>();
        for (int i = 0; i < decodercount; ++i) {
            WebSocketWorker ex = new WebSocketWorker();
            this.decoders.add(ex);
        }
    }

    public void start() {
        if (this.selectorthread != null) {
            throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
        }
        new Thread(this).start();
    }

    public void stop(int timeout) throws InterruptedException {
        this.stop(timeout, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void stop(int timeout, String closeMessage) throws InterruptedException {
        ArrayList<WebSocket> socketsToClose;
        if (!this.isclosed.compareAndSet(false, true)) {
            return;
        }
        Object object = this.connections;
        synchronized (object) {
            socketsToClose = new ArrayList<WebSocket>(this.connections);
        }
        for (WebSocket ws : socketsToClose) {
            ws.close(1001, closeMessage);
        }
        this.wsf.close();
        object = this;
        synchronized (object) {
            if (this.selectorthread != null && this.selector != null) {
                this.selector.wakeup();
                this.selectorthread.join(timeout);
            }
        }
    }

    public void stop() throws InterruptedException {
        this.stop(0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Collection<WebSocket> getConnections() {
        Collection<WebSocket> collection = this.connections;
        synchronized (collection) {
            return Collections.unmodifiableCollection(new ArrayList<WebSocket>(this.connections));
        }
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        int port = this.getAddress().getPort();
        if (port == 0 && this.server != null) {
            port = this.server.socket().getLocalPort();
        }
        return port;
    }

    public List<Draft> getDraft() {
        return Collections.unmodifiableList(this.drafts);
    }

    public void setMaxPendingConnections(int numberOfConnections) {
        this.maxPendingConnections = numberOfConnections;
    }

    public int getMaxPendingConnections() {
        return this.maxPendingConnections;
    }

    /*
     * Exception decompiling
     */
    @Override
    public void run() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [6[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         *     at async.DecompilerRunnable.cfrDecompilation(DecompilerRunnable.java:350)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:311)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:26)
         *     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
         *     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
         *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
         *     at java.lang.Thread.run(Thread.java:750)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private void doAdditionalRead() throws InterruptedException, IOException {
        while (!this.iqueue.isEmpty()) {
            WebSocketImpl conn = this.iqueue.remove(0);
            WrappedByteChannel c = (WrappedByteChannel)conn.getChannel();
            ByteBuffer buf = this.takeBuffer();
            try {
                if (SocketChannelIOHelper.readMore(buf, conn, c)) {
                    this.iqueue.add(conn);
                }
                if (buf.hasRemaining()) {
                    conn.inQueue.put(buf);
                    this.queue(conn);
                    continue;
                }
                this.pushBuffer(buf);
            } catch (IOException e) {
                this.pushBuffer(buf);
                throw e;
            }
        }
    }

    private void doAccept(SelectionKey key, Iterator<SelectionKey> i) throws IOException, InterruptedException {
        if (!this.onConnect(key)) {
            key.cancel();
            return;
        }
        SocketChannel channel = this.server.accept();
        if (channel == null) {
            return;
        }
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        socket.setTcpNoDelay(this.isTcpNoDelay());
        socket.setKeepAlive(true);
        WebSocketImpl w = this.wsf.createWebSocket((WebSocketAdapter)this, this.drafts);
        w.setSelectionKey(channel.register(this.selector, 1, w));
        try {
            w.setChannel(this.wsf.wrapChannel(channel, w.getSelectionKey()));
            i.remove();
            this.allocateBuffers(w);
        } catch (IOException ex) {
            if (w.getSelectionKey() != null) {
                w.getSelectionKey().cancel();
            }
            this.handleIOException(w.getSelectionKey(), null, ex);
        }
    }

    private boolean doRead(SelectionKey key, Iterator<SelectionKey> i) throws InterruptedException, WrappedIOException {
        WebSocketImpl conn = (WebSocketImpl)key.attachment();
        ByteBuffer buf = this.takeBuffer();
        if (conn.getChannel() == null) {
            key.cancel();
            this.handleIOException(key, conn, new IOException());
            return false;
        }
        try {
            if (SocketChannelIOHelper.read(buf, conn, conn.getChannel())) {
                if (buf.hasRemaining()) {
                    conn.inQueue.put(buf);
                    this.queue(conn);
                    i.remove();
                    if (conn.getChannel() instanceof WrappedByteChannel && ((WrappedByteChannel)conn.getChannel()).isNeedRead()) {
                        this.iqueue.add(conn);
                    }
                } else {
                    this.pushBuffer(buf);
                }
            } else {
                this.pushBuffer(buf);
            }
        } catch (IOException e) {
            this.pushBuffer(buf);
            throw new WrappedIOException(conn, e);
        }
        return true;
    }

    private void doWrite(SelectionKey key) throws WrappedIOException {
        WebSocketImpl conn = (WebSocketImpl)key.attachment();
        try {
            if (SocketChannelIOHelper.batch(conn, conn.getChannel()) && key.isValid()) {
                key.interestOps(1);
            }
        } catch (IOException e) {
            throw new WrappedIOException(conn, e);
        }
    }

    private boolean doSetupSelectorAndServerThread() {
        this.selectorthread.setName("WebSocketSelector-" + this.selectorthread.getId());
        try {
            this.server = ServerSocketChannel.open();
            this.server.configureBlocking(false);
            ServerSocket socket = this.server.socket();
            socket.setReceiveBufferSize(16384);
            socket.setReuseAddress(this.isReuseAddr());
            socket.bind(this.address, this.getMaxPendingConnections());
            this.selector = Selector.open();
            this.server.register(this.selector, this.server.validOps());
            this.startConnectionLostTimer();
            for (WebSocketWorker ex : this.decoders) {
                ex.start();
            }
            this.onStart();
        } catch (IOException ex) {
            this.handleFatal(null, ex);
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean doEnsureSingleThread() {
        WebSocketServer webSocketServer = this;
        synchronized (webSocketServer) {
            if (this.selectorthread != null) {
                throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
            }
            this.selectorthread = Thread.currentThread();
            if (this.isclosed.get()) {
                return false;
            }
        }
        return true;
    }

    private void doServerShutdown() {
        this.stopConnectionLostTimer();
        if (this.decoders != null) {
            for (WebSocketWorker w : this.decoders) {
                w.interrupt();
            }
        }
        if (this.selector != null) {
            try {
                this.selector.close();
            } catch (IOException e) {
                this.log.error("IOException during selector.close", e);
                this.onError(null, e);
            }
        }
        if (this.server != null) {
            try {
                this.server.close();
            } catch (IOException e) {
                this.log.error("IOException during server.close", e);
                this.onError(null, e);
            }
        }
    }

    protected void allocateBuffers(WebSocket c) throws InterruptedException {
        if (this.queuesize.get() >= 2 * this.decoders.size() + 1) {
            return;
        }
        this.queuesize.incrementAndGet();
        this.buffers.put(this.createBuffer());
    }

    protected void releaseBuffers(WebSocket c) throws InterruptedException {
    }

    public ByteBuffer createBuffer() {
        return ByteBuffer.allocate(16384);
    }

    protected void queue(WebSocketImpl ws) throws InterruptedException {
        if (ws.getWorkerThread() == null) {
            ws.setWorkerThread(this.decoders.get(this.queueinvokes % this.decoders.size()));
            ++this.queueinvokes;
        }
        ws.getWorkerThread().put(ws);
    }

    private ByteBuffer takeBuffer() throws InterruptedException {
        return this.buffers.take();
    }

    private void pushBuffer(ByteBuffer buf) throws InterruptedException {
        if (this.buffers.size() > this.queuesize.intValue()) {
            return;
        }
        this.buffers.put(buf);
    }

    private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
        SelectableChannel channel;
        if (key != null) {
            key.cancel();
        }
        if (conn != null) {
            conn.closeConnection(1006, ex.getMessage());
        } else if (key != null && (channel = key.channel()) != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException iOException) {
                // empty catch block
            }
            this.log.trace("Connection closed because of exception", ex);
        }
    }

    private void handleFatal(WebSocket conn, Exception e) {
        this.log.error("Shutdown due to fatal error", e);
        this.onError(conn, e);
        String causeMessage = e.getCause() != null ? " caused by " + e.getCause().getClass().getName() : "";
        String errorMessage = "Got error on server side: " + e.getClass().getName() + causeMessage;
        try {
            this.stop(0, errorMessage);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            this.log.error("Interrupt during stop", e);
            this.onError(null, e1);
        }
        if (this.decoders != null) {
            for (WebSocketWorker w : this.decoders) {
                w.interrupt();
            }
        }
        if (this.selectorthread != null) {
            this.selectorthread.interrupt();
        }
    }

    @Override
    public final void onWebsocketMessage(WebSocket conn, String message) {
        this.onMessage(conn, message);
    }

    @Override
    public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
        this.onMessage(conn, blob);
    }

    @Override
    public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
        if (this.addConnection(conn)) {
            this.onOpen(conn, (ClientHandshake)handshake);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
        this.selector.wakeup();
        try {
            if (this.removeConnection(conn)) {
                this.onClose(conn, code, reason, remote);
            }
        } finally {
            try {
                this.releaseBuffers(conn);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean removeConnection(WebSocket ws) {
        boolean removed = false;
        Collection<WebSocket> collection = this.connections;
        synchronized (collection) {
            if (this.connections.contains(ws)) {
                removed = this.connections.remove(ws);
            } else {
                this.log.trace("Removing connection which is not in the connections collection! Possible no handshake received! {}", (Object)ws);
            }
        }
        if (this.isclosed.get() && this.connections.isEmpty()) {
            this.selectorthread.interrupt();
        }
        return removed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean addConnection(WebSocket ws) {
        if (!this.isclosed.get()) {
            Collection<WebSocket> collection = this.connections;
            synchronized (collection) {
                return this.connections.add(ws);
            }
        }
        ws.close(1001);
        return true;
    }

    @Override
    public final void onWebsocketError(WebSocket conn, Exception ex) {
        this.onError(conn, ex);
    }

    @Override
    public final void onWriteDemand(WebSocket w) {
        WebSocketImpl conn = (WebSocketImpl)w;
        try {
            conn.getSelectionKey().interestOps(5);
        } catch (CancelledKeyException e) {
            conn.outQueue.clear();
        }
        this.selector.wakeup();
    }

    @Override
    public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
        this.onCloseInitiated(conn, code, reason);
    }

    @Override
    public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        this.onClosing(conn, code, reason, remote);
    }

    public void onCloseInitiated(WebSocket conn, int code, String reason) {
    }

    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
    }

    public final void setWebSocketFactory(WebSocketServerFactory wsf) {
        if (this.wsf != null) {
            this.wsf.close();
        }
        this.wsf = wsf;
    }

    public final WebSocketFactory getWebSocketFactory() {
        return this.wsf;
    }

    protected boolean onConnect(SelectionKey key) {
        return true;
    }

    private Socket getSocket(WebSocket conn) {
        WebSocketImpl impl = (WebSocketImpl)conn;
        return ((SocketChannel)impl.getSelectionKey().channel()).socket();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return (InetSocketAddress)this.getSocket(conn).getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return (InetSocketAddress)this.getSocket(conn).getRemoteSocketAddress();
    }

    public abstract void onOpen(WebSocket var1, ClientHandshake var2);

    public abstract void onClose(WebSocket var1, int var2, String var3, boolean var4);

    public abstract void onMessage(WebSocket var1, String var2);

    public abstract void onError(WebSocket var1, Exception var2);

    public abstract void onStart();

    public void onMessage(WebSocket conn, ByteBuffer message) {
    }

    public void broadcast(String text) {
        this.broadcast(text, this.connections);
    }

    public void broadcast(byte[] data) {
        this.broadcast(data, this.connections);
    }

    public void broadcast(ByteBuffer data) {
        this.broadcast(data, this.connections);
    }

    public void broadcast(byte[] data, Collection<WebSocket> clients) {
        if (data == null || clients == null) {
            throw new IllegalArgumentException();
        }
        this.broadcast(ByteBuffer.wrap(data), clients);
    }

    public void broadcast(ByteBuffer data, Collection<WebSocket> clients) {
        if (data == null || clients == null) {
            throw new IllegalArgumentException();
        }
        this.doBroadcast(data, clients);
    }

    public void broadcast(String text, Collection<WebSocket> clients) {
        if (text == null || clients == null) {
            throw new IllegalArgumentException();
        }
        this.doBroadcast(text, clients);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doBroadcast(Object data, Collection<WebSocket> clients) {
        ArrayList<WebSocket> clientCopy;
        String strData = null;
        if (data instanceof String) {
            strData = (String)data;
        }
        ByteBuffer byteData = null;
        if (data instanceof ByteBuffer) {
            byteData = (ByteBuffer)data;
        }
        if (strData == null && byteData == null) {
            return;
        }
        HashMap<Draft, List<Framedata>> draftFrames = new HashMap<Draft, List<Framedata>>();
        Collection<WebSocket> collection = clients;
        synchronized (collection) {
            clientCopy = new ArrayList<WebSocket>(clients);
        }
        for (WebSocket client : clientCopy) {
            if (client == null) continue;
            Draft draft = client.getDraft();
            this.fillFrames(draft, draftFrames, strData, byteData);
            try {
                client.sendFrame((Collection)draftFrames.get(draft));
            } catch (WebsocketNotConnectedException websocketNotConnectedException) {}
        }
    }

    private void fillFrames(Draft draft, Map<Draft, List<Framedata>> draftFrames, String strData, ByteBuffer byteData) {
        if (!draftFrames.containsKey(draft)) {
            List<Framedata> frames = null;
            if (strData != null) {
                frames = draft.createFrames(strData, false);
            }
            if (byteData != null) {
                frames = draft.createFrames(byteData, false);
            }
            if (frames != null) {
                draftFrames.put(draft, frames);
            }
        }
    }

    public class WebSocketWorker
    extends Thread {
        private BlockingQueue<WebSocketImpl> iqueue = new LinkedBlockingQueue<WebSocketImpl>();

        public WebSocketWorker() {
            this.setName("WebSocketWorker-" + this.getId());
            this.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    WebSocketServer.this.log.error("Uncaught exception in thread {}: {}", (Object)t.getName(), (Object)e);
                }
            });
        }

        public void put(WebSocketImpl ws) throws InterruptedException {
            this.iqueue.put(ws);
        }

        @Override
        public void run() {
            block6: {
                WebSocketImpl ws = null;
                try {
                    while (true) {
                        ws = this.iqueue.take();
                        ByteBuffer buf = (ByteBuffer)ws.inQueue.poll();
                        assert (buf != null);
                        this.doDecode(ws, buf);
                        ws = null;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (LinkageError | ThreadDeath | VirtualMachineError e) {
                    WebSocketServer.this.log.error("Got fatal error in worker thread {}", (Object)this.getName());
                    Exception exception = new Exception(e);
                    WebSocketServer.this.handleFatal(ws, exception);
                } catch (Throwable e) {
                    WebSocketServer.this.log.error("Uncaught exception in thread {}: {}", (Object)this.getName(), (Object)e);
                    if (ws == null) break block6;
                    Exception exception = new Exception(e);
                    WebSocketServer.this.onWebsocketError(ws, exception);
                    ws.close();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void doDecode(WebSocketImpl ws, ByteBuffer buf) throws InterruptedException {
            try {
                ws.decode(buf);
            } catch (Exception e) {
                WebSocketServer.this.log.error("Error while reading from remote connection", e);
            } finally {
                WebSocketServer.this.pushBuffer(buf);
            }
        }
    }
}

