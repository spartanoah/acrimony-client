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
import org.apache.hc.core5.http.LengthRequiredException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicHttpConnectionMetrics;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.nio.AbstractHttp1StreamDuplexer;
import org.apache.hc.core5.http.impl.nio.ChunkDecoder;
import org.apache.hc.core5.http.impl.nio.ChunkEncoder;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamHandler;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.impl.nio.Http1StreamChannel;
import org.apache.hc.core5.http.impl.nio.IdentityDecoder;
import org.apache.hc.core5.http.impl.nio.LengthDelimitedDecoder;
import org.apache.hc.core5.http.impl.nio.LengthDelimitedEncoder;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.ContentDecoder;
import org.apache.hc.core5.http.nio.ContentEncoder;
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
public class ClientHttp1StreamDuplexer
extends AbstractHttp1StreamDuplexer<HttpResponse, HttpRequest> {
    private final HttpProcessor httpProcessor;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final Http1Config http1Config;
    private final Http1StreamListener streamListener;
    private final Queue<ClientHttp1StreamHandler> pipeline;
    private final Http1StreamChannel<HttpRequest> outputChannel;
    private volatile ClientHttp1StreamHandler outgoing;
    private volatile ClientHttp1StreamHandler incoming;

    public ClientHttp1StreamDuplexer(ProtocolIOSession ioSession, HttpProcessor httpProcessor, Http1Config http1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy, NHttpMessageParser<HttpResponse> incomingMessageParser, NHttpMessageWriter<HttpRequest> outgoingMessageWriter, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, final Http1StreamListener streamListener) {
        super(ioSession, http1Config, charCodingConfig, incomingMessageParser, outgoingMessageWriter, incomingContentStrategy, outgoingContentStrategy);
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.connectionReuseStrategy = connectionReuseStrategy != null ? connectionReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
        this.pipeline = new ConcurrentLinkedQueue<ClientHttp1StreamHandler>();
        this.outputChannel = new Http1StreamChannel<HttpRequest>(){

            @Override
            public void close() {
                ClientHttp1StreamDuplexer.this.shutdownSession(CloseMode.IMMEDIATE);
            }

            @Override
            public void submit(HttpRequest request, boolean endStream, FlushMode flushMode) throws HttpException, IOException {
                if (streamListener != null) {
                    streamListener.onRequestHead(ClientHttp1StreamDuplexer.this, request);
                }
                ClientHttp1StreamDuplexer.this.commitMessageHead(request, endStream, flushMode);
            }

            @Override
            public void suspendOutput() throws IOException {
                ClientHttp1StreamDuplexer.this.suspendSessionOutput();
            }

            @Override
            public void requestOutput() {
                ClientHttp1StreamDuplexer.this.requestSessionOutput();
            }

            @Override
            public Timeout getSocketTimeout() {
                return ClientHttp1StreamDuplexer.this.getSessionTimeout();
            }

            @Override
            public void setSocketTimeout(Timeout timeout) {
                ClientHttp1StreamDuplexer.this.setSessionTimeout(timeout);
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return ClientHttp1StreamDuplexer.this.streamOutput(src);
            }

            @Override
            public void complete(List<? extends Header> trailers) throws IOException {
                ClientHttp1StreamDuplexer.this.endOutputStream(trailers);
            }

            @Override
            public boolean isCompleted() {
                return ClientHttp1StreamDuplexer.this.isOutputCompleted();
            }

            @Override
            public boolean abortGracefully() throws IOException {
                AbstractHttp1StreamDuplexer.MessageDelineation messageDelineation = ClientHttp1StreamDuplexer.this.endOutputStream(null);
                if (messageDelineation == AbstractHttp1StreamDuplexer.MessageDelineation.MESSAGE_HEAD) {
                    ClientHttp1StreamDuplexer.this.requestShutdown(CloseMode.GRACEFUL);
                    return false;
                }
                return true;
            }

            @Override
            public void activate() throws HttpException, IOException {
            }
        };
    }

    @Override
    void terminate(Exception exception) {
        ClientHttp1StreamHandler handler;
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
        ClientHttp1StreamHandler handler;
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
    void updateInputMetrics(HttpResponse response, BasicHttpConnectionMetrics connMetrics) {
        if (response.getCode() >= 200) {
            connMetrics.incrementRequestCount();
        }
    }

    @Override
    void updateOutputMetrics(HttpRequest request, BasicHttpConnectionMetrics connMetrics) {
        connMetrics.incrementRequestCount();
    }

    @Override
    protected boolean handleIncomingMessage(HttpResponse response) throws HttpException {
        if (this.incoming == null) {
            this.incoming = this.pipeline.poll();
        }
        if (this.incoming == null) {
            throw new HttpException("Unexpected response");
        }
        return MessageSupport.canResponseHaveBody(this.incoming.getRequestMethod(), response);
    }

    @Override
    protected ContentDecoder createContentDecoder(long len, ReadableByteChannel channel, SessionInputBuffer buffer, BasicHttpTransportMetrics metrics) throws HttpException {
        if (len >= 0L) {
            return new LengthDelimitedDecoder(channel, buffer, metrics, len);
        }
        if (len == -1L) {
            return new ChunkDecoder(channel, buffer, this.http1Config, metrics);
        }
        return new IdentityDecoder(channel, buffer, metrics);
    }

    @Override
    protected boolean handleOutgoingMessage(HttpRequest request) throws HttpException {
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
        throw new LengthRequiredException();
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
    void outputEnd() throws HttpException, IOException {
        if (this.outgoing != null) {
            if (this.outgoing.isCompleted()) {
                this.outgoing.releaseResources();
            }
            this.outgoing = null;
        }
    }

    @Override
    void execute(RequestExecutionCommand executionCommand) throws HttpException, IOException {
        AsyncClientExchangeHandler exchangeHandler = executionCommand.getExchangeHandler();
        HttpCoreContext context = HttpCoreContext.adapt(executionCommand.getContext());
        context.setAttribute("http.ssl-session", this.getSSLSession());
        context.setAttribute("http.connection-endpoint", this.getEndpointDetails());
        ClientHttp1StreamHandler handler = new ClientHttp1StreamHandler(this.outputChannel, this.httpProcessor, this.http1Config, this.connectionReuseStrategy, exchangeHandler, context);
        this.pipeline.add(handler);
        this.outgoing = handler;
        if (handler.isOutputReady()) {
            handler.produceOutput();
        }
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
    void consumeHeader(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
        if (this.streamListener != null) {
            this.streamListener.onResponseHead(this, response);
        }
        Asserts.notNull(this.incoming, "Response stream handler");
        this.incoming.consumeHeader(response, entityDetails);
    }

    @Override
    void consumeData(ByteBuffer src) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Response stream handler");
        this.incoming.consumeData(src);
    }

    @Override
    void updateCapacity(CapacityChannel capacityChannel) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Response stream handler");
        this.incoming.updateCapacity(capacityChannel);
    }

    @Override
    void dataEnd(List<? extends Header> trailers) throws HttpException, IOException {
        Asserts.notNull(this.incoming, "Response stream handler");
        this.incoming.dataEnd(trailers);
    }

    @Override
    void inputEnd() throws HttpException, IOException {
        if (this.incoming != null && this.incoming.isResponseFinal()) {
            if (this.streamListener != null) {
                this.streamListener.onExchangeComplete(this, this.isOpen());
            }
            if (this.incoming.isCompleted()) {
                this.incoming.releaseResources();
            }
            this.incoming = null;
        }
    }

    @Override
    boolean handleTimeout() {
        return this.outgoing != null && this.outgoing.handleTimeout();
    }

    @Override
    void appendState(StringBuilder buf) {
        super.appendState(buf);
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
}

