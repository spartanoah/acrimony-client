/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.TlsSessionValidator;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.SSLInitializationException;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class SSLConnectionSocketFactory
implements LayeredConnectionSocketFactory {
    private static final String WEAK_KEY_EXCHANGES = "^(TLS|SSL)_(NULL|ECDH_anon|DH_anon|DH_anon_EXPORT|DHE_RSA_EXPORT|DHE_DSS_EXPORT|DSS_EXPORT|DH_DSS_EXPORT|DH_RSA_EXPORT|RSA_EXPORT|KRB5_EXPORT)_(.*)";
    private static final String WEAK_CIPHERS = "^(TLS|SSL)_(.*)_WITH_(NULL|DES_CBC|DES40_CBC|DES_CBC_40|3DES_EDE_CBC|RC4_128|RC4_40|RC2_CBC_40)_(.*)";
    private static final List<Pattern> WEAK_CIPHER_SUITE_PATTERNS = Collections.unmodifiableList(Arrays.asList(Pattern.compile("^(TLS|SSL)_(NULL|ECDH_anon|DH_anon|DH_anon_EXPORT|DHE_RSA_EXPORT|DHE_DSS_EXPORT|DSS_EXPORT|DH_DSS_EXPORT|DH_RSA_EXPORT|RSA_EXPORT|KRB5_EXPORT)_(.*)", 2), Pattern.compile("^(TLS|SSL)_(.*)_WITH_(NULL|DES_CBC|DES40_CBC|DES_CBC_40|3DES_EDE_CBC|RC4_128|RC4_40|RC2_CBC_40)_(.*)", 2)));
    private static final Logger LOG = LoggerFactory.getLogger(SSLConnectionSocketFactory.class);
    private final SSLSocketFactory socketFactory;
    private final HostnameVerifier hostnameVerifier;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;
    private final TlsSessionValidator tlsSessionValidator;

    public static SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(SSLContexts.createDefault(), HttpsSupport.getDefaultHostnameVerifier());
    }

    public static SSLConnectionSocketFactory getSystemSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory((SSLSocketFactory)SSLSocketFactory.getDefault(), HttpsSupport.getSystemProtocols(), HttpsSupport.getSystemCipherSuits(), HttpsSupport.getDefaultHostnameVerifier());
    }

    static boolean isWeakCipherSuite(String cipherSuite) {
        for (Pattern pattern : WEAK_CIPHER_SUITE_PATTERNS) {
            if (!pattern.matcher(cipherSuite).matches()) continue;
            return true;
        }
        return false;
    }

    public SSLConnectionSocketFactory(SSLContext sslContext) {
        this(sslContext, HttpsSupport.getDefaultHostnameVerifier());
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), null, null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(), supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLSocketFactory socketFactory, HostnameVerifier hostnameVerifier) {
        this(socketFactory, null, null, hostnameVerifier);
    }

    public SSLConnectionSocketFactory(SSLSocketFactory socketFactory, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        this.socketFactory = Args.notNull(socketFactory, "SSL socket factory");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : HttpsSupport.getDefaultHostnameVerifier();
        this.tlsSessionValidator = new TlsSessionValidator(LOG);
    }

    protected void prepareSocket(SSLSocket socket) throws IOException {
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return SocketFactory.getDefault().createSocket();
    }

    @Override
    public Socket connectSocket(final TimeValue connectTimeout, Socket socket, HttpHost host, final InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        Socket sock;
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        Socket socket2 = sock = socket != null ? socket : this.createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connecting socket to {} with timeout {}", (Object)remoteAddress, (Object)connectTimeout);
            }
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                    @Override
                    public Object run() throws IOException {
                        sock.connect(remoteAddress, connectTimeout != null ? connectTimeout.toMillisecondsIntBound() : 0);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                Asserts.check(e.getCause() instanceof IOException, "method contract violation only checked exceptions are wrapped: " + e.getCause());
                throw (IOException)e.getCause();
            }
        } catch (IOException ex) {
            Closer.closeQuietly(sock);
            throw ex;
        }
        if (sock instanceof SSLSocket) {
            SSLSocket sslsock = (SSLSocket)sock;
            LOG.debug("Starting handshake");
            sslsock.startHandshake();
            this.verifyHostname(sslsock, host.getHostName());
            return sock;
        }
        return this.createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context) throws IOException {
        SSLSocket sslsock = (SSLSocket)this.socketFactory.createSocket(socket, target, port, true);
        if (this.supportedProtocols != null) {
            sslsock.setEnabledProtocols(this.supportedProtocols);
        } else {
            sslsock.setEnabledProtocols(TLS.excludeWeak(sslsock.getEnabledProtocols()));
        }
        if (this.supportedCipherSuites != null) {
            sslsock.setEnabledCipherSuites(this.supportedCipherSuites);
        } else {
            sslsock.setEnabledCipherSuites(TlsCiphers.excludeWeak(sslsock.getEnabledCipherSuites()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Enabled protocols: {}", (Object)sslsock.getEnabledProtocols());
            LOG.debug("Enabled cipher suites: {}", (Object)sslsock.getEnabledCipherSuites());
        }
        this.prepareSocket(sslsock);
        LOG.debug("Starting handshake");
        sslsock.startHandshake();
        this.verifyHostname(sslsock, target);
        return sslsock;
    }

    private void verifyHostname(SSLSocket sslsock, String hostname) throws IOException {
        try {
            SSLSession session = sslsock.getSession();
            if (session == null) {
                InputStream in = sslsock.getInputStream();
                in.available();
                session = sslsock.getSession();
                if (session == null) {
                    sslsock.startHandshake();
                    session = sslsock.getSession();
                }
            }
            if (session == null) {
                throw new SSLHandshakeException("SSL session not available");
            }
            this.verifySession(hostname, session);
        } catch (IOException iox) {
            Closer.closeQuietly(sslsock);
            throw iox;
        }
    }

    protected void verifySession(String hostname, SSLSession sslSession) throws SSLException {
        this.tlsSessionValidator.verifySession(hostname, sslSession, this.hostnameVerifier);
    }
}

