/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicHttpConnectionMetrics;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.nio.AbstractHttp1StreamDuplexer;
import org.apache.hc.core5.http.impl.nio.ChunkDecoder;
import org.apache.hc.core5.http.impl.nio.ChunkEncoder;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.impl.nio.Http1StreamChannel;
import org.apache.hc.core5.http.impl.nio.IdentityEncoder;
import org.apache.hc.core5.http.impl.nio.LengthDelimitedDecoder;
import org.apache.hc.core5.http.impl.nio.LengthDelimitedEncoder;
import org.apache.hc.core5.http.impl.nio.ServerHttp1StreamHandler;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.ContentDecoder;
import org.apache.hc.core5.http.nio.ContentEncoder;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.NHttpMessageParser;
import org.apache.hc.core5.http.nio.NHttpMessageWriter;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.Timeout;

@Internal
public class ServerHttp1StreamDuplexer
extends AbstractHttp1StreamDuplexer<HttpRequest, HttpResponse> {
    private final String scheme;
    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory;
    private final Http1Config http1Config;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final Http1StreamListener streamListener;
    private final Queue<ServerHttp1StreamHandler> pipeline;
    private final Http1StreamChannel<HttpResponse> outputChannel;
    private volatile ServerHttp1StreamHandler outgoing;
    private volatile ServerHttp1StreamHandler incoming;

    public ServerHttp1StreamDuplexer(ProtocolIOSession ioSession, HttpProcessor httpProcessor, HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory, String scheme, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParser<HttpRequest> incomingMessageParser, NHttpMessageWriter<HttpResponse> outgoingMessageWriter, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, final Http1StreamListener streamListener) {
        super(ioSession, http1Config, charCodingConfig, incomingMessageParser, outgoingMessageWriter, incomingContentStrategy, outgoingContentStrategy);
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.exchangeHandlerFactory = Args.notNull(exchangeHandlerFactory, "Exchange handler factory");
        this.scheme = scheme;
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.connectionReuseStrategy = connectionReuseStrategy != null ? connectionReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
        this.pipeline = new ConcurrentLinkedQueue<ServerHttp1StreamHandler>();
        this.outputChannel = new Http1StreamChannel<HttpResponse>(){

            @Override
            public void close() {
                ServerHttp1StreamDuplexer.this.close(CloseMode.GRACEFUL);
            }

            @Override
            public void submit(HttpResponse response, boolean endStream, FlushMode flushMode) throws HttpException, IOException {
                if (streamListener != null) {
                    streamListener.onResponseHead(ServerHttp1StreamDuplexer.this, response);
                }
                ServerHttp1StreamDuplexer.this.commitMessageHead(response, endStream, flushMode);
            }

            @Override
            public void requestOutput() {
                ServerHttp1StreamDuplexer.this.requestSessionOutput();
            }

            @Override
            public void suspendOutput() throws IOException {
                ServerHttp1StreamDuplexer.this.suspendSessionOutput();
            }

            @Override
            public Timeout getSocketTimeout() {
                return ServerHttp1StreamDuplexer.this.getSessionTimeout();
            }

            @Override
            public void setSocketTimeout(Timeout timeout) {
                ServerHttp1StreamDuplexer.this.setSessionTimeout(timeout);
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return ServerHttp1StreamDuplexer.this.streamOutput(src);
            }

            @Override
            public void complete(List<? extends Header> trailers) throws IOException {
                ServerHttp1StreamDuplexer.this.endOutputStream(trailers);
            }

            @Override
            public boolean isCompleted() {
                return ServerHttp1StreamDuplexer.this.isOutputCompleted();
            }

            @Override
            public boolean abortGracefully() throws IOException {
                AbstractHttp1StreamDuplexer.MessageDelineation messageDelineation = ServerHttp1StreamDuplexer.this.endOutputStream(null);
                return messageDelineation != AbstractHttp1StreamDuplexer.MessageDelineation.MESSAGE_HEAD;
            }

            @Override
            public void activate() throws HttpException, IOException {
            }

            public String toString() {
                return "Http1StreamChannel[" + ServerHttp1StreamDuplexer.this + "]";
            }
        };
    }

    @Override
    void terminate(Exception exception) {
        ServerHttp1StreamHandler handler;
        if (this.incoming != null) {
            this.incoming.failed(exception);
            this.incoming.releaseResources();
            this.incoming = null;
        }
        if (this.outgoing != null) {
            this.outgoing.failed(exception);
            this.outgoing.releaseResources();
            this.outgoing = null;
        }
        while ((handler = this.pipeline.poll()) != null) {
            handler.failed(exception);
            handler.releaseResources();
        }
    }

    @Override
    void disconnected() {
        ServerHttp1StreamHandler handler;
        if (this.incoming != null) {
            if (!this.incoming.isCompleted()) {
                this.incoming.failed(new ConnectionClosedException());
            }
            this.incoming.releaseResources();
            this.incoming = null;
        }
        if (this.outgoing != null) {
            if (!this.outgoing.isCompleted()) {
                this.outgoing.failed(new ConnectionClosedException());
            }
            this.outgoing.releaseResources();
            this.outgoing = null;
        }
        while ((handler = this.pipeline.poll()) != null) {
            handler.failed(new ConnectionClosedException());
            handler.releaseResources();
        }
    }

    @Override
    void updateInputMetrics(HttpRequest request, BasicHttpConnectionMetrics connMetrics) {
        connMetrics.incrementRequestCount();
    }

    @Override
    void updateOutputMetrics(HttpResponse response, BasicHttpConnectionMetrics connMetrics) {
        if (response.getCode() >= 200) {
            connMetrics.incrementRequestCount();
        }
    }

    @Override
    protected boolean handleIncomingMessage(HttpRequest request) throws HttpException {
        return true;
    }

    @Override
    protected ContentDecoder createContentDecoder(long len, ReadableByteChannel channel, SessionInputBuffer buffer, BasicHttpTransportMetrics metrics) throws HttpException {
        if (len >= 0L) {
            return new LengthDelimitedDecoder(channel, buffer, metrics, len);
        }
        if (len == -1L) {
            return new ChunkDecoder(channel, buffer, this.http1Config, metrics);
        }
        return null;
    }

    @Override
    protected boolean handleOutgoingMessage(HttpResponse response) throws HttpException {
        return true;
    }

    @Override
    protected ContentEncoder createContentEncoder(long len, WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics) throws HttpException {
        int chunkSizeHint;
        int n = chunkSizeHint = this.http1Config.getChunkSizeHint() >= 0 ? this.http1Config.getChunkSizeHint() : 2048;
        if (len >= 0L) {
            return new LengthDelimitedEncoder(channel, buffer, metrics, len, chunkSizeHint);
        }
        if (len == -1L) {
            return new ChunkEncoder(channel, buffer, metrics, chunkSizeHint);
        }
        return new IdentityEncoder(channel, buffer, metrics, chunkSizeHint);
    }

    @Override
    boolean inputIdle() {
        return this.incoming == null;
    }

    @Override
    boolean outputIdle() {
        return this.outgoing == null && this.pipeline.isEmpty();
    }

    @Override
    HttpRequest parseMessageHead(boolean endOfStream) throws IOException, HttpException {
        try {
            return (HttpRequest)super.parseMessageHead(endOfStream);
        } catch (HttpException ex) {
            this.terminateExchange(ex);
            return null;
        }
    }

    void terminateExchange(HttpException ex) throws HttpException, IOException {
        ServerHttp1StreamHandler streamHandler;
        this.suspendSessionInput();
        HttpCoreContext context = HttpCoreContext.create();
        context.setAttribute("http.ssl-session", this.getSSLSession());
        context.setAttribute("http.connection-endpoint", this.getEndpointDetails());
        if (this.outgoing == null) {
            this.outgoing = streamHandler = new ServerHttp1StreamHandler(this.outputChannel, this.httpProcessor, this.connectionReuseStrategy, this.exchangeHandlerFactory, context);
        } else {
            streamHandler = new ServerHttp1StreamHandler(new DelayedOutputChannel(this.outputChannel), this.httpProcessor, this.connectionReuseStrategy, this.exchangeHandlerFactory, context);
            this.pipeline.add(streamHandler);
        }
        streamHandler.terminateExchange(ex);
        this.incoming = null;
    }

    @Override
    void consumeHeader(HttpRequest request, EntityDetails entityDetails) throws HttpException, IOException {
        ServerHttp1StreamHandler streamHandler;
        if (this.streamListener != null) {
            this.streamListener.onRequestHead(this, request);
        }
        HttpCoreContext context = HttpCoreContext.create();
        context.setAttribute("http.ssl-session", this.getSSLSession());
        context.setAttribute("http.connection-endpoint", this.getEndpointDetails());
        if (this.outgoing == null) {
            this.outgoing = streamHandler = new ServerHttp1StreamHandler(this.outputChannel, this.httpProcessor, this.connectionReuseStrategy, this.exchangeHandlerFactory, context);
        } else {
            streamHandler = new ServerHttp1StreamHandler(new DelayedOutputChannel(this.outputChannel), this.httpProcessor, this.connectionReuseStrategy, this.exchangeHandlerFactory, context);
            this.pipeline.add(streamHandler);
        }
        request.setScheme(this.scheme);
        streamHandler.consumeHeader(request, entityDetails);
        this.incoming = streamHandler;
    }

    @Override
    void consumeData(ByteBuffer src) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Request stream handler");
        this.incoming.consumeData(src);
    }

    @Override
    void updateCapacity(CapacityChannel capacityChannel) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Request stream handler");
        this.incoming.updateCapacity(capacityChannel);
    }

    @Override
    void dataEnd(List<? extends Header> trailers) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Request stream handler");
        this.incoming.dataEnd(trailers);
    }

    @Override
    void inputEnd() throws HttpException, IOException {
        if (this.incoming != null) {
            if (this.incoming.isCompleted()) {
                this.incoming.releaseResources();
            }
            this.incoming = null;
        }
    }

    @Override
    void execute(RequestExecutionCommand executionCommand) throws HttpException {
        throw new HttpException("Illegal command: " + executionCommand.getClass());
    }

    @Override
    boolean isOutputReady() {
        return this.outgoing != null && this.outgoing.isOutputReady();
    }

    @Override
    void produceOutput() throws HttpException, IOException {
        if (this.outgoing != null) {
            this.outgoing.produceOutput();
        }
    }

    @Override
    void outputEnd() throws HttpException, IOException {
        ServerHttp1StreamHandler handler;
        if (this.outgoing != null && this.outgoing.isResponseFinal()) {
            if (this.streamListener != null) {
                this.streamListener.onExchangeComplete(this, this.outgoing.keepAlive());
            }
            if (this.outgoing.isCompleted()) {
                this.outgoing.releaseResources();
            }
            this.outgoing = null;
        }
        if (this.outgoing == null && this.isOpen() && (handler = this.pipeline.poll()) != null) {
            this.outgoing = handler;
            handler.activateChannel();
            if (handler.isOutputReady()) {
                handler.produceOutput();
            }
        }
    }

    @Override
    boolean handleTimeout() {
        return false;
    }

    @Override
    void appendState(StringBuilder buf) {
        super.appendState(buf);
        buf.append(", incoming=[");
        if (this.incoming != null) {
            this.incoming.appendState(buf);
        }
        buf.append("], outgoing=[");
        if (this.outgoing != null) {
            this.outgoing.appendState(buf);
        }
        buf.append("], pipeline=");
        buf.append(this.pipeline.size());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        this.appendState(buf);
        buf.append("]");
        return buf.toString();
    }

    private static class DelayedOutputChannel
    implements Http1StreamChannel<HttpResponse> {
        private final Http1StreamChannel<HttpResponse> channel;
        private volatile boolean direct;
        private volatile HttpResponse delayedResponse;
        private volatile boolean completed;

        private DelayedOutputChannel(Http1StreamChannel<HttpResponse> channel) {
            this.channel = channel;
        }

        @Override
        public void close() {
            this.channel.close();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void submit(HttpResponse response, boolean endStream, FlushMode flushMode) throws HttpException, IOException {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                if (this.direct) {
                    this.channel.submit(response, endStream, flushMode);
                } else {
                    this.delayedResponse = response;
                    this.completed = endStream;
                }
            }
        }

        @Override
        public void suspendOutput() throws IOException {
            this.channel.suspendOutput();
        }

        @Override
        public void requestOutput() {
            this.channel.requestOutput();
        }

        @Override
        public Timeout getSocketTimeout() {
            return this.channel.getSocketTimeout();
        }

        @Override
        public void setSocketTimeout(Timeout timeout) {
            this.channel.setSocketTimeout(timeout);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int write(ByteBuffer src) throws IOException {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                return this.direct ? this.channel.write(src) : 0;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void complete(List<? extends Header> trailers) throws IOException {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                if (this.direct) {
                    this.channel.complete(trailers);
                } else {
                    this.completed = true;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean abortGracefully() throws IOException {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                if (this.direct) {
                    return this.channel.abortGracefully();
                }
                this.completed = true;
                return true;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean isCompleted() {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                return this.direct ? this.channel.isCompleted() : this.completed;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void activate() throws IOException, HttpException {
            DelayedOutputChannel delayedOutputChannel = this;
            synchronized (delayedOutputChannel) {
                this.direct = true;
                if (this.delayedResponse != null) {
                    this.channel.submit(this.delayedResponse, this.completed, this.completed ? FlushMode.IMMEDIATE : FlushMode.BUFFER);
                    this.delayedResponse = null;
                }
            }
        }
    }
}

