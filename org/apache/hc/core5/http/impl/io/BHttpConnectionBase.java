/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicEndpointDetails;
import org.apache.hc.core5.http.impl.BasicHttpConnectionMetrics;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.io.ChunkedInputStream;
import org.apache.hc.core5.http.impl.io.ChunkedOutputStream;
import org.apache.hc.core5.http.impl.io.ContentLengthInputStream;
import org.apache.hc.core5.http.impl.io.ContentLengthOutputStream;
import org.apache.hc.core5.http.impl.io.EmptyInputStream;
import org.apache.hc.core5.http.impl.io.IdentityInputStream;
import org.apache.hc.core5.http.impl.io.IdentityOutputStream;
import org.apache.hc.core5.http.impl.io.IncomingHttpEntity;
import org.apache.hc.core5.http.impl.io.SessionInputBufferImpl;
import org.apache.hc.core5.http.impl.io.SessionOutputBufferImpl;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.apache.hc.core5.http.io.BHttpConnection;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

class BHttpConnectionBase
implements BHttpConnection {
    final Http1Config http1Config;
    final SessionInputBufferImpl inBuffer;
    final SessionOutputBufferImpl outbuffer;
    final BasicHttpConnectionMetrics connMetrics;
    final AtomicReference<SocketHolder> socketHolderRef;
    volatile ProtocolVersion version;
    volatile EndpointDetails endpointDetails;

    BHttpConnectionBase(Http1Config http1Config, CharsetDecoder charDecoder, CharsetEncoder charEncoder) {
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        BasicHttpTransportMetrics inTransportMetrics = new BasicHttpTransportMetrics();
        BasicHttpTransportMetrics outTransportMetrics = new BasicHttpTransportMetrics();
        this.inBuffer = new SessionInputBufferImpl(inTransportMetrics, this.http1Config.getBufferSize(), -1, this.http1Config.getMaxLineLength(), charDecoder);
        this.outbuffer = new SessionOutputBufferImpl(outTransportMetrics, this.http1Config.getBufferSize(), this.http1Config.getChunkSizeHint(), charEncoder);
        this.connMetrics = new BasicHttpConnectionMetrics(inTransportMetrics, outTransportMetrics);
        this.socketHolderRef = new AtomicReference();
    }

    protected SocketHolder ensureOpen() throws IOException {
        SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder == null) {
            throw new ConnectionClosedException();
        }
        return socketHolder;
    }

    protected void bind(Socket socket) throws IOException {
        Args.notNull(socket, "Socket");
        this.bind(new SocketHolder(socket));
    }

    protected void bind(SocketHolder socketHolder) throws IOException {
        Args.notNull(socketHolder, "Socket holder");
        this.socketHolderRef.set(socketHolder);
        this.endpointDetails = null;
    }

    @Override
    public boolean isOpen() {
        return this.socketHolderRef.get() != null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version;
    }

    protected SocketHolder getSocketHolder() {
        return this.socketHolderRef.get();
    }

    protected OutputStream createContentOutputStream(long len, SessionOutputBuffer buffer, OutputStream outputStream, Supplier<List<? extends Header>> trailers) {
        if (len >= 0L) {
            return new ContentLengthOutputStream(buffer, outputStream, len);
        }
        if (len == -1L) {
            int chunkSizeHint = this.http1Config.getChunkSizeHint() >= 0 ? this.http1Config.getChunkSizeHint() : 2048;
            return new ChunkedOutputStream(buffer, outputStream, chunkSizeHint, trailers);
        }
        return new IdentityOutputStream(buffer, outputStream);
    }

    protected InputStream createContentInputStream(long len, SessionInputBuffer buffer, InputStream inputStream) {
        if (len > 0L) {
            return new ContentLengthInputStream(buffer, inputStream, len);
        }
        if (len == 0L) {
            return EmptyInputStream.INSTANCE;
        }
        if (len == -1L) {
            return new ChunkedInputStream(buffer, inputStream, this.http1Config);
        }
        return new IdentityInputStream(buffer, inputStream);
    }

    HttpEntity createIncomingEntity(HttpMessage message, SessionInputBuffer inBuffer, InputStream inputStream, long len) {
        return new IncomingHttpEntity(this.createContentInputStream(len, inBuffer, inputStream), len >= 0L ? len : -1L, len == -1L, message.getFirstHeader("Content-Type"), message.getFirstHeader("Content-Encoding"));
    }

    @Override
    public SocketAddress getRemoteAddress() {
        SocketHolder socketHolder = this.socketHolderRef.get();
        return socketHolder != null ? socketHolder.getSocket().getRemoteSocketAddress() : null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        SocketHolder socketHolder = this.socketHolderRef.get();
        return socketHolder != null ? socketHolder.getSocket().getLocalSocketAddress() : null;
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                socketHolder.getSocket().setSoTimeout(Timeout.defaultsToDisabled(timeout).toMillisecondsIntBound());
            } catch (SocketException socketException) {
                // empty catch block
            }
        }
    }

    @Override
    public Timeout getSocketTimeout() {
        SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                return Timeout.ofMilliseconds(socketHolder.getSocket().getSoTimeout());
            } catch (SocketException socketException) {
                // empty catch block
            }
        }
        return Timeout.DISABLED;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close(CloseMode closeMode) {
        SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        if (socketHolder != null) {
            Socket socket = socketHolder.getSocket();
            try {
                if (closeMode == CloseMode.IMMEDIATE) {
                    socket.setSoLinger(true, 0);
                }
            } catch (IOException ignore) {
            } finally {
                Closer.closeQuietly(socket);
            }
        }
    }

    @Override
    public void close() throws IOException {
        SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        if (socketHolder != null) {
            try (Socket socket = socketHolder.getSocket();){
                this.inBuffer.clear();
                this.outbuffer.flush(socketHolder.getOutputStream());
                try {
                    try {
                        socket.shutdownOutput();
                    } catch (IOException ignore) {
                        // empty catch block
                    }
                    try {
                        socket.shutdownInput();
                    } catch (IOException ignore) {
                    }
                } catch (UnsupportedOperationException ignore) {
                    // empty catch block
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int fillInputBuffer(Timeout timeout) throws IOException {
        SocketHolder socketHolder = this.ensureOpen();
        Socket socket = socketHolder.getSocket();
        int oldtimeout = socket.getSoTimeout();
        try {
            socket.setSoTimeout(timeout.toMillisecondsIntBound());
            int n = this.inBuffer.fillBuffer(socketHolder.getInputStream());
            return n;
        } finally {
            socket.setSoTimeout(oldtimeout);
        }
    }

    protected boolean awaitInput(Timeout timeout) throws IOException {
        if (this.inBuffer.hasBufferedData()) {
            return true;
        }
        this.fillInputBuffer(timeout);
        return this.inBuffer.hasBufferedData();
    }

    @Override
    public boolean isDataAvailable(Timeout timeout) throws IOException {
        this.ensureOpen();
        try {
            return this.awaitInput(timeout);
        } catch (SocketTimeoutException ex) {
            return false;
        }
    }

    @Override
    public boolean isStale() throws IOException {
        if (!this.isOpen()) {
            return true;
        }
        try {
            int bytesRead = this.fillInputBuffer(Timeout.ofMilliseconds(1L));
            return bytesRead < 0;
        } catch (SocketTimeoutException ex) {
            return false;
        } catch (SocketException ex) {
            return true;
        }
    }

    @Override
    public void flush() throws IOException {
        SocketHolder socketHolder = this.ensureOpen();
        this.outbuffer.flush(socketHolder.getOutputStream());
    }

    protected void incrementRequestCount() {
        this.connMetrics.incrementRequestCount();
    }

    protected void incrementResponseCount() {
        this.connMetrics.incrementResponseCount();
    }

    @Override
    public SSLSession getSSLSession() {
        SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            Socket socket = socketHolder.getSocket();
            return socket instanceof SSLSocket ? ((SSLSocket)socket).getSession() : null;
        }
        return null;
    }

    @Override
    public EndpointDetails getEndpointDetails() {
        SocketHolder socketHolder;
        if (this.endpointDetails == null && (socketHolder = this.socketHolderRef.get()) != null) {
            Timeout socketTimeout;
            Socket socket = socketHolder.getSocket();
            try {
                socketTimeout = Timeout.ofMilliseconds(socket.getSoTimeout());
            } catch (SocketException e) {
                socketTimeout = Timeout.DISABLED;
            }
            this.endpointDetails = new BasicEndpointDetails(socket.getRemoteSocketAddress(), socket.getLocalSocketAddress(), this.connMetrics, socketTimeout);
        }
        return this.endpointDetails;
    }

    public String toString() {
        SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            Socket socket = socketHolder.getSocket();
            StringBuilder buffer = new StringBuilder();
            SocketAddress remoteAddress = socket.getRemoteSocketAddress();
            SocketAddress localAddress = socket.getLocalSocketAddress();
            if (remoteAddress != null && localAddress != null) {
                InetAddressUtils.formatAddress(buffer, localAddress);
                buffer.append("<->");
                InetAddressUtils.formatAddress(buffer, remoteAddress);
            }
            return buffer.toString();
        }
        return "[Not bound]";
    }
}

