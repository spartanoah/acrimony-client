/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.ServerSupport;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.BasicHttpServerExpectationDecorator;
import org.apache.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpService {
    private final HttpProcessor processor;
    private final HttpServerRequestHandler requestHandler;
    private final ConnectionReuseStrategy connReuseStrategy;
    private final Http1StreamListener streamListener;

    public HttpService(HttpProcessor processor, HttpRequestMapper<HttpRequestHandler> handlerMapper, ConnectionReuseStrategy connReuseStrategy, HttpResponseFactory<ClassicHttpResponse> responseFactory, Http1StreamListener streamListener) {
        this(processor, new BasicHttpServerExpectationDecorator(new BasicHttpServerRequestHandler(handlerMapper, responseFactory)), connReuseStrategy, streamListener);
    }

    public HttpService(HttpProcessor processor, HttpRequestMapper<HttpRequestHandler> handlerMapper, ConnectionReuseStrategy connReuseStrategy, HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this(processor, handlerMapper, connReuseStrategy, responseFactory, null);
    }

    public HttpService(HttpProcessor processor, HttpServerRequestHandler requestHandler, ConnectionReuseStrategy connReuseStrategy, Http1StreamListener streamListener) {
        this.processor = Args.notNull(processor, "HTTP processor");
        this.requestHandler = Args.notNull(requestHandler, "Request handler");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public HttpService(HttpProcessor processor, HttpServerRequestHandler requestHandler) {
        this(processor, requestHandler, null, null);
    }

    public void handleRequest(final HttpServerConnection conn, final HttpContext context) throws IOException, HttpException {
        final AtomicBoolean responseSubmitted = new AtomicBoolean(false);
        try {
            final ClassicHttpRequest request = conn.receiveRequestHeader();
            if (this.streamListener != null) {
                this.streamListener.onRequestHead(conn, request);
            }
            conn.receiveRequestEntity(request);
            ProtocolVersion transportVersion = request.getVersion();
            context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
            context.setAttribute("http.ssl-session", conn.getSSLSession());
            context.setAttribute("http.connection-endpoint", conn.getEndpointDetails());
            context.setAttribute("http.request", request);
            this.processor.process(request, (EntityDetails)request.getEntity(), context);
            this.requestHandler.handle(request, new HttpServerRequestHandler.ResponseTrigger(){

                @Override
                public void sendInformation(ClassicHttpResponse response) throws HttpException, IOException {
                    if (responseSubmitted.get()) {
                        throw new HttpException("Response already submitted");
                    }
                    if (response.getCode() >= 200) {
                        throw new HttpException("Invalid intermediate response");
                    }
                    if (HttpService.this.streamListener != null) {
                        HttpService.this.streamListener.onResponseHead(conn, response);
                    }
                    conn.sendResponseHeader(response);
                    conn.flush();
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void submitResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    try {
                        ProtocolVersion transportVersion = response.getVersion();
                        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
                            throw new UnsupportedHttpVersionException(transportVersion);
                        }
                        ServerSupport.validateResponse(response, response.getEntity());
                        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
                        context.setAttribute("http.response", response);
                        HttpService.this.processor.process(response, (EntityDetails)response.getEntity(), context);
                        responseSubmitted.set(true);
                        conn.sendResponseHeader(response);
                        if (HttpService.this.streamListener != null) {
                            HttpService.this.streamListener.onResponseHead(conn, response);
                        }
                        if (MessageSupport.canResponseHaveBody(request.getMethod(), response)) {
                            conn.sendResponseEntity(response);
                        }
                        EntityUtils.consume(request.getEntity());
                        boolean keepAlive = HttpService.this.connReuseStrategy.keepAlive(request, response, context);
                        if (HttpService.this.streamListener != null) {
                            HttpService.this.streamListener.onExchangeComplete(conn, keepAlive);
                        }
                        if (!keepAlive) {
                            conn.close();
                        }
                        conn.flush();
                    } finally {
                        response.close();
                    }
                }
            }, context);
        } catch (HttpException ex) {
            if (responseSubmitted.get()) {
                throw ex;
            }
            try (BasicClassicHttpResponse errorResponse = new BasicClassicHttpResponse(500);){
                this.handleException(ex, errorResponse);
                errorResponse.setHeader("Connection", "close");
                context.setAttribute("http.response", errorResponse);
                this.processor.process(errorResponse, (EntityDetails)errorResponse.getEntity(), context);
                conn.sendResponseHeader(errorResponse);
                if (this.streamListener != null) {
                    this.streamListener.onResponseHead(conn, errorResponse);
                }
                conn.sendResponseEntity(errorResponse);
                conn.close();
            }
        }
    }

    protected void handleException(HttpException ex, ClassicHttpResponse response) {
        response.setCode(this.toStatusCode(ex));
        response.setEntity(new StringEntity(ServerSupport.toErrorMessage(ex), ContentType.TEXT_PLAIN));
    }

    protected int toStatusCode(Exception ex) {
        return ServerSupport.toStatusCode(ex);
    }
}

