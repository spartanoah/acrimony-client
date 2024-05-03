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
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.MisdirectedRequestException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.impl.ServerSupport;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.impl.nio.Http1StreamChannel;
import org.apache.hc.core5.http.impl.nio.MessageState;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.ResourceHolder;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.nio.support.ImmediateResponseExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;

class ServerHttp1StreamHandler
implements ResourceHolder {
    private final Http1StreamChannel<HttpResponse> outputChannel;
    private final DataStreamChannel internalDataChannel;
    private final ResponseChannel responseChannel;
    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory;
    private final ConnectionReuseStrategy connectionReuseStrategy;
    private final HttpCoreContext context;
    private final AtomicBoolean responseCommitted;
    private final AtomicBoolean done;
    private volatile boolean keepAlive;
    private volatile AsyncServerExchangeHandler exchangeHandler;
    private volatile HttpRequest receivedRequest;
    private volatile MessageState requestState;
    private volatile MessageState responseState;

    ServerHttp1StreamHandler(final Http1StreamChannel<HttpResponse> outputChannel, HttpProcessor httpProcessor, ConnectionReuseStrategy connectionReuseStrategy, HandlerFactory<AsyncServerExchangeHandler> exchangeHandlerFactory, HttpCoreContext context) {
        this.outputChannel = outputChannel;
        this.internalDataChannel = new DataStreamChannel(){

            @Override
            public void requestOutput() {
                outputChannel.requestOutput();
            }

            @Override
            public void endStream(List<? extends Header> trailers) throws IOException {
                outputChannel.complete(trailers);
                if (!ServerHttp1StreamHandler.this.keepAlive) {
                    outputChannel.close();
                }
                ServerHttp1StreamHandler.this.responseState = MessageState.COMPLETE;
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
        this.responseChannel = new ResponseChannel(){

            @Override
            public void sendInformation(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
                ServerHttp1StreamHandler.this.commitInformation(response);
            }

            @Override
            public void sendResponse(HttpResponse response, EntityDetails responseEntityDetails, HttpContext httpContext) throws HttpException, IOException {
                ServerSupport.validateResponse(response, responseEntityDetails);
                ServerHttp1StreamHandler.this.commitResponse(response, responseEntityDetails);
            }

            @Override
            public void pushPromise(HttpRequest promise, AsyncPushProducer pushProducer, HttpContext httpContext) throws HttpException, IOException {
                ServerHttp1StreamHandler.this.commitPromise();
            }

            public String toString() {
                return super.toString() + " " + ServerHttp1StreamHandler.this;
            }
        };
        this.httpProcessor = httpProcessor;
        this.connectionReuseStrategy = connectionReuseStrategy;
        this.exchangeHandlerFactory = exchangeHandlerFactory;
        this.context = context;
        this.responseCommitted = new AtomicBoolean(false);
        this.done = new AtomicBoolean(false);
        this.keepAlive = true;
        this.requestState = MessageState.HEADERS;
        this.responseState = MessageState.IDLE;
    }

    private void commitResponse(HttpResponse response, EntityDetails responseEntityDetails) throws HttpException, IOException {
        if (this.responseCommitted.compareAndSet(false, true)) {
            boolean endStream;
            ProtocolVersion transportVersion = response.getVersion();
            if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
                throw new UnsupportedHttpVersionException(transportVersion);
            }
            int status = response.getCode();
            if (status < 200) {
                throw new HttpException("Invalid response: " + status);
            }
            this.context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
            this.context.setAttribute("http.response", response);
            this.httpProcessor.process(response, responseEntityDetails, (HttpContext)this.context);
            boolean bl = endStream = responseEntityDetails == null || this.receivedRequest != null && Method.HEAD.isSame(this.receivedRequest.getMethod());
            if (!this.connectionReuseStrategy.keepAlive(this.receivedRequest, response, this.context)) {
                this.keepAlive = false;
            }
            this.outputChannel.submit(response, endStream, endStream ? FlushMode.IMMEDIATE : FlushMode.BUFFER);
            if (endStream) {
                if (!this.keepAlive) {
                    this.outputChannel.close();
                }
                this.responseState = MessageState.COMPLETE;
            } else {
                this.responseState = MessageState.BODY;
                this.exchangeHandler.produce(this.internalDataChannel);
            }
        } else {
            throw new HttpException("Response already committed");
        }
    }

    private void commitInformation(HttpResponse response) throws IOException, HttpException {
        if (this.responseCommitted.get()) {
            throw new HttpException("Response already committed");
        }
        int status = response.getCode();
        if (status < 100 || status >= 200) {
            throw new HttpException("Invalid intermediate response: " + status);
        }
        this.outputChannel.submit(response, true, FlushMode.IMMEDIATE);
    }

    private void commitPromise() throws HttpException {
        throw new HttpException("HTTP/1.1 does not support server push");
    }

    void activateChannel() throws IOException, HttpException {
        this.outputChannel.activate();
    }

    boolean isResponseFinal() {
        return this.responseState == MessageState.COMPLETE;
    }

    boolean keepAlive() {
        return this.keepAlive;
    }

    boolean isCompleted() {
        return this.requestState == MessageState.COMPLETE && this.responseState == MessageState.COMPLETE;
    }

    void terminateExchange(HttpException ex) throws HttpException, IOException {
        if (this.done.get() || this.requestState != MessageState.HEADERS) {
            throw new ProtocolException("Unexpected message head");
        }
        this.receivedRequest = null;
        this.requestState = MessageState.COMPLETE;
        BasicHttpResponse response = new BasicHttpResponse(ServerSupport.toStatusCode(ex));
        response.addHeader("Connection", "close");
        BasicResponseProducer responseProducer = new BasicResponseProducer((HttpResponse)response, ServerSupport.toErrorMessage(ex));
        this.exchangeHandler = new ImmediateResponseExchangeHandler(responseProducer);
        this.exchangeHandler.handleRequest(null, null, this.responseChannel, this.context);
    }

    void consumeHeader(HttpRequest request, EntityDetails requestEntityDetails) throws HttpException, IOException {
        AsyncServerExchangeHandler handler;
        if (this.done.get() || this.requestState != MessageState.HEADERS) {
            throw new ProtocolException("Unexpected message head");
        }
        this.receivedRequest = request;
        this.requestState = requestEntityDetails == null ? MessageState.COMPLETE : MessageState.BODY;
        try {
            handler = this.exchangeHandlerFactory.create(request, this.context);
        } catch (MisdirectedRequestException ex) {
            handler = new ImmediateResponseExchangeHandler(421, ex.getMessage());
        } catch (HttpException ex) {
            handler = new ImmediateResponseExchangeHandler(500, ex.getMessage());
        }
        if (handler == null) {
            handler = new ImmediateResponseExchangeHandler(404, "Cannot handle request");
        }
        this.exchangeHandler = handler;
        ProtocolVersion transportVersion = request.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
            throw new UnsupportedHttpVersionException(transportVersion);
        }
        this.context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        this.context.setAttribute("http.request", request);
        try {
            this.httpProcessor.process(request, requestEntityDetails, (HttpContext)this.context);
            this.exchangeHandler.handleRequest(request, requestEntityDetails, this.responseChannel, this.context);
        } catch (HttpException ex) {
            if (!this.responseCommitted.get()) {
                BasicHttpResponse response = new BasicHttpResponse(ServerSupport.toStatusCode(ex));
                response.addHeader("Connection", "close");
                BasicResponseProducer responseProducer = new BasicResponseProducer((HttpResponse)response, ServerSupport.toErrorMessage(ex));
                this.exchangeHandler = new ImmediateResponseExchangeHandler(responseProducer);
                this.exchangeHandler.handleRequest(request, requestEntityDetails, this.responseChannel, this.context);
            }
            throw ex;
        }
    }

    boolean isOutputReady() {
        switch (this.responseState) {
            case BODY: {
                return this.exchangeHandler.available() > 0;
            }
        }
        return false;
    }

    void produceOutput() throws HttpException, IOException {
        switch (this.responseState) {
            case BODY: {
                this.exchangeHandler.produce(this.internalDataChannel);
            }
        }
    }

    void consumeData(ByteBuffer src) throws HttpException, IOException {
        if (this.done.get() || this.requestState != MessageState.BODY) {
            throw new ProtocolException("Unexpected message data");
        }
        if (this.responseState == MessageState.ACK) {
            this.outputChannel.requestOutput();
        }
        this.exchangeHandler.consume(src);
    }

    void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.exchangeHandler.updateCapacity(capacityChannel);
    }

    void dataEnd(List<? extends Header> trailers) throws HttpException, IOException {
        if (this.done.get() || this.requestState != MessageState.BODY) {
            throw new ProtocolException("Unexpected message data");
        }
        this.requestState = MessageState.COMPLETE;
        this.exchangeHandler.streamEnd(trailers);
    }

    void failed(Exception cause) {
        if (!this.done.get()) {
            this.exchangeHandler.failed(cause);
        }
    }

    @Override
    public void releaseResources() {
        if (this.done.compareAndSet(false, true)) {
            this.requestState = MessageState.COMPLETE;
            this.responseState = MessageState.COMPLETE;
            this.exchangeHandler.releaseResources();
        }
    }

    void appendState(StringBuilder buf) {
        buf.append("requestState=").append((Object)this.requestState).append(", responseState=").append((Object)this.responseState).append(", responseCommitted=").append(this.responseCommitted).append(", keepAlive=").append(this.keepAlive).append(", done=").append(this.done);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        this.appendState(buf);
        buf.append("]");
        return buf.toString();
    }
}

