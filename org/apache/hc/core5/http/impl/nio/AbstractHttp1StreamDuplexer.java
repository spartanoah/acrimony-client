/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLSession;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicEndpointDetails;
import org.apache.hc.core5.http.impl.BasicHttpConnectionMetrics;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.CharCodingSupport;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.IncomingEntityDetails;
import org.apache.hc.core5.http.impl.nio.ChunkEncoder;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.impl.nio.SessionInputBufferImpl;
import org.apache.hc.core5.http.impl.nio.SessionOutputBufferImpl;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.ContentDecoder;
import org.apache.hc.core5.http.nio.ContentEncoder;
import org.apache.hc.core5.http.nio.NHttpMessageParser;
import org.apache.hc.core5.http.nio.NHttpMessageWriter;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.http.nio.command.CommandSupport;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.SocketTimeoutExceptionFactory;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.Timeout;

abstract class AbstractHttp1StreamDuplexer<IncomingMessage extends HttpMessage, OutgoingMessage extends HttpMessage>
implements Identifiable,
HttpConnection {
    private final ProtocolIOSession ioSession;
    private final Http1Config http1Config;
    private final SessionInputBufferImpl inbuf;
    private final SessionOutputBufferImpl outbuf;
    private final BasicHttpTransportMetrics inTransportMetrics;
    private final BasicHttpTransportMetrics outTransportMetrics;
    private final BasicHttpConnectionMetrics connMetrics;
    private final NHttpMessageParser<IncomingMessage> incomingMessageParser;
    private final NHttpMessageWriter<OutgoingMessage> outgoingMessageWriter;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final ByteBuffer contentBuffer;
    private final AtomicInteger outputRequests;
    private volatile Message<IncomingMessage, ContentDecoder> incomingMessage;
    private volatile Message<OutgoingMessage, ContentEncoder> outgoingMessage;
    private volatile ConnectionState connState;
    private volatile CapacityWindow capacityWindow;
    private volatile ProtocolVersion version;
    private volatile EndpointDetails endpointDetails;

    AbstractHttp1StreamDuplexer(ProtocolIOSession ioSession, Http1Config http1Config, CharCodingConfig charCodingConfig, NHttpMessageParser<IncomingMessage> incomingMessageParser, NHttpMessageWriter<OutgoingMessage> outgoingMessageWriter, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy) {
        this.ioSession = Args.notNull(ioSession, "I/O session");
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        int bufferSize = this.http1Config.getBufferSize();
        this.inbuf = new SessionInputBufferImpl(bufferSize, bufferSize < 512 ? bufferSize : 512, this.http1Config.getMaxLineLength(), CharCodingSupport.createDecoder(charCodingConfig));
        this.outbuf = new SessionOutputBufferImpl(bufferSize, bufferSize < 512 ? bufferSize : 512, CharCodingSupport.createEncoder(charCodingConfig));
        this.inTransportMetrics = new BasicHttpTransportMetrics();
        this.outTransportMetrics = new BasicHttpTransportMetrics();
        this.connMetrics = new BasicHttpConnectionMetrics(this.inTransportMetrics, this.outTransportMetrics);
        this.incomingMessageParser = incomingMessageParser;
        this.outgoingMessageWriter = outgoingMessageWriter;
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.contentBuffer = ByteBuffer.allocate(this.http1Config.getBufferSize());
        this.outputRequests = new AtomicInteger(0);
        this.connState = ConnectionState.READY;
    }

    @Override
    public String getId() {
        return this.ioSession.getId();
    }

    void shutdownSession(CloseMode closeMode) {
        if (closeMode == CloseMode.GRACEFUL) {
            this.connState = ConnectionState.GRACEFUL_SHUTDOWN;
            this.ioSession.enqueue(ShutdownCommand.GRACEFUL, Command.Priority.NORMAL);
        } else {
            this.connState = ConnectionState.SHUTDOWN;
            this.ioSession.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void shutdownSession(Exception exception) {
        this.connState = ConnectionState.SHUTDOWN;
        try {
            this.terminate(exception);
        } finally {
            this.ioSession.close();
        }
    }

    abstract void disconnected();

    abstract void terminate(Exception var1);

    abstract void updateInputMetrics(IncomingMessage var1, BasicHttpConnectionMetrics var2);

    abstract void updateOutputMetrics(OutgoingMessage var1, BasicHttpConnectionMetrics var2);

    abstract void consumeHeader(IncomingMessage var1, EntityDetails var2) throws HttpException, IOException;

    abstract boolean handleIncomingMessage(IncomingMessage var1) throws HttpException;

    abstract boolean handleOutgoingMessage(OutgoingMessage var1) throws HttpException;

    abstract ContentDecoder createContentDecoder(long var1, ReadableByteChannel var3, SessionInputBuffer var4, BasicHttpTransportMetrics var5) throws HttpException;

    abstract ContentEncoder createContentEncoder(long var1, WritableByteChannel var3, SessionOutputBuffer var4, BasicHttpTransportMetrics var5) throws HttpException;

    abstract void consumeData(ByteBuffer var1) throws HttpException, IOException;

    abstract void updateCapacity(CapacityChannel var1) throws HttpException, IOException;

    abstract void dataEnd(List<? extends Header> var1) throws HttpException, IOException;

    abstract boolean isOutputReady();

    abstract void produceOutput() throws HttpException, IOException;

    abstract void execute(RequestExecutionCommand var1) throws HttpException, IOException;

    abstract void inputEnd() throws HttpException, IOException;

    abstract void outputEnd() throws HttpException, IOException;

    abstract boolean inputIdle();

    abstract boolean outputIdle();

    abstract boolean handleTimeout();

    private void processCommands() throws HttpException, IOException {
        Command command;
        block3: {
            while (true) {
                if ((command = this.ioSession.poll()) == null) {
                    return;
                }
                if (command instanceof ShutdownCommand) {
                    ShutdownCommand shutdownCommand = (ShutdownCommand)command;
                    this.requestShutdown(shutdownCommand.getType());
                    continue;
                }
                if (!(command instanceof RequestExecutionCommand)) break block3;
                if (this.connState.compareTo(ConnectionState.GRACEFUL_SHUTDOWN) < 0) break;
                command.cancel();
            }
            this.execute((RequestExecutionCommand)command);
            return;
        }
        throw new HttpException("Unexpected command: " + command.getClass());
    }

    public final void onConnect() throws HttpException, IOException {
        this.connState = ConnectionState.ACTIVE;
        this.processCommands();
    }

    IncomingMessage parseMessageHead(boolean endOfStream) throws IOException, HttpException {
        HttpMessage messageHead = (HttpMessage)this.incomingMessageParser.parse(this.inbuf, endOfStream);
        if (messageHead != null) {
            this.incomingMessageParser.reset();
        }
        return (IncomingMessage)messageHead;
    }

    public final void onInput(ByteBuffer src) throws HttpException, IOException {
        if (src != null) {
            this.inbuf.put(src);
        }
        if (this.connState.compareTo(ConnectionState.GRACEFUL_SHUTDOWN) >= 0 && this.inbuf.hasData() && this.inputIdle()) {
            this.ioSession.clearEvent(1);
            return;
        }
        boolean endOfStream = false;
        if (this.incomingMessage == null) {
            int bytesRead = this.inbuf.fill(this.ioSession);
            if (bytesRead > 0) {
                this.inTransportMetrics.incrementBytesTransferred(bytesRead);
            }
            boolean bl = endOfStream = bytesRead == -1;
        }
        do {
            if (this.incomingMessage == null) {
                ContentDecoder contentDecoder;
                IncomingMessage messageHead = this.parseMessageHead(endOfStream);
                if (messageHead == null) break;
                this.version = messageHead.getVersion();
                this.updateInputMetrics(messageHead, this.connMetrics);
                if (this.handleIncomingMessage(messageHead)) {
                    long len = this.incomingContentStrategy.determineLength((HttpMessage)messageHead);
                    contentDecoder = this.createContentDecoder(len, this.ioSession, this.inbuf, this.inTransportMetrics);
                    this.consumeHeader(messageHead, contentDecoder != null ? new IncomingEntityDetails((MessageHeaders)messageHead, len) : null);
                } else {
                    this.consumeHeader(messageHead, null);
                    contentDecoder = null;
                }
                this.capacityWindow = new CapacityWindow(this.http1Config.getInitialWindowSize(), this.ioSession);
                if (contentDecoder != null) {
                    this.incomingMessage = new Message<IncomingMessage, ContentDecoder>(messageHead, contentDecoder);
                } else {
                    this.inputEnd();
                    if (this.connState.compareTo(ConnectionState.ACTIVE) == 0) {
                        this.ioSession.setEvent(1);
                    }
                }
            }
            if (this.incomingMessage == null) continue;
            ContentDecoder contentDecoder = this.incomingMessage.getBody();
            int bytesRead = contentDecoder.read(this.contentBuffer);
            if (bytesRead > 0) {
                this.contentBuffer.flip();
                this.consumeData(this.contentBuffer);
                this.contentBuffer.clear();
                int capacity = this.capacityWindow.removeCapacity(bytesRead);
                if (capacity <= 0 && !contentDecoder.isCompleted()) {
                    this.updateCapacity(this.capacityWindow);
                }
            }
            if (contentDecoder.isCompleted()) {
                this.dataEnd(contentDecoder.getTrailers());
                this.capacityWindow.close();
                this.incomingMessage = null;
                this.ioSession.setEvent(1);
                this.inputEnd();
            }
            if (bytesRead == 0) break;
        } while (this.inbuf.hasData());
        if (endOfStream && !this.inbuf.hasData()) {
            if (this.outputIdle() && this.inputIdle()) {
                this.requestShutdown(CloseMode.GRACEFUL);
            } else {
                this.shutdownSession(new ConnectionClosedException("Connection closed by peer"));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void onOutput() throws IOException, HttpException {
        this.ioSession.getLock().lock();
        try {
            int bytesWritten;
            if (this.outbuf.hasData() && (bytesWritten = this.outbuf.flush(this.ioSession)) > 0) {
                this.outTransportMetrics.incrementBytesTransferred(bytesWritten);
            }
        } finally {
            this.ioSession.getLock().unlock();
        }
        if (this.connState.compareTo(ConnectionState.SHUTDOWN) < 0) {
            boolean outputEnd;
            this.produceOutput();
            int pendingOutputRequests = this.outputRequests.get();
            boolean outputPending = this.isOutputReady();
            this.ioSession.getLock().lock();
            try {
                if (!outputPending && !this.outbuf.hasData() && this.outputRequests.compareAndSet(pendingOutputRequests, 0)) {
                    this.ioSession.clearEvent(4);
                } else {
                    this.outputRequests.addAndGet(-pendingOutputRequests);
                }
                outputEnd = this.outgoingMessage == null && !this.outbuf.hasData();
            } finally {
                this.ioSession.getLock().unlock();
            }
            if (outputEnd) {
                this.outputEnd();
                if (this.connState.compareTo(ConnectionState.ACTIVE) == 0) {
                    this.processCommands();
                } else if (this.connState.compareTo(ConnectionState.GRACEFUL_SHUTDOWN) >= 0 && this.inputIdle() && this.outputIdle()) {
                    this.connState = ConnectionState.SHUTDOWN;
                }
            }
        }
        if (this.connState.compareTo(ConnectionState.SHUTDOWN) >= 0) {
            this.ioSession.close();
        }
    }

    public final void onTimeout(Timeout timeout) throws IOException, HttpException {
        if (!this.handleTimeout()) {
            this.onException(SocketTimeoutExceptionFactory.create(timeout));
        }
    }

    public final void onException(Exception ex) {
        this.shutdownSession(ex);
        CommandSupport.failCommands(this.ioSession, ex);
    }

    public final void onDisconnect() {
        this.disconnected();
        CommandSupport.cancelCommands(this.ioSession);
    }

    void requestShutdown(CloseMode closeMode) {
        switch (closeMode) {
            case GRACEFUL: {
                if (this.connState != ConnectionState.ACTIVE) break;
                this.connState = ConnectionState.GRACEFUL_SHUTDOWN;
                break;
            }
            case IMMEDIATE: {
                this.connState = ConnectionState.SHUTDOWN;
            }
        }
        this.ioSession.setEvent(4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void commitMessageHead(OutgoingMessage messageHead, boolean endStream, FlushMode flushMode) throws HttpException, IOException {
        this.ioSession.getLock().lock();
        try {
            this.outgoingMessageWriter.write(messageHead, this.outbuf);
            this.updateOutputMetrics(messageHead, this.connMetrics);
            if (!endStream) {
                ContentEncoder contentEncoder;
                if (this.handleOutgoingMessage(messageHead)) {
                    long len = this.outgoingContentStrategy.determineLength((HttpMessage)messageHead);
                    contentEncoder = this.createContentEncoder(len, this.ioSession, this.outbuf, this.outTransportMetrics);
                } else {
                    contentEncoder = null;
                }
                if (contentEncoder != null) {
                    this.outgoingMessage = new Message<OutgoingMessage, Object>(messageHead, contentEncoder);
                }
            }
            this.outgoingMessageWriter.reset();
            if (flushMode == FlushMode.IMMEDIATE) {
                this.outbuf.flush(this.ioSession);
            }
            this.ioSession.setEvent(4);
        } finally {
            this.ioSession.getLock().unlock();
        }
    }

    void requestSessionInput() {
        this.ioSession.setEvent(1);
    }

    void requestSessionOutput() {
        this.outputRequests.incrementAndGet();
        this.ioSession.setEvent(4);
    }

    Timeout getSessionTimeout() {
        return this.ioSession.getSocketTimeout();
    }

    void setSessionTimeout(Timeout timeout) {
        this.ioSession.setSocketTimeout(timeout);
    }

    void suspendSessionInput() {
        this.ioSession.clearEvent(1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void suspendSessionOutput() throws IOException {
        this.ioSession.getLock().lock();
        try {
            if (this.outbuf.hasData()) {
                this.outbuf.flush(this.ioSession);
            } else {
                this.ioSession.clearEvent(4);
            }
        } finally {
            this.ioSession.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int streamOutput(ByteBuffer src) throws IOException {
        this.ioSession.getLock().lock();
        try {
            if (this.outgoingMessage == null) {
                throw new ClosedChannelException();
            }
            ContentEncoder contentEncoder = this.outgoingMessage.getBody();
            int bytesWritten = contentEncoder.write(src);
            if (bytesWritten > 0) {
                this.ioSession.setEvent(4);
            }
            int n = bytesWritten;
            return n;
        } finally {
            this.ioSession.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    MessageDelineation endOutputStream(List<? extends Header> trailers) throws IOException {
        this.ioSession.getLock().lock();
        try {
            if (this.outgoingMessage == null) {
                MessageDelineation messageDelineation = MessageDelineation.NONE;
                return messageDelineation;
            }
            ContentEncoder contentEncoder = this.outgoingMessage.getBody();
            contentEncoder.complete(trailers);
            this.ioSession.setEvent(4);
            this.outgoingMessage = null;
            MessageDelineation messageDelineation = contentEncoder instanceof ChunkEncoder ? MessageDelineation.CHUNK_CODED : MessageDelineation.MESSAGE_HEAD;
            return messageDelineation;
        } finally {
            this.ioSession.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean isOutputCompleted() {
        this.ioSession.getLock().lock();
        try {
            if (this.outgoingMessage == null) {
                boolean bl = true;
                return bl;
            }
            ContentEncoder contentEncoder = this.outgoingMessage.getBody();
            boolean bl = contentEncoder.isCompleted();
            return bl;
        } finally {
            this.ioSession.getLock().unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.ioSession.enqueue(ShutdownCommand.GRACEFUL, Command.Priority.NORMAL);
    }

    @Override
    public void close(CloseMode closeMode) {
        this.ioSession.enqueue(new ShutdownCommand(closeMode), Command.Priority.IMMEDIATE);
    }

    @Override
    public boolean isOpen() {
        return this.connState == ConnectionState.ACTIVE;
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.ioSession.getSocketTimeout();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.ioSession.setSocketTimeout(timeout);
    }

    @Override
    public EndpointDetails getEndpointDetails() {
        if (this.endpointDetails == null) {
            this.endpointDetails = new BasicEndpointDetails(this.ioSession.getRemoteAddress(), this.ioSession.getLocalAddress(), this.connMetrics, this.ioSession.getSocketTimeout());
        }
        return this.endpointDetails;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.ioSession.getRemoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.ioSession.getLocalAddress();
    }

    @Override
    public SSLSession getSSLSession() {
        TlsDetails tlsDetails = this.ioSession.getTlsDetails();
        return tlsDetails != null ? tlsDetails.getSSLSession() : null;
    }

    void appendState(StringBuilder buf) {
        buf.append("connState=").append((Object)this.connState).append(", inbuf=").append(this.inbuf).append(", outbuf=").append(this.outbuf).append(", inputWindow=").append(this.capacityWindow != null ? this.capacityWindow.getWindow() : 0);
    }

    static class CapacityWindow
    implements CapacityChannel {
        private final IOSession ioSession;
        private final Object lock;
        private int window;
        private boolean closed;

        CapacityWindow(int window, IOSession ioSession) {
            this.window = window;
            this.ioSession = ioSession;
            this.lock = new Object();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void update(int increment) throws IOException {
            Object object = this.lock;
            synchronized (object) {
                if (this.closed) {
                    return;
                }
                if (increment > 0) {
                    this.updateWindow(increment);
                    this.ioSession.setEvent(1);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        int removeCapacity(int delta) {
            Object object = this.lock;
            synchronized (object) {
                this.updateWindow(-delta);
                if (this.window <= 0) {
                    this.ioSession.clearEvent(1);
                }
                return this.window;
            }
        }

        private void updateWindow(int delta) {
            int newValue = this.window + delta;
            if (((this.window ^ newValue) & (delta ^ newValue)) < 0) {
                newValue = delta < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
            this.window = newValue;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void close() {
            Object object = this.lock;
            synchronized (object) {
                this.closed = true;
            }
        }

        int getWindow() {
            return this.window;
        }
    }

    static enum MessageDelineation {
        NONE,
        CHUNK_CODED,
        MESSAGE_HEAD;

    }

    private static enum ConnectionState {
        READY,
        ACTIVE,
        GRACEFUL_SHUTDOWN,
        SHUTDOWN;

    }
}

