/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.hc.client5.http.impl.io.LoggingSocketHolder;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultBHttpClientConnection;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultManagedHttpClientConnection
extends DefaultBHttpClientConnection
implements ManagedHttpClientConnection,
Identifiable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultManagedHttpClientConnection.class);
    private static final Logger HEADER_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http.headers");
    private static final Logger WIRE_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http.wire");
    private final String id;
    private final AtomicBoolean closed;
    private Timeout socketTimeout;

    public DefaultManagedHttpClientConnection(String id, CharsetDecoder charDecoder, CharsetEncoder charEncoder, Http1Config h1Config, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory, HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        super(h1Config, charDecoder, charEncoder, incomingContentStrategy, outgoingContentStrategy, requestWriterFactory, responseParserFactory);
        this.id = id;
        this.closed = new AtomicBoolean();
    }

    public DefaultManagedHttpClientConnection(String id) {
        this(id, null, null, null, null, null, null, null);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void bind(SocketHolder socketHolder) throws IOException {
        if (this.closed.get()) {
            Socket socket = socketHolder.getSocket();
            socket.close();
            throw new InterruptedIOException("Connection already shutdown");
        }
        super.bind(socketHolder);
        this.socketTimeout = Timeout.ofMilliseconds(socketHolder.getSocket().getSoTimeout());
    }

    @Override
    public Socket getSocket() {
        SocketHolder socketHolder = this.getSocketHolder();
        return socketHolder != null ? socketHolder.getSocket() : null;
    }

    @Override
    public SSLSession getSSLSession() {
        Socket socket = this.getSocket();
        if (socket instanceof SSLSocket) {
            return ((SSLSocket)socket).getSession();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: Close connection", (Object)this.id);
            }
            super.close();
        }
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: set socket timeout to {}", (Object)this.id, (Object)timeout);
        }
        super.setSocketTimeout(timeout);
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.closed.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: close connection {}", (Object)this.id, (Object)closeMode);
            }
            super.close(closeMode);
        }
    }

    @Override
    public void bind(Socket socket) throws IOException {
        super.bind(WIRE_LOG.isDebugEnabled() ? new LoggingSocketHolder(socket, this.id, WIRE_LOG) : new SocketHolder(socket));
        this.socketTimeout = Timeout.ofMilliseconds(socket.getSoTimeout());
    }

    @Override
    protected void onResponseReceived(ClassicHttpResponse response) {
        if (response != null && HEADER_LOG.isDebugEnabled()) {
            Header[] headers;
            HEADER_LOG.debug("{} << {}", (Object)this.id, (Object)new StatusLine(response));
            for (Header header : headers = response.getHeaders()) {
                HEADER_LOG.debug("{} << {}", (Object)this.id, (Object)header);
            }
        }
    }

    @Override
    protected void onRequestSubmitted(ClassicHttpRequest request) {
        if (request != null && HEADER_LOG.isDebugEnabled()) {
            Header[] headers;
            HEADER_LOG.debug("{} >> {}", (Object)this.id, (Object)new RequestLine(request));
            for (Header header : headers = request.getHeaders()) {
                HEADER_LOG.debug("{} >> {}", (Object)this.id, (Object)header);
            }
        }
    }

    @Override
    public void passivate() {
        super.setSocketTimeout(Timeout.ZERO_MILLISECONDS);
    }

    @Override
    public void activate() {
        super.setSocketTimeout(this.socketTimeout);
    }
}

