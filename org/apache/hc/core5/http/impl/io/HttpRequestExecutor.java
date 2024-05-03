/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpResponseInformationCallback;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class HttpRequestExecutor {
    public static final Timeout DEFAULT_WAIT_FOR_CONTINUE = Timeout.ofSeconds(3L);
    private final Timeout waitForContinue;
    private final ConnectionReuseStrategy connReuseStrategy;
    private final Http1StreamListener streamListener;

    public HttpRequestExecutor(Timeout waitForContinue, ConnectionReuseStrategy connReuseStrategy, Http1StreamListener streamListener) {
        this.waitForContinue = Args.positive(waitForContinue, "Wait for continue time");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public HttpRequestExecutor(ConnectionReuseStrategy connReuseStrategy) {
        this(DEFAULT_WAIT_FOR_CONTINUE, connReuseStrategy, null);
    }

    public HttpRequestExecutor() {
        this(DEFAULT_WAIT_FOR_CONTINUE, null, null);
    }

    public ClassicHttpResponse execute(ClassicHttpRequest request, HttpClientConnection conn, HttpResponseInformationCallback informationCallback, HttpContext context) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(conn, "Client connection");
        Args.notNull(context, "HTTP context");
        try {
            context.setAttribute("http.ssl-session", conn.getSSLSession());
            context.setAttribute("http.connection-endpoint", conn.getEndpointDetails());
            conn.sendRequestHeader(request);
            if (this.streamListener != null) {
                this.streamListener.onRequestHead(conn, request);
            }
            boolean expectContinue = false;
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                Header expect = request.getFirstHeader("Expect");
                boolean bl = expectContinue = expect != null && "100-continue".equalsIgnoreCase(expect.getValue());
                if (!expectContinue) {
                    conn.sendRequestEntity(request);
                }
            }
            conn.flush();
            ClassicHttpResponse response = null;
            while (response == null) {
                int status;
                if (expectContinue) {
                    if (conn.isDataAvailable(this.waitForContinue)) {
                        response = conn.receiveResponseHeader();
                        if (this.streamListener != null) {
                            this.streamListener.onResponseHead(conn, response);
                        }
                        if ((status = response.getCode()) == 100) {
                            response = null;
                            conn.sendRequestEntity(request);
                        } else {
                            if (status < 200) {
                                if (informationCallback != null) {
                                    informationCallback.execute(response, conn, context);
                                }
                                response = null;
                                continue;
                            }
                            if (status >= 400) {
                                conn.terminateRequest(request);
                            } else {
                                conn.sendRequestEntity(request);
                            }
                        }
                    } else {
                        conn.sendRequestEntity(request);
                    }
                    conn.flush();
                    expectContinue = false;
                    continue;
                }
                response = conn.receiveResponseHeader();
                if (this.streamListener != null) {
                    this.streamListener.onResponseHead(conn, response);
                }
                if ((status = response.getCode()) < 100) {
                    throw new ProtocolException("Invalid response: " + new StatusLine(response));
                }
                if (status >= 200) continue;
                if (informationCallback != null && status != 100) {
                    informationCallback.execute(response, conn, context);
                }
                response = null;
            }
            if (MessageSupport.canResponseHaveBody(request.getMethod(), response)) {
                conn.receiveResponseEntity(response);
            }
            return response;
        } catch (IOException | RuntimeException | HttpException ex) {
            Closer.closeQuietly(conn);
            throw ex;
        }
    }

    public ClassicHttpResponse execute(ClassicHttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        return this.execute(request, conn, null, context);
    }

    public void preProcess(ClassicHttpRequest request, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(processor, "HTTP processor");
        Args.notNull(context, "HTTP context");
        ProtocolVersion transportVersion = request.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
            throw new UnsupportedHttpVersionException(transportVersion);
        }
        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        context.setAttribute("http.request", request);
        processor.process(request, (EntityDetails)request.getEntity(), context);
    }

    public void postProcess(ClassicHttpResponse response, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        Args.notNull(processor, "HTTP processor");
        Args.notNull(context, "HTTP context");
        ProtocolVersion transportVersion = response.getVersion();
        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        context.setAttribute("http.response", response);
        processor.process(response, (EntityDetails)response.getEntity(), context);
    }

    public boolean keepAlive(ClassicHttpRequest request, ClassicHttpResponse response, HttpClientConnection connection, HttpContext context) throws IOException {
        boolean keepAlive;
        Args.notNull(connection, "HTTP connection");
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        boolean bl = keepAlive = connection.isConsistent() && this.connReuseStrategy.keepAlive(request, response, context);
        if (this.streamListener != null) {
            this.streamListener.onExchangeComplete(connection, keepAlive);
        }
        return keepAlive;
    }
}

