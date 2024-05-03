/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.impl.nio.Http1StreamChannel;
import org.apache.hc.core5.http.impl.nio.MessageState;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.ResourceHolder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.Timeout;

class ClientHttp1StreamHandler
implements ResourceHolder {
    private final Http1StreamChannel<HttpRequest> outputChannel;
    private final DataStreamChannel internalDataChannel;
    private final HttpProcessor httpProcessor;
    private final Http1Config http1Config;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final AsyncClientExchangeHandler exchangeHandler;
    private final HttpCoreContext context;
    private final AtomicBoolean requestCommitted;
    private final AtomicBoolean done;
    private volatile boolean keepAlive;
    private volatile Timeout timeout;
    private volatile HttpRequest committedRequest;
    private volatile MessageState requestState;
    private volatile MessageState responseState;

    ClientHttp1StreamHandler(final Http1StreamChannel<HttpRequest> outputChannel, HttpProcessor httpProcessor, Http1Config http1Config, ConnectionReuseStrategy connectionReuseStrategy, AsyncClientExchangeHandler exchangeHandler, HttpCoreContext context) {
        this.outputChannel = outputChannel;
        this.internalDataChannel = new DataStreamChannel(){

            @Override
            public void requestOutput() {
                outputChannel.requestOutput();
            }

            @Override
            public void endStream(List<? extends Header> trailers) throws IOException {
                outputChannel.complete(trailers);
                ClientHttp1StreamHandler.this.requestState = MessageState.COMPLETE;
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return outputChannel.write(src);
            }

            @Override
            public void endStream() throws IOException {
                this.endStream(null);
            }
        };
        this.httpProcessor = httpProcessor;
        this.http1Config = http1Config;
        this.connectionReuseStrategy = connectionReuseStrategy;
        this.exchangeHandler = exchangeHandler;
        this.context = context;
        this.requestCommitted = new AtomicBoolean(false);
        this.done = new AtomicBoolean(false);
        this.keepAlive = true;
        this.requestState = MessageState.IDLE;
        this.responseState = MessageState.HEADERS;
    }

    boolean isResponseFinal() {
        return this.responseState == MessageState.COMPLETE;
    }

    boolean isCompleted() {
        return this.requestState == MessageState.COMPLETE && this.responseState == MessageState.COMPLETE;
    }

    String getRequestMethod() {
        return this.committedRequest != null ? this.committedRequest.getMethod() : null;
    }

    boolean isOutputReady() {
        switch (this.requestState) {
            case IDLE: 
            case ACK: {
                return true;
            }
            case BODY: {
                return this.exchangeHandler.available() > 0;
            }
        }
        return false;
    }

    private void commitRequest(HttpRequest request, EntityDetails entityDetails) throws IOException, HttpException {
        if (this.requestCommitted.compareAndSet(false, true)) {
            boolean endStream;
            ProtocolVersion transportVersion = request.getVersion();
            if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
                throw new UnsupportedHttpVersionException(transportVersion);
            }
            this.context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
            this.context.setAttribute("http.request", request);
            this.httpProcessor.process(request, entityDetails, (HttpContext)this.context);
            boolean bl = endStream = entityDetails == null;
            if (endStream) {
                this.outputChannel.submit(request, endStream, FlushMode.IMMEDIATE);
                this.committedRequest = request;
                this.requestState = MessageState.COMPLETE;
            } else {
                Header h = request.getFirstHeader("Expect");
                boolean expectContinue = h != null && "100-continue".equalsIgnoreCase(h.getValue());
                this.outputChannel.submit(request, endStream, expectContinue ? FlushMode.IMMEDIATE : FlushMode.BUFFER);
                this.committedRequest = request;
                if (expectContinue) {
                    this.requestState = MessageState.ACK;
                    this.timeout = this.outputChannel.getSocketTimeout();
                    this.outputChannel.setSocketTimeout(this.http1Config.getWaitForContinueTimeout());
                } else {
                    this.requestState = MessageState.BODY;
                    this.exchangeHandler.produce(this.internalDataChannel);
                }
            }
        } else {
            throw new HttpException("Request already committed");
        }
    }

    void produceOutput() throws HttpException, IOException {
        switch (this.requestState) {
            case IDLE: {
                this.requestState = MessageState.HEADERS;
                this.exchangeHandler.produceRequest(new RequestChannel(){

                    @Override
                    public void sendRequest(HttpRequest request, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
                        ClientHttp1StreamHandler.this.commitRequest(request, entityDetails);
                    }
                }, this.context);
                break;
            }
            case ACK: {
                this.outputChannel.suspendOutput();
                break;
            }
            case BODY: {
                this.exchangeHandler.produce(this.internalDataChannel);
            }
        }
    }

    void consumeHeader(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
        if (this.done.get() || this.responseState != MessageState.HEADERS) {
            throw new ProtocolException("Unexpected message head");
        }
        ProtocolVersion transportVersion = response.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
            throw new UnsupportedHttpVersionException(transportVersion);
        }
        int status = response.getCode();
        if (status < 100) {
            throw new ProtocolException("Invalid response: " + new StatusLine(response));
        }
        if (status > 100 && status < 200) {
            this.exchangeHandler.consumeInformation(response, this.context);
        } else if (!this.connectionReuseStrategy.keepAlive(this.committedRequest, response, this.context)) {
            this.keepAlive = false;
        }
        if (this.requestState == MessageState.ACK && (status == 100 || status >= 200)) {
            this.outputChannel.setSocketTimeout(this.timeout);
            this.requestState = MessageState.BODY;
            if (status < 400) {
                this.exchangeHandler.produce(this.internalDataChannel);
            }
        }
        if (status < 200) {
            return;
        }
        if (this.requestState == MessageState.BODY && this.keepAlive && status >= 400) {
            this.requestState = MessageState.COMPLETE;
            if (!this.outputChannel.abortGracefully()) {
                this.keepAlive = false;
            }
        }
        this.context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        this.context.setAttribute("http.response", response);
        this.httpProcessor.process(response, entityDetails, (HttpContext)this.context);
        if (entityDetails == null && !this.keepAlive) {
            this.outputChannel.close();
        }
        this.exchangeHandler.consumeResponse(response, entityDetails, this.context);
        this.responseState = entityDetails == null ? MessageState.COMPLETE : MessageState.BODY;
    }

    void consumeData(ByteBuffer src) throws HttpException, IOException {
        if (this.done.get() || this.responseState != MessageState.BODY) {
            throw new ProtocolException("Unexpected message data");
        }
        this.exchangeHandler.consume(src);
    }

    void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.exchangeHandler.updateCapacity(capacityChannel);
    }

    void dataEnd(List<? extends Header> trailers) throws HttpException, IOException {
        if (this.done.get() || this.responseState != MessageState.BODY) {
            throw new ProtocolException("Unexpected message data");
        }
        if (!this.keepAlive) {
            this.outputChannel.close();
        }
        this.responseState = MessageState.COMPLETE;
        this.exchangeHandler.streamEnd(trailers);
    }

    boolean handleTimeout() {
        if (this.requestState == MessageState.ACK) {
            this.requestState = MessageState.BODY;
            this.outputChannel.setSocketTimeout(this.timeout);
            this.outputChannel.requestOutput();
            return true;
        }
        return false;
    }

    void failed(Exception cause) {
        if (!this.done.get()) {
            this.exchangeHandler.failed(cause);
        }
    }

    @Override
    public void releaseResources() {
        if (this.done.compareAndSet(false, true)) {
            this.responseState = MessageState.COMPLETE;
            this.requestState = MessageState.COMPLETE;
            this.exchangeHandler.releaseResources();
        }
    }

    void appendState(StringBuilder buf) {
        buf.append("requestState=").append((Object)this.requestState).append(", responseState=").append((Object)this.responseState).append(", responseCommitted=").append(this.requestCommitted).append(", keepAlive=").append(this.keepAlive).append(", done=").append(this.done);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        this.appendState(buf);
        buf.append("]");
        return buf.toString();
    }
}

